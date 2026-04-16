package com.twinmind.recorder.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.*
import androidx.core.app.NotificationCompat
import com.twinmind.recorder.MainActivity
import com.twinmind.recorder.R
import com.twinmind.recorder.data.local.entity.AudioChunkEntity
import com.twinmind.recorder.data.local.entity.SessionEntity
import com.twinmind.recorder.data.local.entity.SessionStatus
import com.twinmind.recorder.data.repository.RecordingRepository
import com.twinmind.recorder.worker.TranscriptionWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class RecordingService : Service() {

    @Inject
    lateinit var repository: RecordingRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var mediaRecorder: MediaRecorder? = null
    private var currentSessionId: String? = null
    private var chunkIndex = 0
    private var sessionStartTime = 0L
    private var chunkStartTime = 0L
    private var currentChunkFile: File? = null
    private var timerJob: Job? = null
    private var chunkJob: Job? = null
    private var isRecording = false

    companion object {
        const val CHANNEL_ID = "recording_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_START = "ACTION_START"
        const val EXTRA_SESSION_ID = "session_id"
        const val CHUNK_DURATION_MS = 30_000L

        fun startRecording(context: Context, sessionId: String) {
            val intent = Intent(context, RecordingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SESSION_ID, sessionId)
            }
            context.startForegroundService(intent)
        }

        fun stopRecording(context: Context) {
            val intent = Intent(context, RecordingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: return START_NOT_STICKY
                startForeground(NOTIFICATION_ID, buildNotification("Recording...", 0))
                startRecordingSession(sessionId)
            }
            ACTION_STOP -> {
                stopRecordingSession()
            }
        }
        return START_STICKY
    }

    private fun startRecordingSession(sessionId: String) {
        currentSessionId = sessionId
        sessionStartTime = System.currentTimeMillis()
        chunkIndex = 0
        isRecording = true

        startNewChunk()
        startTimerUpdates()
        scheduleNextChunk()
    }

    private fun startNewChunk() {
        val sessionId = currentSessionId ?: return
        val file = createChunkFile(sessionId, chunkIndex)
        currentChunkFile = file
        chunkStartTime = System.currentTimeMillis()

        try {
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
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
        }
    }

    private fun saveCurrentChunk() {
        val sessionId = currentSessionId ?: return
        val file = currentChunkFile ?: return
        if (!file.exists()) return

        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val chunkDuration = System.currentTimeMillis() - chunkStartTime
        val chunk = AudioChunkEntity(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            filePath = file.absolutePath,
            chunkIndex = chunkIndex,
            createdAt = chunkStartTime,
            durationMs = chunkDuration
        )

        serviceScope.launch {
            repository.saveChunk(chunk)
            TranscriptionWorker.enqueue(applicationContext, sessionId, chunk.id)
        }

        chunkIndex++
    }

    private fun scheduleNextChunk() {
        chunkJob?.cancel()
        chunkJob = serviceScope.launch {
            while (isRecording) {
                delay(CHUNK_DURATION_MS)
                if (isRecording) {
                    saveCurrentChunk()
                    startNewChunk()
                }
            }
        }
    }

    private fun startTimerUpdates() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isRecording) {
                val elapsed = System.currentTimeMillis() - sessionStartTime
                currentSessionId?.let { id ->
                    repository.updateSessionDuration(id, elapsed)
                }
                val minutes = (elapsed / 1000 / 60).toString().padStart(2, '0')
                val seconds = (elapsed / 1000 % 60).toString().padStart(2, '0')
                updateNotification("Recording... $minutes:$seconds")
                delay(1000)
            }
        }
    }

    private fun stopRecordingSession() {
        isRecording = false
        timerJob?.cancel()
        chunkJob?.cancel()

        saveCurrentChunk()

        val sessionId = currentSessionId
        if (sessionId != null) {
            serviceScope.launch {
                val elapsed = System.currentTimeMillis() - sessionStartTime
                repository.updateSessionDuration(sessionId, elapsed)
                repository.updateSessionStatus(sessionId, SessionStatus.TRANSCRIBING)
            }
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createChunkFile(sessionId: String, index: Int): File {
        val dir = File(filesDir, "recordings/$sessionId")
        dir.mkdirs()
        return File(dir, "chunk_${index.toString().padStart(4, '0')}.m4a")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recording",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Audio recording in progress"
                setSound(null, null)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(status: String, elapsed: Long): Notification {
        val stopIntent = Intent(this, RecordingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = Intent(this, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TwinMind")
            .setContentText(status)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(status: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(status, 0))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
