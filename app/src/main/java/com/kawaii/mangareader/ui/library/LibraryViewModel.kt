package com.kawaii.mangareader.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kawaii.mangareader.domain.model.LibraryCategory
import com.kawaii.mangareader.domain.model.Manga
import com.kawaii.mangareader.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LibraryUiState(
    val selectedCategory: LibraryCategory = LibraryCategory.FAVORITES,
    val mangas: List<Manga> = emptyList(),
    val isMultiSelectMode: Boolean = false,
    val selectedMangaIds: Set<String> = emptySet(),
    val isLoading: Boolean = false
)

class LibraryViewModel(
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        observeCategory(LibraryCategory.FAVORITES)
    }

    fun selectCategory(category: LibraryCategory) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            isMultiSelectMode = false,
            selectedMangaIds = emptySet()
        )
        observeCategory(category)
    }

    private fun observeCategory(category: LibraryCategory) {
        viewModelScope.launch {
            libraryRepository.getMangasByCategory(category).collect { list ->
                _uiState.value = _uiState.value.copy(mangas = list)
            }
        }
    }

    fun toggleMultiSelectMode() {
        val newMode = !_uiState.value.isMultiSelectMode
        _uiState.value = _uiState.value.copy(
            isMultiSelectMode = newMode,
            selectedMangaIds = emptySet()
        )
    }

    fun toggleMangaSelection(mangaId: String) {
        val current = _uiState.value.selectedMangaIds.toMutableSet()
        if (current.contains(mangaId)) {
            current.remove(mangaId)
        } else {
            current.add(mangaId)
        }
        _uiState.value = _uiState.value.copy(selectedMangaIds = current)
    }

    fun selectAll() {
        val allIds = _uiState.value.mangas.map { it.id }.toSet()
        _uiState.value = _uiState.value.copy(selectedMangaIds = allIds)
    }

    fun deleteSelectedBatch() {
        val ids = _uiState.value.selectedMangaIds.toList()
        viewModelScope.launch {
            libraryRepository.removeBatchFromLibrary(ids)
            _uiState.value = _uiState.value.copy(
                isMultiSelectMode = false,
                selectedMangaIds = emptySet()
            )
        }
    }

    fun moveSelectedBatchTo(targetCategory: LibraryCategory) {
        val ids = _uiState.value.selectedMangaIds.toList()
        viewModelScope.launch {
            libraryRepository.moveBatchToCategory(ids, targetCategory)
            _uiState.value = _uiState.value.copy(
                isMultiSelectMode = false,
                selectedMangaIds = emptySet()
            )
        }
    }
}
