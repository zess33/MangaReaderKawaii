package com.kawaii.mangareader.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kawaii.mangareader.domain.model.Manga
import com.kawaii.mangareader.domain.model.MangaTag
import com.kawaii.mangareader.domain.repository.MangaRepository
import com.kawaii.mangareader.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExploreUiState(
    val isLoading: Boolean = true,
    val featuredMangas: List<Manga> = emptyList(),
    val popularMangas: List<Manga> = emptyList(),
    val selectedGenre: MangaTag? = null,
    val selectedStatus: String? = null, // null (all), "ongoing", "completed"
    val availableGenres: List<MangaTag> = MangaTag.POPULAR_GENRES,
    val allTags: List<MangaTag> = emptyList(),
    val isAllGenresSheetOpen: Boolean = false,
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false,
    val isPickingRandom: Boolean = false,
    val currentPage: Int = 1,
    val hasMorePages: Boolean = true,
    val pageOffset: Int = 0
)

class ExploreViewModel(
    private val mangaRepository: MangaRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private val _randomMangaEvent = MutableSharedFlow<String>()
    val randomMangaEvent: SharedFlow<String> = _randomMangaEvent.asSharedFlow()

    val dedicationMessage: StateFlow<String> = settingsRepository.observeDedicationMessage()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "✨ Hecho con mucho cariño para ti por Uriel Huerta ✨"
        )

    val allowBlYaoi: StateFlow<Boolean> = settingsRepository.observeAllowBlYaoi()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val isFirstLaunch: StateFlow<Boolean> = settingsRepository.observeIsFirstLaunch()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun dismissFirstLaunchDialog() {
        viewModelScope.launch {
            settingsRepository.setFirstLaunchCompleted()
        }
    }

    init {
        loadInitialData()
        observeAllowBlYaoi()
    }

    private fun observeAllowBlYaoi() {
        viewModelScope.launch {
            settingsRepository.observeAllowBlYaoi().collect {
                loadInitialData()
            }
        }
    }

    fun openAllGenresSheet() {
        _uiState.value = _uiState.value.copy(isAllGenresSheetOpen = true)
    }

    fun closeAllGenresSheet() {
        _uiState.value = _uiState.value.copy(isAllGenresSheetOpen = false)
    }

    fun loadInitialData() {
        loadPage(1)
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
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, currentPage = page)

            val tagsResult = mangaRepository.getAvailableTags()
            val allGenres = tagsResult.getOrDefault(MangaTag.POPULAR_GENRES)

            val limit = 20
            val offset = (page - 1) * limit

            val tag = _uiState.value.selectedGenre
            val isSelectingBL = tag?.id == MangaTag.BOYS_LOVE.id

            val result = if (tag != null) {
                mangaRepository.getMangasByGenre(
                    tagId = tag.id,
                    offset = offset,
                    limit = limit,
                    status = _uiState.value.selectedStatus
                )
            } else {
                mangaRepository.getPopularMangas(
                    offset = offset,
                    limit = limit,
                    status = _uiState.value.selectedStatus
                )
            }

            if (result.isSuccess) {
                val isBlAllowed = try { settingsRepository.observeAllowBlYaoi().first() } catch (e: Exception) { true }
                val rawList = result.getOrNull() ?: emptyList()
                val list = if (!isBlAllowed && !isSelectingBL) rawList.filter { !it.isBL } else rawList

                val featured = if (page == 1 && tag == null) list.take(5) else emptyList()
                val regular = if (page == 1 && tag == null) list.drop(5) else list

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    featuredMangas = featured,
                    popularMangas = regular,
                    allTags = allGenres,
                    hasMorePages = list.size >= 15,
                    pageOffset = offset
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No se pudieron cargar los mangas. Comprueba tu conexión."
                )
            }
        }
    }

    fun selectGenre(tag: MangaTag?) {
        if (_uiState.value.selectedGenre == tag) {
            _uiState.value = _uiState.value.copy(selectedGenre = null, isAllGenresSheetOpen = false)
            loadPage(1)
            return
        }

        _uiState.value = _uiState.value.copy(selectedGenre = tag, isAllGenresSheetOpen = false)
        loadPage(1)
    }

    fun selectStatus(status: String?) {
        val newStatus = if (_uiState.value.selectedStatus == status) null else status
        _uiState.value = _uiState.value.copy(selectedStatus = newStatus)
        loadPage(1)
    }

    fun pickRandomManga(onSelect: (String) -> Unit) {
        val currentPool = (_uiState.value.featuredMangas + _uiState.value.popularMangas).distinctBy { it.id }
        if (currentPool.isNotEmpty()) {
            val random = currentPool.random()
            onSelect(random.id)
        } else {
            viewModelScope.launch {
                val result = mangaRepository.getPopularMangas(offset = (0..20).random(), limit = 10)
                val manga = result.getOrNull()?.randomOrNull()
                if (manga != null) {
                    onSelect(manga.id)
                }
            }
        }
    }

    fun loadMoreMangas() {
        val currentTag = _uiState.value.selectedGenre
        val offset = _uiState.value.pageOffset
        val status = _uiState.value.selectedStatus

        viewModelScope.launch {
            val result = if (currentTag != null) {
                mangaRepository.getMangasByGenre(currentTag.id, offset = offset, limit = 20, status = status)
            } else {
                mangaRepository.getPopularMangas(offset = offset, limit = 20, status = status)
            }

            if (result.isSuccess) {
                val newList = result.getOrNull() ?: emptyList()
                if (newList.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        popularMangas = _uiState.value.popularMangas + newList,
                        pageOffset = offset + 20
                    )
                }
            }
        }
    }
}
