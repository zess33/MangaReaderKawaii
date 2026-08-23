package com.kawaii.mangareader.domain.model

data class Manga(
    val id: String,
    val title: String,
    val altTitles: List<String> = emptyList(),
    val description: String = "",
    val coverUrl: String = "",
    val status: String = "ongoing", // "ongoing", "completed"
    val author: String = "",
    val artist: String = "",
    val tags: List<MangaTag> = emptyList(),
    val contentRating: String = "safe",
    val publicationDemographic: String? = null,
    val originalLanguage: String = "ja",
    val inLibrary: Boolean = false,
    val libraryCategory: LibraryCategory? = null,
    val totalChaptersCount: Int = 0,
    val latestChapter: String? = null,
    val lastReadChapterId: String? = null,
    val lastReadChapterNum: String? = null,
    val lastReadPage: Int = 0,
    val lastReadTimestamp: Long = 0L
) {
    val originBadge: String
        get() = when (originalLanguage.lowercase()) {
            "ko" -> "Manhwa"
            "zh", "zh-hk" -> "Manhua"
            "ja" -> "Manga"
            else -> "Webtoon"
        }

    val latestChapterBadge: String?
        get() {
            val cap = latestChapter?.trim() ?: return null
            if (cap.isEmpty() || cap.contains("-") || (cap.length > 8 && cap.toDoubleOrNull() == null)) {
                return null
            }
            return if (cap.startsWith("Cap", ignoreCase = true)) cap else "Cap. $cap"
        }

    val isBLorRomance: Boolean
        get() = tags.any {
            it.name.contains("Boys' Love", ignoreCase = true) ||
            it.name.contains("Yaoi", ignoreCase = true) ||
            it.name.contains("Romance", ignoreCase = true) ||
            it.name.contains("Shounen Ai", ignoreCase = true)
        }

    val isBL: Boolean
        get() = tags.any {
            it.name.contains("Boys' Love", ignoreCase = true) ||
            it.name.contains("Yaoi", ignoreCase = true) ||
            it.name.contains("Shounen Ai", ignoreCase = true) ||
            it.name.contains("Shounen-Ai", ignoreCase = true)
        } || title.contains("Yaoi", ignoreCase = true)

    val isCompleted: Boolean
        get() = status.equals("completed", ignoreCase = true)
}
