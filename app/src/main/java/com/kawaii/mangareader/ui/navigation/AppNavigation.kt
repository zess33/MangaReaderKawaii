package com.kawaii.mangareader.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.kawaii.mangareader.data.local.AppDatabase
import com.kawaii.mangareader.data.local.preference.UserPreferencesManager
import com.kawaii.mangareader.data.repository.BackupRepository
import com.kawaii.mangareader.data.repository.ChapterRepositoryImpl
import com.kawaii.mangareader.data.repository.HistoryRepositoryImpl
import com.kawaii.mangareader.data.repository.LibraryRepositoryImpl
import com.kawaii.mangareader.data.repository.MangaRepositoryImpl
import com.kawaii.mangareader.data.repository.SettingsRepositoryImpl
import com.kawaii.mangareader.data.worker.DownloadManager
import com.kawaii.mangareader.ui.components.KawaiiBottomNavBar
import com.kawaii.mangareader.ui.detail.MangaDetailScreen
import com.kawaii.mangareader.ui.detail.MangaDetailViewModel
import com.kawaii.mangareader.ui.explore.ExploreScreen
import com.kawaii.mangareader.ui.explore.ExploreViewModel
import com.kawaii.mangareader.ui.history.HistoryScreen
import com.kawaii.mangareader.ui.history.HistoryViewModel
import com.kawaii.mangareader.ui.library.LibraryScreen
import com.kawaii.mangareader.ui.library.LibraryViewModel
import com.kawaii.mangareader.ui.reader.MangaReaderScreen
import com.kawaii.mangareader.ui.reader.MangaReaderViewModel
import com.kawaii.mangareader.ui.reader.WebReaderScreen
import com.kawaii.mangareader.ui.search.SearchScreen
import com.kawaii.mangareader.ui.search.SearchViewModel
import com.kawaii.mangareader.ui.settings.SettingsScreen
import com.kawaii.mangareader.ui.settings.SettingsViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation(
    navController: NavHostController,
    database: AppDatabase,
    preferencesManager: UserPreferencesManager,
    downloadManager: DownloadManager,
    modifier: Modifier = Modifier
) {
    val mangaRepository = MangaRepositoryImpl(mangaDao = database.mangaDao())
    val chapterRepository = ChapterRepositoryImpl(chapterDao = database.chapterDao(), mangaDao = database.mangaDao())
    val libraryRepository = LibraryRepositoryImpl(mangaDao = database.mangaDao())
    val historyRepository = HistoryRepositoryImpl(historyDao = database.historyDao())
    val settingsRepository = SettingsRepositoryImpl(preferencesManager = preferencesManager)
    val backupRepository = BackupRepository(database = database)

    val exploreViewModel = remember { ExploreViewModel(mangaRepository, settingsRepository) }
    val searchViewModel = remember { SearchViewModel(mangaRepository) }
    val libraryViewModel = remember { LibraryViewModel(libraryRepository) }
    val historyViewModel = remember { HistoryViewModel(historyRepository) }
    val settingsViewModel = remember { SettingsViewModel(settingsRepository, backupRepository) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom bar when on Detail, Reader or WebReader screens
    val isBottomBarVisible = currentRoute in listOf(
        Screen.Explore.route,
        Screen.Search.route,
        Screen.Library.route,
        Screen.History.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                KawaiiBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { targetRoute ->
                        navController.navigate(targetRoute) {
                            popUpTo(Screen.Explore.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Explore.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isBottomBarVisible) paddingValues.calculateBottomPadding() else 0.dp)
        ) {
            composable(Screen.Explore.route) {
                ExploreScreen(
                    viewModel = exploreViewModel,
                    onMangaClick = { mangaId ->
                        navController.navigate(Screen.MangaDetail.createRoute(mangaId))
                    }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    viewModel = searchViewModel,
                    onMangaClick = { mangaId ->
                        navController.navigate(Screen.MangaDetail.createRoute(mangaId))
                    }
                )
            }

            composable(Screen.Library.route) {
                LibraryScreen(
                    viewModel = libraryViewModel,
                    onMangaClick = { mangaId ->
                        navController.navigate(Screen.MangaDetail.createRoute(mangaId))
                    }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = historyViewModel,
                    onResumeReading = { mangaId, chapterId ->
                        navController.navigate(Screen.Reader.createRoute(mangaId, chapterId))
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }

            composable(
                route = Screen.MangaDetail.route,
                arguments = listOf(navArgument("mangaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val mangaId = backStackEntry.arguments?.getString("mangaId") ?: ""
                val detailViewModel = MangaDetailViewModel(
                    mangaId = mangaId,
                    mangaRepository = mangaRepository,
                    chapterRepository = chapterRepository,
                    libraryRepository = libraryRepository,
                    downloadManager = downloadManager
                )
                MangaDetailScreen(
                    viewModel = detailViewModel,
                    onBackClick = { navController.popBackStack() },
                    onChapterClick = { targetMangaId, chapterId ->
                        navController.navigate(Screen.Reader.createRoute(targetMangaId, chapterId))
                    },
                    onWebReaderClick = { title, url ->
                        navController.navigate(Screen.WebReader.createRoute(title, url))
                    }
                )
            }

            composable(
                route = Screen.Reader.route,
                arguments = listOf(
                    navArgument("mangaId") { type = NavType.StringType },
                    navArgument("chapterId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val mangaId = backStackEntry.arguments?.getString("mangaId") ?: ""
                val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
                val readerViewModel = MangaReaderViewModel(
                    mangaId = mangaId,
                    initialChapterId = chapterId,
                    mangaRepository = mangaRepository,
                    chapterRepository = chapterRepository,
                    historyRepository = historyRepository,
                    libraryRepository = libraryRepository,
                    settingsRepository = settingsRepository
                )
                MangaReaderScreen(
                    viewModel = readerViewModel,
                    onBackClick = { navController.popBackStack() },
                    onMangaInfoClick = { targetMangaId ->
                        navController.navigate(Screen.MangaDetail.createRoute(targetMangaId))
                    }
                )
            }

            composable(
                route = Screen.WebReader.route,
                arguments = listOf(
                    navArgument("title") { type = NavType.StringType; defaultValue = "Lector Web" },
                    navArgument("url") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                val rawTitle = backStackEntry.arguments?.getString("title") ?: "Lector Web"
                val rawUrl = backStackEntry.arguments?.getString("url") ?: ""
                val decodedTitle = try { URLDecoder.decode(rawTitle, StandardCharsets.UTF_8.toString()) } catch (e: Exception) { rawTitle }
                val decodedUrl = try { URLDecoder.decode(rawUrl, StandardCharsets.UTF_8.toString()) } catch (e: Exception) { rawUrl }

                WebReaderScreen(
                    title = decodedTitle,
                    initialUrl = decodedUrl,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
