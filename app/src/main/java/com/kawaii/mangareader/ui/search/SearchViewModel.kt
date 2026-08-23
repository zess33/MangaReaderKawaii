package com.kawaii.mangareader.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kawaii.mangareader.domain.model.Manga
import com.kawaii.mangareader.domain.model.MangaTag
import com.kawaii.mangareader.domain.repository.MangaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val searchResults: List<Manga> = emptyList(),
    val availableTags: List<MangaTag> = MangaTag.POPULAR_GENRES,
    val selectedTagIds: Set<String> = emptySet(),
    val errorMessage: String? = null,
    val currentPage: Int = 1,
    val hasMorePages: Boolean = true,
    val pageOffset: Int = 0
)

class SearchViewModel(
    private val mangaRepository: MangaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadTags()
    }

    private fun loadTags() {
        viewModelScope.launch {
            val result = mangaRepository.getAvailableTags()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    availableTags = result.getOrDefault(MangaTag.POPULAR_GENRES)
                )
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400) // Debounce
            loadPage(1)
        }
    }

    fun toggleTagSelection(tagId: String) {
        val current = _uiState.value.selectedTagIds.toMutableSet()
        if (current.contains(tagId)) {
            current.remove(tagId)
        } else {
            current.add(tagId)
        }
        _uiState.value = _uiState.value.copy(selectedTagIds = current)
        loadPage(1)
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(query = "", selectedTagIds = emptySet(), searchResults = emptyList(), currentPage = 1)
    }

    fun nextPage() {
        val next = _uiState.value.currentPage + 1
        loadPage(next)
    }

    fun previousPage() {
        val prev = (_uiState.value.currentPage - 1).coerceAtLeast(1)
        loadPage(prev)
    }

    fun loadPage(page: Int) {
        val limit = 20
        val offset = (page - 1) * limit
        val query = _uiState.value.query.trim()
        val tags = _uiState.value.selectedTagIds.toList()

        if (query.isEmpty() && tags.isEmpty()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList(), isLoading = false, currentPage = 1)
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, currentPage = page)

        viewModelScope.launch {
            val result = mangaRepository.searchManga(
                query = query,
                tagIds = tags,
                offset = offset,
                limit = limit
            )

            if (result.isSuccess) {
                val list = result.getOrNull() ?: emptyList()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    searchResults = list,
                    hasMorePages = list.size >= 15,
                    pageOffset = offset
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No se encontraron resultados"
                )
            }
        }
    }
}
