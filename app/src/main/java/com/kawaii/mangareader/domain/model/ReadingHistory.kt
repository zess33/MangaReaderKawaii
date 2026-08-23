package com.kawaii.mangareader.domain.model

data class ReadingHistory(
    val mangaId: String,
    val mangaTitle: String,
    val mangaCoverUrl: String,
    val chapterId: String,
    val chapterNumber: String,
    val chapterTitle: String?,
    val pageIndex: Int,
    val totalPages: Int,
    val lastReadTimestamp: Long
) {
    val progressPercent: Float
        get() = if (totalPages > 0) (pageIndex + 1).toFloat() / totalPages.toFloat() else 0f
}
