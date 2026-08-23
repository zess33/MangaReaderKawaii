package com.kawaii.mangareader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.kawaii.mangareader.domain.model.AppTheme
import com.kawaii.mangareader.ui.navigation.AppNavigation
import com.kawaii.mangareader.ui.theme.MangaReaderKawaiiTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MangaReaderApp

        setContent {
            val selectedTheme by app.preferencesManager.appThemeFlow.collectAsState(initial = AppTheme.SAKURA_PINK)
            val isDark = isSystemInDarkTheme() || selectedTheme == AppTheme.PASTEL_DARK

            MangaReaderKawaiiTheme(
                appTheme = selectedTheme,
                darkTheme = isDark
            ) {
                val navController = rememberNavController()
                AppNavigation(
                    navController = navController,
                    database = app.database,
                    preferencesManager = app.preferencesManager,
                    downloadManager = app.downloadManager
                )
            }
        }
    }
}
