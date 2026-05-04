package com.twinmind.recorder.service

import android.app.*
import android.content.*
import android.media.*
import android.os.*
import android.telephony.*
import androidx.core.app.NotificationCompat
import com.twinmind.recorder.MainActivity
import com.twinmind.recorder.data.local.entity.AudioChunkEntity
import com.twinmind.recorder.data.local.entity.SessionStatus
import com.twinmind.recorder.data.repository.RecordingRepository
import com.twinmind.recorder.worker.TranscriptionWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────────────────────
// Pause reasons as bitmask flags — multiple can be active simultaneously
// ─────────────────────────────────────────────────────────────────────────────
private object PauseReason {
    const val NONE        = 0
    const val PHONE_CALL  = 1 shl 0   // Case 1
    const val AUDIO_FOCUS = 1 shl 1   // Case 2
    const val LOW_STORAGE = 1 shl 2   // Case 4
}

@AndroidEntryPoint
class RecordingService : Service() {

    @Inject lateinit var repository: RecordingRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── MediaRecorder ──
    private var mediaRecorder: MediaRecorder? = null
    private var currentSessionId: String? = null
    private var chunkIndex = 0
    private var chunkStartTime = 0L
    private var currentChunkFile: File? = null

    // ── Jobs ──
    private var timerJob: Job? = null
    private var chunkJob: Job? = null
    private var silenceCheckJob: Job? = null
    private var storageCheckJob: Job? = null

    // ── State ──
    private var isRecording = false
    private var pauseFlags = PauseReason.NONE
    private val isPaused get() = pauseFlags != PauseReason.NONE

    // Accurate elapsed time — excludes paused durations
    private var totalElapsedMs = 0L
    private var lastResumeTime = 0L

    // ── Constants ──
    private val CHUNK_DURATION_MS    = 30_000L
    private val OVERLAP_MS           = 2_000L
    private val MIN_STORAGE_BYTES    = 50L * 1024 * 1024   // 50 MB
    private val SILENCE_THRESHOLD_MS = 10_000L

    // ── Listeners ──
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var telephonyManager: TelephonyManager? = null
    private var phoneStateListener: PhoneStateListener? = null
    private var telephonyCallback: TelephonyCallback? = null
    private var headsetReceiver: BroadcastReceiver? = null

    companion object {
        const val CHANNEL_ID       = "recording_channel"
        const val NOTIFICATION_ID  = 1
        const val ACTION_START     = "ACTION_START"
        const val ACTION_STOP      = "ACTION_STOP"
        const val ACTION_RESUME    = "ACTION_RESUME"   // for notification Resume button
        const val EXTRA_SESSION_ID = "session_id"

        // Broadcast to UI
        const val BROADCAST_STATUS  = "com.twinmind.recorder.STATUS"
        const val EXTRA_STATUS_TEXT = "status_text"
        const val EXTRA_ELAPSED_MS  = "elapsed_ms"
        const val EXTRA_IS_PAUSED   = "is_paused"
        const val EXTRA_IS_STOPPED  = "is_stopped"
        const val EXTRA_WARNING     = "warning"

        fun startRecording(context: Context, sessionId: String) {
            context.startForegroundService(
                Intent(context, RecordingService::class.java).apply {
                    action = ACTION_START
                    putExtra(EXTRA_SESSION_ID, sessionId)
                }
            )
        }

        fun stopRecording(context: Context) {
            context.startService(
                Intent(context, RecordingService::class.java).apply { action = ACTION_STOP }
            )
        }
    }

    // =========================================================================
    // Lifecycle
    // =========================================================================

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        registerPhoneListener()    // Case 1
        registerHeadsetReceiver()  // Case 3
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: return START_NOT_STICKY
                startForeground(NOTIFICATION_ID, buildNotification("Recording", paused = false))
                startRecordingSession(sessionId)
            }
            ACTION_STOP   -> stopRecordingSession()
            // Case 2: user taps Resume on the notification after audio focus loss
            ACTION_RESUME -> clearPause(PauseReason.AUDIO_FOCUS)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        super.onDestroy()
        unregisterPhoneListener()
        unregisterHeadsetReceiver()
        abandonAudioFocus()
        serviceScope.cancel()
        releaseRecorder()
    }

    // =========================================================================
    // Session control
    // =========================================================================

    private fun startRecordingSession(sessionId: String) {
        currentSessionId = sessionId
        lastResumeTime   = System.currentTimeMillis()
        totalElapsedMs   = 0L
        chunkIndex       = 0
        isRecording      = true
        pauseFlags       = PauseReason.NONE

        // Case 4: pre-flight storage check
        if (!checkStorage()) return

        // Case 5: persist RECORDING state so BootReceiver can recover it
        serviceScope.launch {
            repository.updateSessionStatus(sessionId, SessionStatus.RECORDING)
        }

        requestAudioFocus()        // Case 2
        startNewChunk()
        scheduleNextChunk()
        startTimerUpdates()
        startStorageMonitor()      // Case 4
        startSilenceDetection()    // Case 6
    }

    private fun stopRecordingSession() {
        if (!isRecording) return
        isRecording = false

        timerJob?.cancel()
        chunkJob?.cancel()
        silenceCheckJob?.cancel()
        storageCheckJob?.cancel()

        saveCurrentChunk()
        abandonAudioFocus()

        val sessionId = currentSessionId
        if (sessionId != null) {
            serviceScope.launch {
                repository.updateSessionDuration(sessionId, accumulatedElapsed())
                repository.updateSessionStatus(sessionId, SessionStatus.TRANSCRIBING)
            }
        }

        broadcastStatus(stopped = true)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // =========================================================================
    // Case 1 — Phone calls
    // =========================================================================

    private fun registerPhoneListener() {
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val cb = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) = handleCallState(state)
            }
            telephonyCallback = cb
            telephonyManager?.registerTelephonyCallback(mainExecutor, cb)
        } else {
            @Suppress("DEPRECATION")
            val l = object : PhoneStateListener() {
                override fun onCallStateChanged(state: Int, number: String?) = handleCallState(state)
            }
            phoneStateListener = l
            @Suppress("DEPRECATION")
            telephonyManager?.listen(l, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    private fun handleCallState(state: Int) {
        if (!isRecording) return
        when (state) {
            TelephonyManager.CALL_STATE_RINGING,
            TelephonyManager.CALL_STATE_OFFHOOK -> setPause(PauseReason.PHONE_CALL, "Paused - Phone call")
            TelephonyManager.CALL_STATE_IDLE    -> clearPause(PauseReason.PHONE_CALL)
        }
    }

    private fun unregisterPhoneListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyCallback?.let { telephonyManager?.unregisterTelephonyCallback(it) }
        } else {
            @Suppress("DEPRECATION")
            phoneStateListener?.let { telephonyManager?.listen(it, PhoneStateListener.LISTEN_NONE) }
        }
    }

    // =========================================================================
    // Case 2 — Audio focus loss
    // =========================================================================

    private fun requestAudioFocus() {
        val listener = AudioManager.OnAudioFocusChangeListener { change ->
            if (!isRecording) return@OnAudioFocusChangeListener
            when (change) {
                AudioManager.AUDIOFOCUS_LOSS,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->
                    // Show "Paused – Audio focus lost" with Resume + Stop actions
                    setPause(PauseReason.AUDIO_FOCUS, "Paused - Audio focus lost", showResume = true)
                AudioManager.AUDIOFOCUS_GAIN ->
                    clearPause(PauseReason.AUDIO_FOCUS)
            }
        }
        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setOnAudioFocusChangeListener(listener)
            .setWillPauseWhenDucked(false)
            .build()
        audioManager?.requestAudioFocus(audioFocusRequest!!)
    }

    private fun abandonAudioFocus() {
        audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        audioFocusRequest = null
    }

    // =========================================================================
    // Case 3 — Microphone source changes (wired + Bluetooth)
    // Per spec: CONTINUE recording, just show a notification of the change.
    // Android's AudioSource.MIC re-routes automatically on headset events.
    // =========================================================================

    private fun registerHeadsetReceiver() {
        headsetReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (!isRecording) return
                when (intent?.action) {
                    Intent.ACTION_HEADSET_PLUG -> {
                        val state = intent.getIntExtra("state", -1)
                        val msg = if (state == 1)
                            "Wired headset connected - continuing recording"
                        else
                            "Wired headset disconnected - continuing recording"
                        updateNotification(msg, paused = isPaused)
                        broadcastWarning(msg)
                    }
                    AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED -> {
                        val state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1)
                        val msg = when (state) {
                            AudioManager.SCO_AUDIO_STATE_CONNECTED    ->
                                "Bluetooth headset connected - continuing recording"
                            AudioManager.SCO_AUDIO_STATE_DISCONNECTED ->
                                "Bluetooth headset disconnected - continuing recording"
                            else -> return
                        }
                        updateNotification(msg, paused = isPaused)
                        broadcastWarning(msg)
                    }
                }
            }
        }
        registerReceiver(headsetReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_HEADSET_PLUG)
            addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
        })
    }

    private fun unregisterHeadsetReceiver() {
        headsetReceiver?.let { try { unregisterReceiver(it) } catch (_: Exception) {} }
    }

    // =========================================================================
    // Case 4 — Low storage
    // =========================================================================

    /** Returns true if storage is OK, false if critically low (and stops session). */
    private fun checkStorage(): Boolean {
        val stat = StatFs(filesDir.absolutePath)
        val free = stat.availableBlocksLong * stat.blockSizeLong
        return if (free < MIN_STORAGE_BYTES) {
            setPause(PauseReason.LOW_STORAGE, "Recording stopped - Low storage")
            stopRecordingSession()
            false
        } else true
    }

    private fun startStorageMonitor() {
        storageCheckJob?.cancel()
        storageCheckJob = serviceScope.launch {
            while (isRecording) {
                delay(10_000)   // check every 10 s
                checkStorage()
            }
        }
    }

    // =========================================================================
    // Case 5 — Process death recovery
    // Session status is persisted as RECORDING in Room. On boot/restart,
    // BootReceiver finds any session with status=RECORDING and enqueues
    // RecoveryWorker to finalize the last chunk and resume transcription.
    // See BootReceiver.kt and RecoveryWorker.kt.
    // =========================================================================

    // =========================================================================
    // Case 6 — Silent audio detection
    // =========================================================================

    private fun startSilenceDetection() {
        silenceCheckJob?.cancel()
        silenceCheckJob = serviceScope.launch {
            delay(SILENCE_THRESHOLD_MS)
            if (!isRecording || isPaused) return@launch
            // maxAmplitude returns 0 if the mic has recorded nothing since last call
            val amplitude = mediaRecorder?.maxAmplitude ?: 0
            if (amplitude == 0) {
                val msg = "No audio detected - Check microphone"
                updateNotification(msg, paused = false)
                broadcastWarning(msg)
            }
        }
    }

    // =========================================================================
    // Pause / Resume core logic
    // =========================================================================

    private fun setPause(reason: Int, notificationText: String, showResume: Boolean = false) {
        val wasAlreadyPaused = isPaused
        pauseFlags = pauseFlags or reason

        if (!wasAlreadyPaused) {
            // Snapshot elapsed time before going silent
            totalElapsedMs += System.currentTimeMillis() - lastResumeTime
            chunkJob?.cancel()
            saveCurrentChunk()
        }

        updateNotification(notificationText, paused = true, showResume = showResume)
        broadcastStatus()
    }

    private fun clearPause(reason: Int) {
        pauseFlags = pauseFlags and reason.inv()

        if (!isPaused && isRecording) {
            lastResumeTime = System.currentTimeMillis()
            startNewChunk()
            scheduleNextChunk()
            updateNotification(statusText(), paused = false)
            broadcastStatus()
        } else if (isPaused) {
            // Still paused for another reason — just refresh notification text
            updateNotification(statusText(), paused = true)
            broadcastStatus()
        }
    }

    private fun accumulatedElapsed(): Long =
        if (!isPaused && isRecording)
            totalElapsedMs + (System.currentTimeMillis() - lastResumeTime)
        else
            totalElapsedMs

    private fun statusText(): String = when {
        pauseFlags and PauseReason.PHONE_CALL  != 0 -> "Paused - Phone call"
        pauseFlags and PauseReason.AUDIO_FOCUS != 0 -> "Paused - Audio focus lost"
        pauseFlags and PauseReason.LOW_STORAGE != 0 -> "Recording stopped - Low storage"
        else                                         -> "Recording"
    }

    // =========================================================================
    // Chunk management (30-second chunks with 2-second overlap)
    // =========================================================================

    private fun startNewChunk() {
        val sessionId = currentSessionId ?: return
        val file = createChunkFile(sessionId, chunkIndex)
        currentChunkFile = file
        chunkStartTime   = System.currentTimeMillis()

        try {
            mediaRecorder = (
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(this)
                    else @Suppress("DEPRECATION") MediaRecorder()
                    ).apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioSamplingRate(44100)
                    setAudioEncodingBitRate(128000)
                    setOutputFile(file.absolutePath)
                    prepare()
                    start()
                }
        } catch (e: Exception) {
            e.printStackTrace()
            broadcastWarning("Microphone error: ${e.message}")
        }
    }

    private fun saveCurrentChunk() {
        val sessionId = currentSessionId ?: return
        val file      = currentChunkFile   ?: return
        if (!file.exists()) return

        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
            file.delete()   // corrupt — discard
            releaseRecorder()
            return
        }
        releaseRecorder()

        val duration = System.currentTimeMillis() - chunkStartTime
        if (duration < 500L) {
            file.delete()   // too short — discard
            return
        }

        val chunk = AudioChunkEntity(
            id         = UUID.randomUUID().toString(),
            sessionId  = sessionId,
            filePath   = file.absolutePath,
            chunkIndex = chunkIndex,
            createdAt  = chunkStartTime,
            durationMs = duration
        )

        serviceScope.launch {
            repository.saveChunk(chunk)
            TranscriptionWorker.enqueue(applicationContext, sessionId, chunk.id)
        }

        chunkIndex++
    }

    /**
     * Rotates chunks every (CHUNK_DURATION_MS - OVERLAP_MS = 28 s).
     * The new chunk starts 2 s before the old one ends, preserving speech
     * continuity at boundaries for Gemini transcription.
     */
    private fun scheduleNextChunk() {
        chunkJob?.cancel()
        chunkJob = serviceScope.launch {
            while (isRecording && !isPaused) {
                delay(CHUNK_DURATION_MS - OVERLAP_MS)
                if (isRecording && !isPaused) {
                    saveCurrentChunk()
                    startNewChunk()
                }
            }
        }
    }

    // =========================================================================
    // Timer — updates notification and broadcasts elapsed time every second
    // =========================================================================

    private fun startTimerUpdates() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isRecording) {
                val elapsed = accumulatedElapsed()
                currentSessionId?.let { repository.updateSessionDuration(it, elapsed) }

                if (!isPaused) {
                    val mm = (elapsed / 60_000).toString().padStart(2, '0')
                    val ss = (elapsed / 1_000 % 60).toString().padStart(2, '0')
                    updateNotification("Recording... $mm:$ss", paused = false)
                }

                broadcastStatus()
                delay(1_000)
            }
        }
    }

    // =========================================================================
    // Broadcasts to UI
    // =========================================================================

    private fun broadcastStatus(stopped: Boolean = false, warning: String? = null) {
        sendBroadcast(Intent(BROADCAST_STATUS).apply {
            putExtra(EXTRA_STATUS_TEXT, if (stopped) "Stopped" else statusText())
            putExtra(EXTRA_ELAPSED_MS,  accumulatedElapsed())
            putExtra(EXTRA_IS_PAUSED,   isPaused)
            putExtra(EXTRA_IS_STOPPED,  stopped)
            warning?.let { putExtra(EXTRA_WARNING, it) }
        })
    }

    private fun broadcastWarning(msg: String) = broadcastStatus(warning = msg)

    // =========================================================================
    // Notification
    // =========================================================================

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(CHANNEL_ID, "Recording", NotificationManager.IMPORTANCE_LOW)
                .apply {
                    description = "Audio recording in progress"
                    setSound(null, null)
                }.also {
                    getSystemService(NotificationManager::class.java).createNotificationChannel(it)
                }
        }
    }

    private fun buildNotification(
        status: String,
        paused: Boolean,
        showResume: Boolean = false
    ): Notification {
        val stopPi = PendingIntent.getService(
            this, 0,
            Intent(this, RecordingService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val resumePi = PendingIntent.getService(
            this, 1,
            Intent(this, RecordingService::class.java).apply { action = ACTION_RESUME },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val openPi = PendingIntent.getActivity(
            this, 2,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TwinMind")
            .setContentText(status)
            .setSmallIcon(
                if (paused) android.R.drawable.ic_media_pause
                else        android.R.drawable.ic_btn_speak_now
            )
            .setContentIntent(openPi)
            .apply {
                // Case 2: show Resume button only for audio focus loss
                if (showResume) addAction(android.R.drawable.ic_media_play, "Resume", resumePi)
                addAction(android.R.drawable.ic_media_pause, "Stop", stopPi)
            }
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(status: String, paused: Boolean, showResume: Boolean = false) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(status, paused, showResume))
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private fun createChunkFile(sessionId: String, index: Int): File {
        val dir = File(filesDir, "recordings/$sessionId").also { it.mkdirs() }
        return File(dir, "chunk_${index.toString().padStart(4, '0')}.m4a")
    }

    private fun releaseRecorder() {
        try { mediaRecorder?.release() } catch (_: Exception) {}
        mediaRecorder = null
    }
}