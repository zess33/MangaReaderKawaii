package com.kawaii.mangareader.data.remote

object MangaDexUrlBuilder {
    private const val UPLOADS_BASE_URL = "https://uploads.mangadex.org"

    /**
     * Construye la URL para la portada de un Manga con resolución optimizada
     */
    fun getCoverUrl(mangaId: String, fileName: String?, quality: CoverQuality = CoverQuality.MEDIUM): String {
        if (fileName.isNullOrBlank()) return ""
        val suffix = when (quality) {
            CoverQuality.ORIGINAL -> ""
            CoverQuality.MEDIUM -> ".512.jpg"
            CoverQuality.SMALL -> ".256.jpg"
        }
        return "$UPLOADS_BASE_URL/covers/$mangaId/$fileName$suffix"
    }

    /**
     * Construye la URL completa de una página de capítulo a partir de MangaDex@Home
     */
    fun getPageUrl(baseUrl: String, hash: String, fileName: String, isDataSaver: Boolean = false): String {
        val mode = if (isDataSaver) "data-saver" else "data"
        return "$baseUrl/$mode/$hash/$fileName"
    }

    enum class CoverQuality {
        ORIGINAL,
        MEDIUM,
        SMALL
    }
}
