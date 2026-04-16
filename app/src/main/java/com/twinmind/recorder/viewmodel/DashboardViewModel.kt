package com.twinmind.recorder.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twinmind.recorder.data.local.entity.SessionEntity
import com.twinmind.recorder.data.local.entity.SessionStatus
import com.twinmind.recorder.data.repository.RecordingRepository
import com.twinmind.recorder.service.RecordingService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: RecordingRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val sessions: StateFlow<List<SessionEntity>> = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun startNewSession(): String {
        val sessionId = UUID.randomUUID().toString()
        val title = "Meeting ${java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}"

        viewModelScope.launch {
            val session = SessionEntity(
                id = sessionId,
                title = title,
                createdAt = System.currentTimeMillis(),
                status = SessionStatus.RECORDING
            )
            repository.createSession(session)
            RecordingService.startRecording(context, sessionId)
        }
        return sessionId
    }


    }

