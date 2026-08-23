package com.kawaii.mangareader.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kawaii.mangareader.data.worker.DownloadManager
import com.kawaii.mangareader.domain.model.Chapter
import com.kawaii.mangareader.domain.model.LibraryCategory
import com.kawaii.mangareader.domain.model.Manga
import com.kawaii.mangareader.domain.repository.ChapterRepository
import com.kawaii.mangareader.domain.repository.LibraryRepository
import com.kawaii.mangareader.domain.repository.MangaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MangaDetailUiState(
    val mangaId: String = "",
    val isLoading: Boolean = true,
    val manga: Manga? = null,
    val allChapters: List<Chapter> = emptyList(),
    val filteredChapters: List<Chapter> = emptyList(),
    val availableScans: List<String> = emptyList(),
    val selectedScan: String? = null, // null = "Todos (Desduplicado)"
    val isInLibrary: Boolean = false,
    val currentCategory: LibraryCategory? = null,
    val isCategorySheetOpen: Boolean = false,
    val errorMessage: String? = null,
    val isDownloadingAll: Boolean = false,
    val isSearchingMirrors: Boolean = false,
    val selectedRangeIndex: Int? = null,
    val chapterRanges: List<Pair<Int, Int>> = emptyList(),
    val displayedChapters: List<Chapter> = emptyList()
)

class MangaDetailViewModel(
    private val mangaId: String,
    private val mangaRepository: MangaRepository,
    private val chapterRepository: ChapterRepository,
    private val libraryRepository: LibraryRepository,
    private val downloadManager: DownloadManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MangaDetailUiState(mangaId = mangaId))
    val uiState: StateFlow<MangaDetailUiState> = _uiState.asStateFlow()

    init {
        loadMangaDetails()
        observeLocalManga()
        observeLocalChapters()
    }

    private fun observeLocalManga() {
        viewModelScope.launch {
            mangaRepository.observeManga(mangaId).collect { localManga ->
                if (localManga != null) {
                    _uiState.value = _uiState.value.copy(
                        isInLibrary = localManga.inLibrary,
                        currentCategory = localManga.libraryCategory
                    )
                }
            }
        }
    }

    private fun observeLocalChapters() {
        viewModelScope.launch {
            chapterRepository.observeChapters(mangaId).collect { localList ->
                if (localList.isNotEmpty()) {
                    updateChapterLists(localList, _uiState.value.selectedScan, _uiState.value.selectedRangeIndex)
                }
            }
        }
    }

    fun loadMangaDetails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val detailsResult = mangaRepository.getMangaDetails(mangaId)
            val chaptersResult = chapterRepository.getChaptersForManga(mangaId)

            if (detailsResult.isSuccess) {
                val manga = detailsResult.getOrNull()
                val chapters = chaptersResult.getOrDefault(emptyList())

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    manga = manga,
                    isInLibrary = manga?.inLibrary ?: false,
                    currentCategory = manga?.libraryCategory
                )
                updateChapterLists(chapters, _uiState.value.selectedScan, _uiState.value.selectedRangeIndex)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No se pudieron cargar los detalles del manga"
                )
            }
        }
    }

    fun selectScan(scanName: String?) {
        _uiState.value = _uiState.value.copy(selectedScan = scanName)
        updateChapterLists(_uiState.value.allChapters, scanName, _uiState.value.selectedRangeIndex)
    }

    fun selectRange(rangeIndex: Int?) {
        _uiState.value = _uiState.value.copy(selectedRangeIndex = rangeIndex)
        val filtered = _uiState.value.filteredChapters
        val displayed = if (rangeIndex != null && rangeIndex in _uiState.value.chapterRanges.indices) {
            val (start, end) = _uiState.value.chapterRanges[rangeIndex]
            filtered.subList(start - 1, end.coerceAtMost(filtered.size))
        } else {
            filtered
        }
        _uiState.value = _uiState.value.copy(displayedChapters = displayed)
    }

    private fun updateChapterLists(chapters: List<Chapter>, selectedScan: String?, rangeIndex: Int? = null) {
        val scans = chapters.mapNotNull { it.scanlationGroup?.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        val filtered = if (selectedScan != null) {
            chapters.filter { it.scanlationGroup?.trim() == selectedScan }
        } else {
            // Smart Gap-Filling & Deduplicated mode: for each chapter number, pick the latest / best one
            chapters.groupBy { it.chapterNumber }
                .map { (_, groupChapters) ->
                    groupChapters.maxByOrNull { it.publishAt ?: "" } ?: groupChapters.first()
                }
                .sortedBy { it.normalizedNumber }
        }

        // Build ranges of 25 chapters
        val ranges = mutableListOf<Pair<Int, Int>>()
        if (filtered.size > 25) {
            val chunkSize = 25
            for (i in filtered.indices step chunkSize) {
                val start = i + 1
                val end = (i + chunkSize).coerceAtMost(filtered.size)
                ranges.add(start to end)
            }
        }

        val displayed = if (rangeIndex != null && rangeIndex in ranges.indices) {
            val (start, end) = ranges[rangeIndex]
            filtered.subList(start - 1, end.coerceAtMost(filtered.size))
        } else {
            filtered
        }

        _uiState.value = _uiState.value.copy(
            allChapters = chapters,
            filteredChapters = filtered,
            chapterRanges = ranges,
            displayedChapters = displayed,
            availableScans = scans,
            selectedRangeIndex = rangeIndex
        )
    }

    fun openCategorySheet() {
        _uiState.value = _uiState.value.copy(isCategorySheetOpen = true)
    }

    fun closeCategorySheet() {
        _uiState.value = _uiState.value.copy(isCategorySheetOpen = false)
    }

    fun setLibraryCategory(category: LibraryCategory) {
        val manga = _uiState.value.manga ?: return
        viewModelScope.launch {
            libraryRepository.addToLibrary(manga, category)
            _uiState.value = _uiState.value.copy(
                isInLibrary = true,
                currentCategory = category,
                isCategorySheetOpen = false
            )
        }
    }

    fun removeFromLibrary() {
        viewModelScope.launch {
            libraryRepository.removeFromLibrary(mangaId)
            _uiState.value = _uiState.value.copy(
                isInLibrary = false,
                currentCategory = null,
                isCategorySheetOpen = false
            )
        }
    }

    fun downloadChapter(chapter: Chapter) {
        val manga = _uiState.value.manga ?: return
        downloadManager.enqueueChapterDownload(manga, chapter)
    }

    fun downloadAllChapters() {
        val manga = _uiState.value.manga ?: return
        _uiState.value = _uiState.value.copy(isDownloadingAll = true)
        viewModelScope.launch {
            _uiState.value.filteredChapters.filter { !it.isDownloaded }.forEach { chapter ->
                downloadManager.enqueueChapterDownload(manga, chapter)
            }
            _uiState.value = _uiState.value.copy(isDownloadingAll = false)
        }
    }

    fun searchMirrorChapters() {
        val title = _uiState.value.manga?.title ?: return
        _uiState.value = _uiState.value.copy(isSearchingMirrors = true)
        viewModelScope.launch {
            try {
                val mirrorMangas = com.kawaii.mangareader.data.remote.extractor.SpanishMirrorExtractor.searchSpanishMirror(title)
                val mirrorManga = mirrorMangas.firstOrNull()
                if (mirrorManga != null) {
                    val mirrorChapters = chapterRepository.getChaptersForManga(mirrorManga.id).getOrDefault(emptyList())
                    if (mirrorChapters.isNotEmpty()) {
                        val merged = (_uiState.value.allChapters + mirrorChapters).distinctBy { it.chapterNumber }
                        updateChapterLists(merged, _uiState.value.selectedScan)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.value = _uiState.value.copy(isSearchingMirrors = false)
            }
        }
    }
}
