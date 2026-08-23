package com.kawaii.mangareader.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kawaii.mangareader.domain.model.ReadingHistory

@Entity(tableName = "reading_history")
data class ReadingHistoryEntity(
    @PrimaryKey val mangaId: String,
    val mangaTitle: String,
    val mangaCoverUrl: String,
    val chapterId: String,
    val chapterNumber: String,
    val chapterTitle: String?,
    val pageIndex: Int,
    val totalPages: Int,
    val lastReadTimestamp: Long
) {
    fun toDomain(): ReadingHistory {
        return ReadingHistory(
            mangaId = mangaId,
            mangaTitle = mangaTitle,
            mangaCoverUrl = mangaCoverUrl,
            chapterId = chapterId,
            chapterNumber = chapterNumber,
            chapterTitle = chapterTitle,
            pageIndex = pageIndex,
            totalPages = totalPages,
            lastReadTimestamp = lastReadTimestamp
        )
    }
}
