package com.kawaii.mangareader.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kawaii.mangareader.domain.model.Chapter
import com.kawaii.mangareader.domain.model.LibraryCategory
import com.kawaii.mangareader.domain.model.Manga
import com.kawaii.mangareader.domain.model.Page
import com.kawaii.mangareader.domain.model.ReaderSettings
import com.kawaii.mangareader.domain.model.VisualFilter
import com.kawaii.mangareader.domain.repository.ChapterRepository
import com.kawaii.mangareader.domain.repository.HistoryRepository
import com.kawaii.mangareader.domain.repository.LibraryRepository
import com.kawaii.mangareader.domain.repository.MangaRepository
import com.kawaii.mangareader.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ReaderUiState(
    val mangaId: String = "",
    val chapterId: String = "",
    val manga: Manga? = null,
    val currentChapter: Chapter? = null,
    val allChapters: List<Chapter> = emptyList(),
    val pages: List<Page> = emptyList(),
    val currentPageIndex: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isHudVisible: Boolean = false,
    val isLocked: Boolean = false,
    val isInLibrary: Boolean = false,
    val isChaptersSheetOpen: Boolean = false,
    val isMangaInfoSheetOpen: Boolean = false,
    val currentFilter: VisualFilter = VisualFilter.NORMAL,
    val zoomScale: Float = 1f,
    val isAutoScrolling: Boolean = false,
    val autoScrollSpeed: Float = 1f, // 1f = slow, 2f = normal, 3f = fast
    val brightnessPercent: Float = 0.5f,
    val isBrightnessHudVisible: Boolean = false,
    val selectedPageForAction: Page? = null
)

class MangaReaderViewModel(
    private val mangaId: String,
    private val initialChapterId: String,
    private val mangaRepository: MangaRepository,
    private val chapterRepository: ChapterRepository,
    private val historyRepository: HistoryRepository,
    private val libraryRepository: LibraryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ReaderUiState(mangaId = mangaId, chapterId = initialChapterId)
    )
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    val readerSettings: StateFlow<ReaderSettings> = settingsRepository.observeReaderSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReaderSettings()
        )

    init {
        loadChapter(initialChapterId)
    }

    fun loadChapter(chapterId: String, startAtBeginning: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                chapterId = chapterId,
                isLoading = true,
                errorMessage = null,
                zoomScale = 1f,
                currentPageIndex = 0,
                isAutoScrolling = false
            )

            val mangaResult = mangaRepository.getMangaDetails(mangaId)
            val chaptersResult = chapterRepository.getChaptersForManga(mangaId)
            val manga = mangaResult.getOrNull()
            val allChapters = chaptersResult.getOrDefault(emptyList())
            val currentChapter = allChapters.find { it.id == chapterId }
            val inLibrary = libraryRepository.isMangaInLibrary(mangaId)

            val settings = readerSettings.value
            val pagesResult = chapterRepository.getChapterPages(chapterId, settings.dataSaverMode)

            if (pagesResult.isSuccess) {
                val pages = pagesResult.getOrNull() ?: emptyList()
                val initialPage = if (startAtBeginning) {
                    0
                } else {
                    val savedPage = (if (chapterId == manga?.lastReadChapterId && (manga?.lastReadPage ?: 0) > 0) {
                        manga?.lastReadPage
                    } else {
                        currentChapter?.lastReadPage
                    }) ?: 0
                    if (savedPage in pages.indices) savedPage else 0
                }

                _uiState.value = _uiState.value.copy(
                    manga = manga,
                    currentChapter = currentChapter,
                    allChapters = allChapters,
                    pages = pages,
                    currentPageIndex = initialPage,
                    currentFilter = settings.visualFilter,
                    isInLibrary = inLibrary,
                    isLoading = false
                )

                recordProgress(initialPage, pages.size)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar las páginas del capítulo. Intenta de nuevo."
                )
            }
        }
    }

    fun toggleFavorite() {
        val manga = _uiState.value.manga ?: return
        viewModelScope.launch {
            if (_uiState.value.isInLibrary) {
                libraryRepository.removeFromLibrary(mangaId)
                _uiState.value = _uiState.value.copy(isInLibrary = false)
            } else {
                libraryRepository.addToLibrary(manga, LibraryCategory.READING)
                _uiState.value = _uiState.value.copy(isInLibrary = true)
            }
        }
    }

    fun openChaptersSheet() {
        _uiState.value = _uiState.value.copy(isChaptersSheetOpen = true)
    }

    fun closeChaptersSheet() {
        _uiState.value = _uiState.value.copy(isChaptersSheetOpen = false)
    }

    fun openMangaInfoSheet() {
        _uiState.value = _uiState.value.copy(isMangaInfoSheetOpen = true)
    }

    fun closeMangaInfoSheet() {
        _uiState.value = _uiState.value.copy(isMangaInfoSheetOpen = false)
    }

    fun jumpToChapter(chapterId: String) {
        closeChaptersSheet()
        loadChapter(chapterId, startAtBeginning = true)
    }

    fun toggleLock() {
        val newLock = !_uiState.value.isLocked
        _uiState.value = _uiState.value.copy(
            isLocked = newLock,
            isHudVisible = if (newLock) false else _uiState.value.isHudVisible
        )
    }

    fun toggleHud() {
        if (_uiState.value.isLocked) return
        _uiState.value = _uiState.value.copy(isHudVisible = !_uiState.value.isHudVisible)
    }

    fun toggleAutoScroll() {
        _uiState.value = _uiState.value.copy(isAutoScrolling = !_uiState.value.isAutoScrolling)
    }

    fun setAutoScrollSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(autoScrollSpeed = speed)
    }

    fun setBrightness(brightness: Float, showHud: Boolean = true) {
        val clamped = brightness.coerceIn(0.05f, 1f)
        _uiState.value = _uiState.value.copy(
            brightnessPercent = clamped,
            isBrightnessHudVisible = showHud
        )
    }

    fun hideBrightnessHud() {
        _uiState.value = _uiState.value.copy(isBrightnessHudVisible = false)
    }

    fun selectPageForAction(page: Page?) {
        _uiState.value = _uiState.value.copy(selectedPageForAction = page)
    }

    fun setVisualFilter(filter: VisualFilter) {
        _uiState.value = _uiState.value.copy(currentFilter = filter)
        viewModelScope.launch {
            settingsRepository.updateReaderSettings(
                readerSettings.value.copy(visualFilter = filter)
            )
        }
    }

    fun setReadingMode(mode: com.kawaii.mangareader.domain.model.ReadingMode) {
        viewModelScope.launch {
            settingsRepository.updateReaderSettings(
                readerSettings.value.copy(readingMode = mode)
            )
        }
    }

    fun onPageChanged(pageIndex: Int) {
        if (pageIndex == _uiState.value.currentPageIndex) return
        val total = _uiState.value.pages.size
        _uiState.value = _uiState.value.copy(currentPageIndex = pageIndex)
        recordProgress(pageIndex, total)
    }

    private fun recordProgress(pageIndex: Int, totalPages: Int) {
        val manga = _uiState.value.manga ?: return
        val chapter = _uiState.value.currentChapter ?: return

        viewModelScope.launch {
            // Update in DB chapter lastReadPage
            chapterRepository.saveReadingProgress(
                mangaId = manga.id,
                chapterId = chapter.id,
                chapterNumber = chapter.chapterNumber,
                chapterTitle = chapter.title,
                pageIndex = pageIndex,
                totalPages = totalPages
            )

            // Update in manga table
            mangaRepository.updateMangaProgress(
                manga = manga,
                chapterId = chapter.id,
                chapterNumber = chapter.chapterNumber,
                pageIndex = pageIndex
            )

            // Update in reading history
            historyRepository.recordHistory(
                mangaId = manga.id,
                mangaTitle = manga.title,
                mangaCoverUrl = manga.coverUrl,
                chapterId = chapter.id,
                chapterNumber = chapter.chapterNumber,
                chapterTitle = chapter.title,
                pageIndex = pageIndex,
                totalPages = totalPages
            )
        }
    }

    fun nextChapter() {
        val sorted = _uiState.value.allChapters.sortedBy { it.normalizedNumber }
        val currentIdx = sorted.indexOfFirst { it.id == _uiState.value.chapterId }
        if (currentIdx != -1 && currentIdx + 1 < sorted.size) {
            val nextChap = sorted[currentIdx + 1]
            loadChapter(nextChap.id, startAtBeginning = true)
        }
    }

    fun previousChapter() {
        val sorted = _uiState.value.allChapters.sortedBy { it.normalizedNumber }
        val currentIdx = sorted.indexOfFirst { it.id == _uiState.value.chapterId }
        if (currentIdx > 0) {
            val prevChap = sorted[currentIdx - 1]
            loadChapter(prevChap.id, startAtBeginning = true)
        }
    }
}
