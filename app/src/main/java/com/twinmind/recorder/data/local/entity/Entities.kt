package com.twinmind.recorder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SessionStatus { RECORDING, STOPPED, TRANSCRIBING, SUMMARIZING, COMPLETED, ERROR }

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val durationMs: Long = 0L,
    val status: SessionStatus = SessionStatus.RECORDING,
    val transcript: String? = null,
    val summary: String? = null,
    val summaryTitle: String? = null,
    val actionItems: String? = null,
    val keyPoints: String? = null,
    val errorMessage: String? = null
)

@Entity(tableName = "audio_chunks")
data class AudioChunkEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val filePath: String,
    val chunkIndex: Int,
    val createdAt: Long,
    val durationMs: Long = 30000L,
    val isTranscribed: Boolean = false,
    val transcript: String? = null,
    val retryCount: Int = 0
)
