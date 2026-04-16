package com.twinmind.recorder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twinmind.recorder.data.local.entity.SessionEntity
import com.twinmind.recorder.data.local.entity.SessionStatus
import com.twinmind.recorder.data.repository.RecordingRepository
import com.twinmind.recorder.worker.SummaryWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val repository: RecordingRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _sessionId = MutableStateFlow<String?>(null)

    val session: StateFlow<SessionEntity?> = _sessionId
        .filterNotNull()
        .flatMapLatest { repository.getSessionById(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isLoading: StateFlow<Boolean> = session
        .map { it?.status == SessionStatus.SUMMARIZING || it?.status == SessionStatus.TRANSCRIBING }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val hasError: StateFlow<Boolean> = session
        .map { it?.status == SessionStatus.ERROR }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val errorMessage: StateFlow<String> = session
        .map { it?.errorMessage ?: "An unknown error occurred" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun loadSession(sessionId: String) {
        _sessionId.value = sessionId
    }

    fun retrySummary() {
        val sessionId = _sessionId.value ?: return
        viewModelScope.launch {
            repository.updateSessionStatus(sessionId, SessionStatus.SUMMARIZING)
            SummaryWorker.enqueue(context, sessionId)
        }
    }
}
