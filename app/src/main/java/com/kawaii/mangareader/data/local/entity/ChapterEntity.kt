package com.kawaii.mangareader.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kawaii.mangareader.domain.model.Chapter

@Entity(
    tableName = "chapters",
    indices = [Index(value = ["mangaId"])]
)
data class ChapterEntity(
    @PrimaryKey val id: String,
    val mangaId: String,
    val chapterNumber: String,
    val volume: String? = null,
    val title: String? = null,
    val translatedLanguage: String = "es-la",
    val scanlationGroup: String? = null,
    val publishAt: String? = null,
    val pagesCount: Int = 0,
    val isDownloaded: Boolean = false,
    val downloadPath: String? = null,
    val isRead: Boolean = false,
    val lastReadPage: Int = 0
) {
    fun toDomain(): Chapter {
        return Chapter(
            id = id,
            mangaId = mangaId,
            chapterNumber = chapterNumber,
            volume = volume,
            title = title,
            translatedLanguage = translatedLanguage,
            scanlationGroup = scanlationGroup,
            publishAt = publishAt,
            pagesCount = pagesCount,
            isDownloaded = isDownloaded,
            downloadPath = downloadPath,
            isRead = isRead,
            lastReadPage = lastReadPage
        )
    }

    companion object {
        fun fromDomain(chapter: Chapter): ChapterEntity {
            return ChapterEntity(
                id = chapter.id,
                mangaId = chapter.mangaId,
                chapterNumber = chapter.chapterNumber,
                volume = chapter.volume,
                title = chapter.title,
                translatedLanguage = chapter.translatedLanguage,
                scanlationGroup = chapter.scanlationGroup,
                publishAt = chapter.publishAt,
                pagesCount = chapter.pagesCount,
                isDownloaded = chapter.isDownloaded,
                downloadPath = chapter.downloadPath,
                isRead = chapter.isRead,
                lastReadPage = chapter.lastReadPage
            )
        }
    }
}
