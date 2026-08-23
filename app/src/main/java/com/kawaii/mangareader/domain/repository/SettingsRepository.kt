package com.kawaii.mangareader.domain.repository

import com.kawaii.mangareader.domain.model.AppCustomIcon
import com.kawaii.mangareader.domain.model.AppTheme
import com.kawaii.mangareader.domain.model.ReaderSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeAppTheme(): Flow<AppTheme>
    suspend fun setAppTheme(theme: AppTheme)

    fun observeAppCustomIcon(): Flow<AppCustomIcon>
    suspend fun setAppCustomIcon(icon: AppCustomIcon)

    fun observeReaderSettings(): Flow<ReaderSettings>
    suspend fun updateReaderSettings(settings: ReaderSettings)

    fun observeDedicationMessage(): Flow<String>
    suspend fun setDedicationMessage(message: String)

    fun observeIsFirstLaunch(): Flow<Boolean>
    suspend fun setFirstLaunchCompleted()

    fun observeAllowBlYaoi(): Flow<Boolean>
    suspend fun setAllowBlYaoi(allow: Boolean)
}
