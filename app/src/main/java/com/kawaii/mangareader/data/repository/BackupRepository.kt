package com.kawaii.mangareader.data.repository

import com.kawaii.mangareader.data.local.AppDatabase
import com.kawaii.mangareader.data.local.entity.ChapterEntity
import com.kawaii.mangareader.data.local.entity.MangaEntity
import com.kawaii.mangareader.data.local.entity.ReadingHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class BackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val appName: String = "MangaReader Kawaii",
    val libraryMangas: List<BackupManga> = emptyList(),
    val history: List<BackupHistory> = emptyList()
)

@Serializable
data class BackupManga(
    val id: String,
    val title: String,
    val coverUrl: String,
    val status: String,
    val author: String,
    val category: String?,
    val originalLanguage: String
)

@Serializable
data class BackupHistory(
    val mangaId: String,
    val mangaTitle: String,
    val mangaCoverUrl: String,
    val chapterId: String,
    val chapterNumber: String,
    val chapterTitle: String?,
    val pageIndex: Int,
    val totalPages: Int,
    val lastReadTimestamp: Long
)

class BackupRepository(
    private val database: AppDatabase
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun exportBackup(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val mangas = database.mangaDao().observeLibraryMangas().first()
            val history = database.historyDao().observeHistory().first()

            val backup = BackupData(
                libraryMangas = mangas.map {
                    BackupManga(
                        id = it.id,
                        title = it.title,
                        coverUrl = it.coverUrl,
                        status = it.status,
                        author = it.author,
                        category = it.libraryCategory,
                        originalLanguage = it.originalLanguage
                    )
                },
                history = history.map {
                    BackupHistory(
                        mangaId = it.mangaId,
                        mangaTitle = it.mangaTitle,
                        mangaCoverUrl = it.mangaCoverUrl,
                        chapterId = it.chapterId,
                        chapterNumber = it.chapterNumber,
                        chapterTitle = it.chapterTitle,
                        pageIndex = it.pageIndex,
                        totalPages = it.totalPages,
                        lastReadTimestamp = it.lastReadTimestamp
                    )
                }
            )

            val jsonString = json.encodeToString(backup)
            Result.success(jsonString)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importBackup(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val backup = json.decodeFromString<BackupData>(jsonString)
            var count = 0

            backup.libraryMangas.forEach { bManga ->
                val entity = MangaEntity(
                    id = bManga.id,
                    title = bManga.title,
                    coverUrl = bManga.coverUrl,
                    status = bManga.status,
                    author = bManga.author,
                    inLibrary = true,
                    libraryCategory = bManga.category,
                    originalLanguage = bManga.originalLanguage
                )
                database.mangaDao().insertOrUpdate(entity)
                count++
            }

            backup.history.forEach { bHistory ->
                val hEntity = ReadingHistoryEntity(
                    mangaId = bHistory.mangaId,
                    mangaTitle = bHistory.mangaTitle,
                    mangaCoverUrl = bHistory.mangaCoverUrl,
                    chapterId = bHistory.chapterId,
                    chapterNumber = bHistory.chapterNumber,
                    chapterTitle = bHistory.chapterTitle,
                    pageIndex = bHistory.pageIndex,
                    totalPages = bHistory.totalPages,
                    lastReadTimestamp = bHistory.lastReadTimestamp
                )
                database.historyDao().insertOrUpdate(hEntity)
            }

            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
