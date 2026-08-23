package com.kawaii.mangareader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kawaii.mangareader.data.local.entity.ReadingHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(history: ReadingHistoryEntity)

    @Query("SELECT * FROM reading_history ORDER BY lastReadTimestamp DESC")
    fun observeHistory(): Flow<List<ReadingHistoryEntity>>

    @Query("DELETE FROM reading_history WHERE mangaId = :mangaId")
    suspend fun deleteByMangaId(mangaId: String)

    @Query("DELETE FROM reading_history")
    suspend fun clearAll()
}
