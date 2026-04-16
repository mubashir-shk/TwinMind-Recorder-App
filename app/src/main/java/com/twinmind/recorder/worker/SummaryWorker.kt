package com.twinmind.recorder.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.twinmind.recorder.data.repository.RecordingRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@HiltWorker
class SummaryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: RecordingRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_SESSION_ID = "session_id"
        private const val MAX_RETRIES = 3

        fun enqueue(context: Context, sessionId: String) {
            val request = OneTimeWorkRequestBuilder<SummaryWorker>()
                .setInputData(workDataOf(KEY_SESSION_ID to sessionId))
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, java.util.concurrent.TimeUnit.SECONDS)
                .addTag("summary_$sessionId")
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val sessionId = inputData.getString(KEY_SESSION_ID) ?: return@withContext Result.failure()

        if (runAttemptCount >= MAX_RETRIES) {
            repository.updateError(sessionId, "Summary generation failed after $MAX_RETRIES retries")
            return@withContext Result.failure()
        }

        try {
            val session = repository.getSessionByIdOnce(sessionId)
                ?: return@withContext Result.failure()

            val transcript = session.transcript
                ?: return@withContext Result.failure()

            delay(2000)

            val summaryData = generateMockSummary(transcript)

            repository.updateSummary(
                id = sessionId,
                summary = summaryData.summary,
                title = summaryData.title,
                actionItems = summaryData.actionItems,
                keyPoints = summaryData.keyPoints
            )

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun generateMockSummary(transcript: String): SummaryData {
        val wordCount = transcript.split(" ").size
        return SummaryData(
            title = "Meeting - ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date())}",
            summary = "This meeting covered quarterly performance results showing 15% revenue growth, product roadmap planning for Q2 with three new feature launches, and a review of outstanding action items. The team discussed improvements to the customer onboarding experience based on recent user feedback. Marketing campaign results were reviewed showing strong performance metrics.",
            actionItems = "• Schedule follow-up meeting for implementation plan review\n• Engineering team to complete remaining tasks from last week\n• UX team to redesign onboarding flow based on user feedback\n• Prepare Q2 launch timeline for next meeting",
            keyPoints = "• Revenue up 15% compared to last quarter\n• Three new features planned for Q2 launch\n• 25% increase in user acquisition from marketing campaign\n• Customer onboarding experience needs improvement\n• Next meeting scheduled for Tuesday"
        )
    }

    data class SummaryData(
        val title: String,
        val summary: String,
        val actionItems: String,
        val keyPoints: String
    )
}
