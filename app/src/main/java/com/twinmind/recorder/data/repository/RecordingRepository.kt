package com.twinmind.recorder.data.repository

import com.twinmind.recorder.data.local.dao.AudioChunkDao
import com.twinmind.recorder.data.local.dao.SessionDao
import com.twinmind.recorder.data.local.entity.AudioChunkEntity
import com.twinmind.recorder.data.local.entity.SessionEntity
import com.twinmind.recorder.data.local.entity.SessionStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingRepository @Inject constructor(
    private val sessionDao: SessionDao,
    private val audioChunkDao: AudioChunkDao
) {
    fun getAllSessions(): Flow<List<SessionEntity>> = sessionDao.getAllSessions()
    fun getSessionById(id: String): Flow<SessionEntity?> = sessionDao.getSessionById(id)
    suspend fun getSessionByIdOnce(id: String): SessionEntity? = sessionDao.getSessionByIdOnce(id)
    suspend fun createSession(session: SessionEntity) = sessionDao.insertSession(session)
    suspend fun updateSessionStatus(id: String, status: SessionStatus) = sessionDao.updateStatus(id, status)
    suspend fun updateSessionDuration(id: String, durationMs: Long) = sessionDao.updateDuration(id, durationMs)
    suspend fun updateTranscript(id: String, transcript: String) = sessionDao.updateTranscript(id, transcript, SessionStatus.SUMMARIZING)
    suspend fun updateSummary(id: String, summary: String, title: String, actionItems: String, keyPoints: String) =
        sessionDao.updateSummary(id, summary, title, actionItems, keyPoints, SessionStatus.COMPLETED)
    suspend fun updateError(id: String, error: String) = sessionDao.updateError(id, error, SessionStatus.ERROR)
    suspend fun saveChunk(chunk: AudioChunkEntity) = audioChunkDao.insertChunk(chunk)
    suspend fun getChunksForSession(sessionId: String) = audioChunkDao.getChunksForSession(sessionId)
    suspend fun getUntranscribedChunks(sessionId: String) = audioChunkDao.getUntranscribedChunks(sessionId)
    suspend fun markChunkTranscribed(id: String, transcript: String) = audioChunkDao.markTranscribed(id, transcript)
    suspend fun incrementChunkRetry(id: String) = audioChunkDao.incrementRetry(id)
}
