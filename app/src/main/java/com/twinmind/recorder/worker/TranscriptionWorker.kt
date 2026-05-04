package com.twinmind.recorder.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.twinmind.recorder.data.local.entity.SessionStatus
import com.twinmind.recorder.data.repository.RecordingRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

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

        // 🔑 Replace with your key, or use BuildConfig.GEMINI_API_KEY
        private const val GEMINI_API_KEY = "AIzaSyBfupXPQ7UcFhz8E2ScQTYl0hxkc8BxUq8"
        private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com"
        private const val GEMINI_MODEL = "gemini-2.5-flash"

        fun enqueue(context: Context, sessionId: String, chunkId: String) {
            val request = OneTimeWorkRequestBuilder<TranscriptionWorker>()
                .setInputData(
                    workDataOf(
                        KEY_SESSION_ID to sessionId,
                        KEY_CHUNK_ID to chunkId
                    )
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED) // requires internet
                        .build()
                )
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

    // OkHttpClient with generous timeouts for audio upload + generation
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS) // audio upload can be slow
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val sessionId = inputData.getString(KEY_SESSION_ID) ?: return@withContext Result.failure()
        val chunkId = inputData.getString(KEY_CHUNK_ID) ?: return@withContext Result.failure()

        if (runAttemptCount >= MAX_RETRIES) {
            repository.updateError(sessionId, "Transcription failed after $MAX_RETRIES retries")
            return@withContext Result.failure()
        }

        try {
            repository.incrementChunkRetry(chunkId)

            // 1. Load chunk from DB to get the audio file path
            val chunk = repository.getChunkById(chunkId)
                ?: return@withContext Result.failure()

            val audioFile = File(chunk.filePath)
            if (!audioFile.exists()) {
                repository.updateError(sessionId, "Audio file missing: ${chunk.filePath}")
                return@withContext Result.failure()
            }

            // 2. Upload audio to Gemini File API → get file URI
            val fileUri = uploadAudioToGemini(audioFile)
                ?: return@withContext Result.retry() // network/upload issue → retry

            // 3. Send file URI to Gemini for transcription
            val transcript = requestTranscription(fileUri)
                ?: return@withContext Result.retry() // generation issue → retry

            // 4. Save transcript and check if session is fully transcribed
            repository.markChunkTranscribed(chunkId, transcript)

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

    /**
     * Step 1: Upload the .m4a audio file using Gemini's resumable upload protocol.
     * Returns the file URI (e.g. "https://generativelanguage.googleapis.com/v1beta/files/xyz")
     * which is then passed to the generation request.
     */
    private fun uploadAudioToGemini(file: File): String? {
        // --- Initiate the resumable upload session ---
        val initBody = JSONObject()
            .put("file", JSONObject().put("display_name", file.name))
            .toString()
            .toRequestBody("application/json".toMediaType())

        val initRequest = Request.Builder()
            .url("$GEMINI_BASE_URL/upload/v1beta/files?key=$GEMINI_API_KEY")
            .post(initBody)
            .addHeader("X-Goog-Upload-Protocol", "resumable")
            .addHeader("X-Goog-Upload-Command", "start")
            .addHeader("X-Goog-Upload-Header-Content-Length", file.length().toString())
            .addHeader("X-Goog-Upload-Header-Content-Type", "audio/mp4")
            .build()

        val uploadUrl = httpClient.newCall(initRequest).execute().use { response ->
            if (!response.isSuccessful) {
                android.util.Log.e("TranscriptionWorker", "Upload init failed (${response.code}): ${response.body?.string()}")
                return null
            }
            response.header("X-Goog-Upload-URL")
        } ?: run {
            android.util.Log.e("TranscriptionWorker", "No upload URL in response headers")
            return null
        }

        // --- Upload the actual audio bytes ---
        val uploadRequest = Request.Builder()
            .url(uploadUrl)
            .post(file.asRequestBody("audio/mp4".toMediaType()))
            .addHeader("X-Goog-Upload-Command", "upload, finalize")
            .addHeader("X-Goog-Upload-Offset", "0")
            .build()

        return httpClient.newCall(uploadRequest).execute().use { response ->
            if (!response.isSuccessful) {
                android.util.Log.e("TranscriptionWorker", "Upload failed (${response.code}): ${response.body?.string()}")
                return null
            }
            val body = response.body?.string() ?: return null
            try {
                JSONObject(body).getJSONObject("file").getString("uri")
            } catch (e: Exception) {
                android.util.Log.e("TranscriptionWorker", "Failed to parse upload response: $body")
                null
            }
        }
    }

    /**
     * Step 2: Send the uploaded file URI to Gemini and ask for a transcription.
     * Returns the raw transcript text from the model.
     */
    private fun requestTranscription(fileUri: String): String? {
        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        // Audio file reference
                        put(JSONObject().apply {
                            put("file_data", JSONObject().apply {
                                put("mime_type", "audio/mp4")
                                put("file_uri", fileUri)
                            })
                        })
                        // Transcription prompt — keep it focused, SummaryWorker handles summarization
                        put(JSONObject().apply {
                            put("text", "Please transcribe this audio recording accurately. Return only the transcribed text, with no additional commentary or formatting.")
                        })
                    })
                })
            })
        }.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$GEMINI_BASE_URL/v1beta/models/$GEMINI_MODEL:generateContent?key=$GEMINI_API_KEY")
            .post(requestBody)
            .build()

        return httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                android.util.Log.e("TranscriptionWorker", "Generation failed (${response.code}): ${response.body?.string()}")
                return null
            }
            val body = response.body?.string() ?: return null
            try {
                JSONObject(body)
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()
            } catch (e: Exception) {
                android.util.Log.e("TranscriptionWorker", "Failed to parse generation response: $body")
                null
            }
        }
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