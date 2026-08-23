package com.kawaii.mangareader.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kawaii.mangareader.domain.model.ReadingHistory
import com.kawaii.mangareader.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val historyList: List<ReadingHistory> = emptyList(),
    val isLoading: Boolean = false
)

class HistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        observeHistory()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            historyRepository.getReadingHistory().collect { list ->
                _uiState.value = _uiState.value.copy(historyList = list)
            }
        }
    }

    fun deleteHistoryItem(mangaId: String) {
        viewModelScope.launch {
            historyRepository.deleteHistoryForManga(mangaId)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearAllHistory()
        }
    }
}
