package com.twinmind.recorder.data.repository

// data/repository/GeminiRepository.kt

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRepository @Inject constructor() {

    companion object {
        // 🔑 Put your Gemini API key here (or load from BuildConfig)
        private const val API_KEY = "AIzaSyBfupXPQ7UcFhz8E2ScQTYl0hxkc8BxUq8"
        private const val BASE_URL = "https://generativelanguage.googleapis.com"
        private const val MODEL = "gemini-1.5-flash" // free tier model
        private const val TAG = "GeminiRepository"
    }

    private val client = OkHttpClient.Builder().build()

    /**
     * Full pipeline: upload audio → request summary → return text
     */
    suspend fun transcribeAndSummarize(audioFile: File): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                // 1. Upload the audio file to Gemini File API
                val fileUri = uploadAudioFile(audioFile)
                    ?: return@withContext Result.failure(Exception("File upload failed"))

                Log.d(TAG, "Uploaded file URI: $fileUri")

                // 2. Ask Gemini to transcribe + summarize
                val summary = requestSummary(fileUri)
                    ?: return@withContext Result.failure(Exception("Summary generation failed"))

                Result.success(summary)
            } catch (e: Exception) {
                Log.e(TAG, "Gemini pipeline error", e)
                Result.failure(e)
            }
        }

    /**
     * Step 1: Upload audio using Gemini resumable upload API
     * Returns the file URI to use in the generation request
     */
    private fun uploadAudioFile(file: File): String? {
        // Initiate upload
        val initBody = JSONObject()
            .put("file", JSONObject().put("display_name", file.name))
            .toString()
            .toRequestBody("application/json".toMediaType())

        val initRequest = Request.Builder()
            .url("$BASE_URL/upload/v1beta/files?key=$API_KEY")
            .post(initBody)
            .addHeader("X-Goog-Upload-Protocol", "resumable")
            .addHeader("X-Goog-Upload-Command", "start")
            .addHeader("X-Goog-Upload-Header-Content-Length", file.length().toString())
            .addHeader("X-Goog-Upload-Header-Content-Type", "audio/mp4")
            .build()

        val uploadUrl = client.newCall(initRequest).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e(TAG, "Upload init failed: ${response.body?.string()}")
                return null
            }
            response.header("X-Goog-Upload-URL")
        } ?: return null

        // Upload the actual bytes
        val uploadRequest = Request.Builder()
            .url(uploadUrl)
            .post(file.asRequestBody("audio/mp4".toMediaType()))
            .addHeader("X-Goog-Upload-Command", "upload, finalize")
            .addHeader("X-Goog-Upload-Offset", "0")
            .build()

        return client.newCall(uploadRequest).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e(TAG, "Upload failed: ${response.body?.string()}")
                return null
            }
            val body = response.body?.string() ?: return null
            JSONObject(body).getJSONObject("file").getString("uri")
        }
    }

    /**
     * Step 2: Send file URI to Gemini and request transcription + summary
     */
    private fun requestSummary(fileUri: String): String? {
        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        // Audio part
                        put(JSONObject().apply {
                            put("file_data", JSONObject().apply {
                                put("mime_type", "audio/mp4")
                                put("file_uri", fileUri)
                            })
                        })
                        // Prompt part
                        put(JSONObject().apply {
                            put("text", """
                                Please do the following for this audio recording:
                                1. Transcribe the speech accurately.
                                2. Provide a concise summary of the key points discussed.
                                3. List any action items or important decisions mentioned.
                                
                                Format your response as:
                                TRANSCRIPTION:
                                [full transcription here]
                                
                                SUMMARY:
                                [summary here]
                                
                                ACTION ITEMS:
                                [bullet points, or "None" if not applicable]
                            """.trimIndent())
                        })
                    })
                })
            })
        }.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$BASE_URL/v1beta/models/$MODEL:generateContent?key=$API_KEY")
            .post(requestBody)
            .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e(TAG, "Generate failed: ${response.body?.string()}")
                return null
            }
            val body = response.body?.string() ?: return null
            JSONObject(body)
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        }
    }
}