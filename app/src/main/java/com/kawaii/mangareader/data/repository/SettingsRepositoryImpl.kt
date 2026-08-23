package com.kawaii.mangareader.data.repository

import com.kawaii.mangareader.data.local.preference.UserPreferencesManager
import com.kawaii.mangareader.domain.model.AppCustomIcon
import com.kawaii.mangareader.domain.model.AppTheme
import com.kawaii.mangareader.domain.model.ReaderSettings
import com.kawaii.mangareader.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(
    private val preferencesManager: UserPreferencesManager
) : SettingsRepository {

    override fun observeAppTheme(): Flow<AppTheme> = preferencesManager.appThemeFlow

    override suspend fun setAppTheme(theme: AppTheme) {
        preferencesManager.setAppTheme(theme)
    }

    override fun observeAppCustomIcon(): Flow<AppCustomIcon> = preferencesManager.appCustomIconFlow

    override suspend fun setAppCustomIcon(icon: AppCustomIcon) {
        preferencesManager.setAppCustomIcon(icon)
    }

    override fun observeReaderSettings(): Flow<ReaderSettings> = preferencesManager.readerSettingsFlow

    override suspend fun updateReaderSettings(settings: ReaderSettings) {
        preferencesManager.updateReaderSettings(settings)
    }

    override fun observeDedicationMessage(): Flow<String> = preferencesManager.dedicationMessageFlow

    override suspend fun setDedicationMessage(message: String) {
        preferencesManager.setDedicationMessage(message)
    }

    override fun observeIsFirstLaunch(): Flow<Boolean> = preferencesManager.isFirstLaunchFlow

    override suspend fun setFirstLaunchCompleted() {
        preferencesManager.setFirstLaunchCompleted()
    }

    override fun observeAllowBlYaoi(): Flow<Boolean> = preferencesManager.allowBlYaoiFlow

    override suspend fun setAllowBlYaoi(allow: Boolean) {
        preferencesManager.setAllowBlYaoi(allow)
    }
}
