package com.twinmind.recorder.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.twinmind.recorder.data.repository.RecordingRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

@HiltWorker
class SummaryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: RecordingRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_SESSION_ID = "session_id"
        private const val MAX_RETRIES = 3

        // 🔑 Use BuildConfig.GEMINI_API_KEY if configured in build.gradle
        private const val GEMINI_API_KEY = "AIzaSyBfupXPQ7UcFhz8E2ScQTYl0hxkc8BxUq8"
        private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com"
        private const val GEMINI_MODEL = "gemini-2.5-flash"

        fun enqueue(context: Context, sessionId: String) {
            val request = OneTimeWorkRequestBuilder<SummaryWorker>()
                .setInputData(workDataOf(KEY_SESSION_ID to sessionId))
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag("summary_$sessionId")
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

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

            val summaryData = requestSummaryFromGemini(transcript)
                ?: return@withContext Result.retry()

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

    /**
     * Sends the full transcript to Gemini and asks for structured JSON output
     * containing title, summary, actionItems, and keyPoints.
     */
    private fun requestSummaryFromGemini(transcript: String): SummaryData? {
        val prompt = """
            You are a meeting assistant. Analyze the following transcript and respond ONLY with a valid JSON object — no markdown, no code fences, no extra text.

            The JSON must have exactly these four fields:
            {
              "title": "A short, descriptive title for this recording (max 8 words)",
              "summary": "A clear 3-5 sentence summary of the main discussion",
              "actionItems": "Bullet points of action items, one per line starting with •. Write 'None' if there are no action items.",
              "keyPoints": "Bullet points of key points discussed, one per line starting with •"
            }

            Transcript:
            $transcript
        """.trimIndent()

        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            })
            // Ask Gemini to respond in JSON
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.3) // lower = more consistent/structured output
            })
        }.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$GEMINI_BASE_URL/v1beta/models/$GEMINI_MODEL:generateContent?key=$GEMINI_API_KEY")
            .post(requestBody)
            .build()

        return httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                android.util.Log.e("SummaryWorker", "Gemini request failed (${response.code}): ${response.body?.string()}")
                return null
            }

            val body = response.body?.string() ?: return null

            try {
                // Extract the text content from Gemini's response envelope
                val text = JSONObject(body)
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()

                // Parse the structured JSON Gemini returned
                val json = JSONObject(text)
                SummaryData(
                    title = json.optString("title", "Recording Summary"),
                    summary = json.optString("summary", ""),
                    actionItems = json.optString("actionItems", "None"),
                    keyPoints = json.optString("keyPoints", "")
                )
            } catch (e: Exception) {
                android.util.Log.e("SummaryWorker", "Failed to parse Gemini response: $body", e)
                null
            }
        }
    }

    data class SummaryData(
        val title: String,
        val summary: String,
        val actionItems: String,
        val keyPoints: String
    )
}