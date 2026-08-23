package com.kawaii.mangareader.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kawaii.mangareader.data.repository.BackupRepository
import com.kawaii.mangareader.domain.model.AppTheme
import com.kawaii.mangareader.domain.model.ReaderSettings
import com.kawaii.mangareader.domain.model.VisualFilter
import com.kawaii.mangareader.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isEditDedicationOpen: Boolean = false,
    val tempDedicationMessage: String = "",
    val isExporting: Boolean = false,
    val isImporting: Boolean = false
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    val currentTheme: StateFlow<AppTheme> = settingsRepository.observeAppTheme()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.SAKURA_PINK
        )

    val dedicationMessage: StateFlow<String> = settingsRepository.observeDedicationMessage()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "✨ Hecho con mucho cariño para ti por Uriel Huerta ✨"
        )

    val readerSettings: StateFlow<ReaderSettings> = settingsRepository.observeReaderSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReaderSettings()
        )

    val allowBlYaoi: StateFlow<Boolean> = settingsRepository.observeAllowBlYaoi()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun setAllowBlYaoi(allow: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAllowBlYaoi(allow)
            _toastMessage.emit(if (allow) "Contenido BL / Yaoi desbloqueado 🔓" else "Contenido BL / Yaoi bloqueado 🛡️")
        }
    }

    val currentAppIcon: StateFlow<com.kawaii.mangareader.domain.model.AppCustomIcon> = settingsRepository.observeAppCustomIcon()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.kawaii.mangareader.domain.model.AppCustomIcon.DEFAULT
        )

    fun selectAppIcon(context: android.content.Context, icon: com.kawaii.mangareader.domain.model.AppCustomIcon) {
        viewModelScope.launch {
            settingsRepository.setAppCustomIcon(icon)
            com.kawaii.mangareader.ui.theme.AppIconManager.setAppIcon(context, icon)
            _toastMessage.emit("¡Icono ${icon.displayName} activado en tu celular! 🦊✨")
        }
    }

    fun selectTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.setAppTheme(theme)
        }
    }

    fun toggleVolumeNav(enabled: Boolean) {
        viewModelScope.launch {
            val current = readerSettings.value
            settingsRepository.updateReaderSettings(current.copy(volumeKeyNavigation = enabled))
        }
    }

    fun toggleDataSaver(enabled: Boolean) {
        viewModelScope.launch {
            val current = readerSettings.value
            settingsRepository.updateReaderSettings(current.copy(dataSaverMode = enabled))
        }
    }

    fun toggleKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch {
            val current = readerSettings.value
            settingsRepository.updateReaderSettings(current.copy(keepScreenOn = enabled))
        }
    }

    fun setDefaultFilter(filter: VisualFilter) {
        viewModelScope.launch {
            val current = readerSettings.value
            settingsRepository.updateReaderSettings(current.copy(visualFilter = filter))
        }
    }

    fun openEditDedication() {
        _uiState.value = _uiState.value.copy(
            isEditDedicationOpen = true,
            tempDedicationMessage = dedicationMessage.value
        )
    }

    fun onTempDedicationChange(newMsg: String) {
        _uiState.value = _uiState.value.copy(tempDedicationMessage = newMsg)
    }

    fun saveDedicationMessage() {
        viewModelScope.launch {
            val msg = _uiState.value.tempDedicationMessage.trim()
            if (msg.isNotEmpty()) {
                settingsRepository.setDedicationMessage(msg)
            }
            _uiState.value = _uiState.value.copy(isEditDedicationOpen = false)
        }
    }

    fun closeEditDedication() {
        _uiState.value = _uiState.value.copy(isEditDedicationOpen = false)
    }

    fun exportBackup(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            val result = backupRepository.exportBackup()
            _uiState.value = _uiState.value.copy(isExporting = false)
            if (result.isSuccess) {
                onResult(result.getOrNull())
                _toastMessage.emit("✨ Copia de seguridad generada con éxito")
            } else {
                onResult(null)
                _toastMessage.emit("Error al generar copia de seguridad")
            }
        }
    }

    fun importBackup(jsonString: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            val result = backupRepository.importBackup(jsonString)
            _uiState.value = _uiState.value.copy(isImporting = false)
            if (result.isSuccess) {
                val count = result.getOrDefault(0)
                _toastMessage.emit("🌸 ¡Restauración completada! ($count mangas recuperados)")
            } else {
                _toastMessage.emit("Error al restaurar: Archivo JSON no válido")
            }
        }
    }

    fun checkLibraryUpdates(context: android.content.Context) {
        viewModelScope.launch {
            _toastMessage.emit("🔔 Comprobando nuevos capítulos en la biblioteca...")
            try {
                val request = androidx.work.OneTimeWorkRequestBuilder<com.kawaii.mangareader.data.worker.MangaUpdateCheckWorker>()
                    .build()
                androidx.work.WorkManager.getInstance(context).enqueue(request)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
