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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val repository: RecordingRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _sessionId = MutableStateFlow<String?>(null)

    val session: StateFlow<SessionEntity?> = _sessionId
        .filterNotNull()
        .flatMapLatest { repository.getSessionById(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isRecording: StateFlow<Boolean> = session
        .map { it?.status == SessionStatus.RECORDING }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val formattedDuration: StateFlow<String> = session
        .map { formatDuration(it?.durationMs ?: 0L) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "00:00")

    val statusText: StateFlow<String> = session
        .map { session ->
            when (session?.status) {
                SessionStatus.RECORDING -> "Recording..."
                SessionStatus.STOPPED -> "Stopped"
                SessionStatus.TRANSCRIBING -> "Transcribing..."
                SessionStatus.SUMMARIZING -> "Generating summary..."
                SessionStatus.COMPLETED -> "Completed"
                SessionStatus.ERROR -> "Error: ${session.errorMessage}"
                null -> ""
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun loadSession(sessionId: String) {
        _sessionId.value = sessionId
    }

    fun stopRecording() {
        RecordingService.stopRecording(context)
        _sessionId.value?.let { id ->
            viewModelScope.launch {
                repository.updateSessionStatus(id, SessionStatus.STOPPED)
            }
        }
    }

    private fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = (totalSeconds / 60).toString().padStart(2, '0')
        val seconds = (totalSeconds % 60).toString().padStart(2, '0')
        return "$minutes:$seconds"
    }
}
