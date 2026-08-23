package com.kawaii.mangareader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kawaii.mangareader.data.local.entity.MangaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(manga: MangaEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfNotExists(mangas: List<MangaEntity>)

    @Update
    suspend fun update(manga: MangaEntity)

    @Query("SELECT * FROM mangas WHERE id = :id LIMIT 1")
    fun observeMangaById(id: String): Flow<MangaEntity?>

    @Query("SELECT * FROM mangas WHERE id = :id LIMIT 1")
    suspend fun getMangaById(id: String): MangaEntity?

    @Query("SELECT * FROM mangas WHERE inLibrary = 1 ORDER BY lastReadTimestamp DESC")
    fun observeLibraryMangas(): Flow<List<MangaEntity>>

    @Query("SELECT * FROM mangas WHERE inLibrary = 1 ORDER BY lastReadTimestamp DESC")
    suspend fun getLibraryMangasSync(): List<MangaEntity>

    @Query("SELECT * FROM mangas WHERE inLibrary = 1 AND libraryCategory = :category ORDER BY lastReadTimestamp DESC")
    fun observeLibraryMangasByCategory(category: String): Flow<List<MangaEntity>>

    @Query("UPDATE mangas SET inLibrary = :inLibrary, libraryCategory = :category WHERE id = :id")
    suspend fun updateLibraryStatus(id: String, inLibrary: Boolean, category: String?)

    @Query("UPDATE mangas SET inLibrary = 0, libraryCategory = NULL WHERE id IN (:ids)")
    suspend fun removeBatchFromLibrary(ids: List<String>)

    @Query("UPDATE mangas SET libraryCategory = :category WHERE id IN (:ids)")
    suspend fun moveBatchToCategory(ids: List<String>, category: String)

    @Query("UPDATE mangas SET lastReadChapterId = :chapterId, lastReadChapterNum = :chapterNum, lastReadPage = :page, lastReadTimestamp = :timestamp WHERE id = :mangaId")
    suspend fun updateReadingProgress(mangaId: String, chapterId: String, chapterNum: String, page: Int, timestamp: Long)

    @Query("SELECT inLibrary FROM mangas WHERE id = :id LIMIT 1")
    suspend fun isMangaInLibrary(id: String): Boolean?
}
