package com.kawaii.mangareader.data.local.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kawaii.mangareader.domain.model.AppTheme
import com.kawaii.mangareader.domain.model.ReaderSettings
import com.kawaii.mangareader.domain.model.ReadingMode
import com.kawaii.mangareader.domain.model.VisualFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kawaii_preferences")

class UserPreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val APP_THEME = stringPreferencesKey("app_theme")
        val DEDICATION_MESSAGE = stringPreferencesKey("dedication_message")
        val READING_MODE = stringPreferencesKey("reading_mode")
        val VISUAL_FILTER = stringPreferencesKey("visual_filter")
        val VOLUME_KEY_NAV = booleanPreferencesKey("volume_key_nav")
        val DATA_SAVER = booleanPreferencesKey("data_saver")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val ZOOM_SENSITIVITY = floatPreferencesKey("zoom_sensitivity")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val ALLOW_BL_YAOI = booleanPreferencesKey("allow_bl_yaoi")
        val APP_CUSTOM_ICON = stringPreferencesKey("app_custom_icon")
    }

    val appCustomIconFlow: Flow<com.kawaii.mangareader.domain.model.AppCustomIcon> = context.dataStore.data.map { preferences ->
        val iconId = preferences[PreferencesKeys.APP_CUSTOM_ICON] ?: com.kawaii.mangareader.domain.model.AppCustomIcon.DEFAULT.id
        com.kawaii.mangareader.domain.model.AppCustomIcon.fromId(iconId)
    }

    suspend fun setAppCustomIcon(icon: com.kawaii.mangareader.domain.model.AppCustomIcon) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_CUSTOM_ICON] = icon.id
        }
    }

    val allowBlYaoiFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ALLOW_BL_YAOI] ?: true
    }

    suspend fun setAllowBlYaoi(allow: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALLOW_BL_YAOI] = allow
        }
    }

    val isFirstLaunchFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_FIRST_LAUNCH] ?: true
    }

    suspend fun setFirstLaunchCompleted() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_LAUNCH] = false
        }
    }

    val appThemeFlow: Flow<AppTheme> = context.dataStore.data.map { preferences ->
        val themeName = preferences[PreferencesKeys.APP_THEME] ?: AppTheme.PASTEL_DARK.name
        AppTheme.fromName(themeName)
    }

    suspend fun setAppTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_THEME] = theme.name
        }
    }

    val dedicationMessageFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEDICATION_MESSAGE] ?: "✨ Hecho con mucho cariño para ti por Uriel Huerta ✨"
    }

    suspend fun setDedicationMessage(message: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEDICATION_MESSAGE] = message
        }
    }

    val readerSettingsFlow: Flow<ReaderSettings> = context.dataStore.data.map { preferences ->
        val modeStr = preferences[PreferencesKeys.READING_MODE] ?: ReadingMode.WEBTOON_VERTICAL.name
        val filterStr = preferences[PreferencesKeys.VISUAL_FILTER] ?: VisualFilter.NORMAL.name
        val volumeNav = preferences[PreferencesKeys.VOLUME_KEY_NAV] ?: true
        val dataSaver = preferences[PreferencesKeys.DATA_SAVER] ?: false
        val keepScreenOn = preferences[PreferencesKeys.KEEP_SCREEN_ON] ?: true
        val zoom = preferences[PreferencesKeys.ZOOM_SENSITIVITY] ?: 2.5f

        ReaderSettings(
            readingMode = try { ReadingMode.valueOf(modeStr) } catch (e: Exception) { ReadingMode.WEBTOON_VERTICAL },
            visualFilter = try { VisualFilter.valueOf(filterStr) } catch (e: Exception) { VisualFilter.NORMAL },
            volumeKeyNavigation = volumeNav,
            dataSaverMode = dataSaver,
            keepScreenOn = keepScreenOn,
            zoomSensitivity = zoom
        )
    }

    suspend fun updateReaderSettings(settings: ReaderSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.READING_MODE] = settings.readingMode.name
            preferences[PreferencesKeys.VISUAL_FILTER] = settings.visualFilter.name
            preferences[PreferencesKeys.VOLUME_KEY_NAV] = settings.volumeKeyNavigation
            preferences[PreferencesKeys.DATA_SAVER] = settings.dataSaverMode
            preferences[PreferencesKeys.KEEP_SCREEN_ON] = settings.keepScreenOn
            preferences[PreferencesKeys.ZOOM_SENSITIVITY] = settings.zoomSensitivity
        }
    }
}
