package com.kawaii.mangareader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kawaii.mangareader.data.local.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateChapters(chapters: List<ChapterEntity>)

    @Query("SELECT * FROM chapters WHERE mangaId = :mangaId ORDER BY CAST(chapterNumber AS REAL) ASC")
    fun observeChaptersByMangaId(mangaId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE mangaId = :mangaId ORDER BY CAST(chapterNumber AS REAL) ASC")
    suspend fun getChaptersByMangaId(mangaId: String): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE id = :chapterId LIMIT 1")
    suspend fun getChapterById(chapterId: String): ChapterEntity?

    @Query("UPDATE chapters SET isRead = :isRead WHERE id = :chapterId")
    suspend fun updateReadStatus(chapterId: String, isRead: Boolean)

    @Query("UPDATE chapters SET isDownloaded = :isDownloaded, downloadPath = :downloadPath WHERE id = :chapterId")
    suspend fun updateDownloadStatus(chapterId: String, isDownloaded: Boolean, downloadPath: String?)

    @Query("UPDATE chapters SET lastReadPage = :page WHERE id = :chapterId")
    suspend fun updateLastReadPage(chapterId: String, page: Int)

    @Query("SELECT * FROM chapters WHERE isDownloaded = 1")
    suspend fun getAllDownloadedChapters(): List<ChapterEntity>
}
