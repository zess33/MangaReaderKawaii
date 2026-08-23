package com.kawaii.mangareader.domain.repository

import com.kawaii.mangareader.domain.model.Chapter
import com.kawaii.mangareader.domain.model.Page
import kotlinx.coroutines.flow.Flow

interface ChapterRepository {
    suspend fun getChaptersForManga(mangaId: String, offset: Int = 0, limit: Int = 100): Result<List<Chapter>>
    suspend fun getChapterPages(chapterId: String, isDataSaver: Boolean = false): Result<List<Page>>
    suspend fun markChapterAsRead(mangaId: String, chapterId: String, isRead: Boolean)
    suspend fun saveReadingProgress(mangaId: String, chapterId: String, chapterNumber: String, chapterTitle: String?, pageIndex: Int, totalPages: Int)
    fun observeChapters(mangaId: String): Flow<List<Chapter>>
}
