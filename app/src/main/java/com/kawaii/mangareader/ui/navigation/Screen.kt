package com.kawaii.mangareader.ui.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    data object Explore : Screen("explore")
    data object Search : Screen("search")
    data object Library : Screen("library")
    data object History : Screen("history")
    data object Settings : Screen("settings")

    data object MangaDetail : Screen("manga_detail/{mangaId}") {
        fun createRoute(mangaId: String): String = "manga_detail/$mangaId"
    }

    data object Reader : Screen("reader/{mangaId}/{chapterId}") {
        fun createRoute(mangaId: String, chapterId: String): String = "reader/$mangaId/$chapterId"
    }

    data object WebReader : Screen("web_reader?title={title}&url={url}") {
        fun createRoute(title: String, url: String): String {
            val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
            val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
            return "web_reader?title=$encodedTitle&url=$encodedUrl"
        }
    }
}
