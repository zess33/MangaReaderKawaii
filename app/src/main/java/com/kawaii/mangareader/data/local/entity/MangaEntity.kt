package com.kawaii.mangareader.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kawaii.mangareader.domain.model.LibraryCategory
import com.kawaii.mangareader.domain.model.Manga
import com.kawaii.mangareader.domain.model.MangaTag

@Entity(tableName = "mangas")
data class MangaEntity(
    @PrimaryKey val id: String,
    val title: String,
    val altTitles: String = "", // Comma-separated or JSON
    val description: String = "",
    val coverUrl: String = "",
    val status: String = "ongoing",
    val author: String = "",
    val artist: String = "",
    val tagNames: String = "", // Comma-separated tag names
    val tagIds: String = "", // Comma-separated tag ids
    val contentRating: String = "safe",
    val publicationDemographic: String? = null,
    val originalLanguage: String = "ja",
    val inLibrary: Boolean = false,
    val libraryCategory: String? = null, // "FAVORITES", "READING", etc.
    val totalChaptersCount: Int = 0,
    val lastReadChapterId: String? = null,
    val lastReadChapterNum: String? = null,
    val lastReadPage: Int = 0,
    val lastReadTimestamp: Long = 0L
) {
    fun toDomain(): Manga {
        val tagsList = if (tagIds.isNotBlank() && tagNames.isNotBlank()) {
            val ids = tagIds.split(";")
            val names = tagNames.split(";")
            ids.indices.mapNotNull { i ->
                if (i < names.size) MangaTag(ids[i], names[i]) else null
            }
        } else emptyList()

        return Manga(
            id = id,
            title = title,
            altTitles = if (altTitles.isNotBlank()) altTitles.split(";;;") else emptyList(),
            description = description,
            coverUrl = coverUrl,
            status = status,
            author = author,
            artist = artist,
            tags = tagsList,
            contentRating = contentRating,
            publicationDemographic = publicationDemographic,
            originalLanguage = originalLanguage,
            inLibrary = inLibrary,
            libraryCategory = libraryCategory?.let { LibraryCategory.fromName(it) },
            totalChaptersCount = totalChaptersCount,
            lastReadChapterId = lastReadChapterId,
            lastReadChapterNum = lastReadChapterNum,
            lastReadPage = lastReadPage,
            lastReadTimestamp = lastReadTimestamp
        )
    }

    companion object {
        fun fromDomain(manga: Manga): MangaEntity {
            return MangaEntity(
                id = manga.id,
                title = manga.title,
                altTitles = manga.altTitles.joinToString(";;;"),
                description = manga.description,
                coverUrl = manga.coverUrl,
                status = manga.status,
                author = manga.author,
                artist = manga.artist,
                tagNames = manga.tags.joinToString(";") { it.name },
                tagIds = manga.tags.joinToString(";") { it.id },
                contentRating = manga.contentRating,
                publicationDemographic = manga.publicationDemographic,
                originalLanguage = manga.originalLanguage,
                inLibrary = manga.inLibrary,
                libraryCategory = manga.libraryCategory?.name,
                totalChaptersCount = manga.totalChaptersCount,
                lastReadChapterId = manga.lastReadChapterId,
                lastReadChapterNum = manga.lastReadChapterNum,
                lastReadPage = manga.lastReadPage,
                lastReadTimestamp = manga.lastReadTimestamp
            )
        }
    }
}
