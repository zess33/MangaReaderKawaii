package com.kawaii.mangareader.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kawaii.mangareader.data.local.dao.ChapterDao
import com.kawaii.mangareader.data.local.dao.HistoryDao
import com.kawaii.mangareader.data.local.dao.MangaDao
import com.kawaii.mangareader.data.local.entity.ChapterEntity
import com.kawaii.mangareader.data.local.entity.MangaEntity
import com.kawaii.mangareader.data.local.entity.ReadingHistoryEntity

@Database(
    entities = [
        MangaEntity::class,
        ChapterEntity::class,
        ReadingHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mangaDao(): MangaDao
    abstract fun chapterDao(): ChapterDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "manga_reader_kawaii.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
