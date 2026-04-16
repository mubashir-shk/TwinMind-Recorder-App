package com.twinmind.recorder.data.local.dao

import androidx.room.*
import com.twinmind.recorder.data.local.entity.AudioChunkEntity
import com.twinmind.recorder.data.local.entity.SessionEntity
import com.twinmind.recorder.data.local.entity.SessionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    fun getSessionById(id: String): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionByIdOnce(id: String): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("UPDATE sessions SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: SessionStatus)

    @Query("UPDATE sessions SET durationMs = :durationMs WHERE id = :id")
    suspend fun updateDuration(id: String, durationMs: Long)

    @Query("UPDATE sessions SET transcript = :transcript, status = :status WHERE id = :id")
    suspend fun updateTranscript(id: String, transcript: String, status: SessionStatus)

    @Query("UPDATE sessions SET summary = :summary, summaryTitle = :title, actionItems = :actionItems, keyPoints = :keyPoints, status = :status WHERE id = :id")
    suspend fun updateSummary(
        id: String,
        summary: String,
        title: String,
        actionItems: String,
        keyPoints: String,
        status: SessionStatus
    )

    @Query("UPDATE sessions SET errorMessage = :error, status = :status WHERE id = :id")
    suspend fun updateError(id: String, error: String, status: SessionStatus)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteSession(id: String)
}

@Dao
interface AudioChunkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChunk(chunk: AudioChunkEntity)

    @Query("SELECT * FROM audio_chunks WHERE sessionId = :sessionId ORDER BY chunkIndex ASC")
    suspend fun getChunksForSession(sessionId: String): List<AudioChunkEntity>

    @Query("SELECT * FROM audio_chunks WHERE sessionId = :sessionId AND isTranscribed = 0 ORDER BY chunkIndex ASC")
    suspend fun getUntranscribedChunks(sessionId: String): List<AudioChunkEntity>

    @Query("UPDATE audio_chunks SET isTranscribed = 1, transcript = :transcript WHERE id = :id")
    suspend fun markTranscribed(id: String, transcript: String)

    @Query("UPDATE audio_chunks SET retryCount = retryCount + 1 WHERE id = :id")
    suspend fun incrementRetry(id: String)

    @Query("DELETE FROM audio_chunks WHERE sessionId = :sessionId")
    suspend fun deleteChunksForSession(sessionId: String)
}
