package com.kawaii.mangareader.domain.repository

import com.kawaii.mangareader.domain.model.ReadingHistory
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getReadingHistory(): Flow<List<ReadingHistory>>
    suspend fun recordHistory(
        mangaId: String,
        mangaTitle: String,
        mangaCoverUrl: String,
        chapterId: String,
        chapterNumber: String,
        chapterTitle: String?,
        pageIndex: Int,
        totalPages: Int
    )
    suspend fun deleteHistoryForManga(mangaId: String)
    suspend fun clearAllHistory()
}
