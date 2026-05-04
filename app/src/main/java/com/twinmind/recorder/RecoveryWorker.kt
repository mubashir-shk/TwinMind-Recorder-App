package com.twinmind.recorder


import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.twinmind.recorder.data.local.entity.SessionStatus
import com.twinmind.recorder.data.repository.RecordingRepository
import com.twinmind.recorder.worker.SummaryWorker
import com.twinmind.recorder.worker.TranscriptionWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Case 5 — Process death recovery.
 *
 * When the process is killed mid-recording, the session stays in RECORDING
 * status in Room. This worker:
 *   1. Finds all sessions stuck in RECORDING status.
 *   2. Transitions them to TRANSCRIBING.
 *   3. Enqueues TranscriptionWorker for any chunks that were saved but not
 *      yet transcribed (isTranscribed = false).
 *
 * The last in-progress chunk (the one being recorded when the process died)
 * may be partially written. Android's MediaRecorder flushes the MPEG-4 header
 * on stop(), but since stop() never got called, that file is likely corrupt.
 * We skip files under 1 KB (corrupt/empty) and only re-queue valid chunks.
 *
 * Enqueued by BootReceiver on boot and by Application.onCreate() on restart.
 */
@HiltWorker
class RecoveryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: RecordingRepository
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "recovery"

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<RecoveryWorker>()
                .addTag(TAG)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            // Use KEEP so if the app restarts repeatedly we don't stack workers
            WorkManager.getInstance(context).enqueueUniqueWork(
                TAG,
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Find all sessions that were interrupted mid-recording
            val stuckSessions = repository.getSessionsByStatus(SessionStatus.RECORDING)

            if (stuckSessions.isEmpty()) return@withContext Result.success()

            for (session in stuckSessions) {
                // Move session to TRANSCRIBING so the UI reflects the correct state
                repository.updateSessionStatus(session.id, SessionStatus.TRANSCRIBING)

                // Find chunks that were saved to disk but not yet transcribed
                val untranscribed = repository.getUntranscribedChunks(session.id)

                for (chunk in untranscribed) {
                    val file = java.io.File(chunk.filePath)

                    // Skip the last (likely corrupt) chunk if it's under 1 KB
                    if (!file.exists() || file.length() < 1_024L) {
                        continue
                    }

                    // Re-enqueue transcription for valid saved chunks
                    TranscriptionWorker.enqueue(applicationContext, session.id, chunk.id)
                }

                // If all chunks were already transcribed (rare), jump straight to summary
                val allChunks     = repository.getChunksForSession(session.id)
                val stillPending  = repository.getUntranscribedChunks(session.id)
                if (stillPending.isEmpty() && allChunks.isNotEmpty()) {
                    val fullTranscript = allChunks
                        .sortedBy { it.chunkIndex }
                        .mapNotNull { it.transcript }
                        .joinToString(" ")
                    if (fullTranscript.isNotBlank()) {
                        repository.updateTranscript(session.id, fullTranscript)
                        SummaryWorker.enqueue(applicationContext, session.id)
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}