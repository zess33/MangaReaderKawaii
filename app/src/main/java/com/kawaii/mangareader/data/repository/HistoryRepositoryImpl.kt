package com.kawaii.mangareader.data.repository

import com.kawaii.mangareader.data.local.dao.HistoryDao
import com.kawaii.mangareader.data.local.entity.ReadingHistoryEntity
import com.kawaii.mangareader.domain.model.ReadingHistory
import com.kawaii.mangareader.domain.repository.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class HistoryRepositoryImpl(
    private val historyDao: HistoryDao
) : HistoryRepository {

    override fun getReadingHistory(): Flow<List<ReadingHistory>> {
        return historyDao.observeHistory().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun recordHistory(
        mangaId: String,
        mangaTitle: String,
        mangaCoverUrl: String,
        chapterId: String,
        chapterNumber: String,
        chapterTitle: String?,
        pageIndex: Int,
        totalPages: Int
    ) = withContext(Dispatchers.IO) {
        val historyEntity = ReadingHistoryEntity(
            mangaId = mangaId,
            mangaTitle = mangaTitle,
            mangaCoverUrl = mangaCoverUrl,
            chapterId = chapterId,
            chapterNumber = chapterNumber,
            chapterTitle = chapterTitle,
            pageIndex = pageIndex,
            totalPages = totalPages,
            lastReadTimestamp = System.currentTimeMillis()
        )
        historyDao.insertOrUpdate(historyEntity)
    }

    override suspend fun deleteHistoryForManga(mangaId: String) = withContext(Dispatchers.IO) {
        historyDao.deleteByMangaId(mangaId)
    }

    override suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        historyDao.clearAll()
    }
}
