package com.kawaii.mangareader.domain.model

data class Chapter(
    val id: String,
    val mangaId: String,
    val chapterNumber: String,
    val volume: String? = null,
    val title: String? = null,
    val translatedLanguage: String = "es-la", // "es-la" or "es"
    val scanlationGroupId: String? = null,
    val scanlationGroup: String? = null,
    val publishAt: String? = null,
    val pagesCount: Int = 0,
    val isDownloaded: Boolean = false,
    val downloadPath: String? = null,
    val isRead: Boolean = false,
    val lastReadPage: Int = 0
) {
    val displayTitle: String
        get() {
            val cleanNum = chapterNumber.trim()
            val isNumeric = cleanNum.isNotEmpty() && (cleanNum.toDoubleOrNull() != null || cleanNum.all { it.isDigit() || it == '.' })
            val numLabel = when {
                isNumeric -> "Capítulo $cleanNum"
                cleanNum.equals("oneshot", ignoreCase = true) || cleanNum.isEmpty() || cleanNum.contains("-") -> "Oneshot"
                cleanNum.length <= 8 -> "Capítulo $cleanNum"
                else -> "Capítulo especial"
            }
            return if (!title.isNullOrBlank()) "$numLabel: $title" else numLabel
        }

    val languageFlag: String
        get() = when (translatedLanguage.lowercase()) {
            "es-la" -> "ES-LA"
            "es" -> "ES"
            else -> "ES"
        }

    val normalizedNumber: Double
        get() = chapterNumber.toDoubleOrNull() ?: 0.0
}
