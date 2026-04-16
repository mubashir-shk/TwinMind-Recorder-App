package com.twinmind.recorder.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.twinmind.recorder.data.local.entity.SessionStatus
import com.twinmind.recorder.data.repository.RecordingRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

@HiltWorker
class TranscriptionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: RecordingRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_SESSION_ID = "session_id"
        const val KEY_CHUNK_ID = "chunk_id"
        private const val MAX_RETRIES = 3

        fun enqueue(context: Context, sessionId: String, chunkId: String) {
            val request = OneTimeWorkRequestBuilder<TranscriptionWorker>()
                .setInputData(
                    workDataOf(
                        KEY_SESSION_ID to sessionId,
                        KEY_CHUNK_ID to chunkId
                    )
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, java.util.concurrent.TimeUnit.SECONDS)
                .setConstraints(Constraints.Builder().build())
                .addTag("transcription_$sessionId")
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }

        fun enqueueSessionFinalization(context: Context, sessionId: String) {
            val request = OneTimeWorkRequestBuilder<SessionFinalizerWorker>()
                .setInputData(workDataOf(KEY_SESSION_ID to sessionId))
                .addTag("finalize_$sessionId")
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val sessionId = inputData.getString(KEY_SESSION_ID) ?: return@withContext Result.failure()
        val chunkId = inputData.getString(KEY_CHUNK_ID) ?: return@withContext Result.failure()

        if (runAttemptCount >= MAX_RETRIES) {
            repository.updateError(sessionId, "Transcription failed after $MAX_RETRIES retries")
            return@withContext Result.failure()
        }

        try {
            repository.incrementChunkRetry(chunkId)

            delay(1500)
            val mockTranscript = generateMockTranscript(chunkId)

            repository.markChunkTranscribed(chunkId, mockTranscript)

            val allChunks = repository.getChunksForSession(sessionId)
            val untranscribed = repository.getUntranscribedChunks(sessionId)

            if (untranscribed.isEmpty() && allChunks.isNotEmpty()) {
                val fullTranscript = allChunks
                    .sortedBy { it.chunkIndex }
                    .mapNotNull { it.transcript }
                    .joinToString(" ")
                repository.updateTranscript(sessionId, fullTranscript)
                SummaryWorker.enqueue(applicationContext, sessionId)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun generateMockTranscript(chunkId: String): String {
        val samples = listOf(
            "Good morning everyone, let's get started with today's meeting. We have a few important topics to discuss.",
            "The quarterly results show significant growth in our core markets. Revenue is up by 15 percent compared to last quarter.",
            "I wanted to talk about the new product roadmap. We're planning to launch three new features in Q2.",
            "Action items from last week have been mostly completed. The remaining tasks are assigned to the engineering team.",
            "We need to address the customer feedback regarding the onboarding experience. Users are finding it confusing.",
            "The marketing campaign showed strong results with a 25 percent increase in user acquisition.",
            "Let's schedule a follow-up meeting to review the implementation plan in more detail.",
            "Thank you everyone for your contributions. We'll reconvene next Tuesday at the same time."
        )
        return samples[chunkId.hashCode().and(0x7FFFFFFF) % samples.size]
    }
}

@HiltWorker
class SessionFinalizerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: RecordingRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val sessionId = inputData.getString(TranscriptionWorker.KEY_SESSION_ID)
            ?: return@withContext Result.failure()

        try {
            val session = repository.getSessionByIdOnce(sessionId) ?: return@withContext Result.failure()

            if (session.status == SessionStatus.RECORDING) {
                repository.updateSessionStatus(sessionId, SessionStatus.TRANSCRIBING)
                val chunks = repository.getUntranscribedChunks(sessionId)
                chunks.forEach { chunk ->
                    TranscriptionWorker.enqueue(applicationContext, sessionId, chunk.id)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
