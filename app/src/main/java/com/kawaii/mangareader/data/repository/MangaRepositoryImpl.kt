package com.kawaii.mangareader.data.repository

import com.kawaii.mangareader.data.local.dao.MangaDao
import com.kawaii.mangareader.data.local.entity.MangaEntity
import com.kawaii.mangareader.data.remote.MangaDexClient
import com.kawaii.mangareader.data.remote.MangaDexUrlBuilder
import com.kawaii.mangareader.data.remote.api.MangaDexApi
import com.kawaii.mangareader.data.remote.dto.MangaDataDto
import com.kawaii.mangareader.domain.model.Manga
import com.kawaii.mangareader.domain.model.MangaTag
import com.kawaii.mangareader.domain.repository.MangaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MangaRepositoryImpl(
    private val api: MangaDexApi = MangaDexClient.api,
    private val mangaDao: MangaDao
) : MangaRepository {

    override suspend fun getPopularMangas(offset: Int, limit: Int, status: String?): Result<List<Manga>> = withContext(Dispatchers.IO) {
        try {
            val statusList = if (!status.isNullOrBlank() && status != "all") listOf(status) else null
            val response = api.searchManga(
                status = statusList,
                translatedLanguages = listOf("es-la", "es"),
                limit = limit,
                offset = offset,
                orderMap = mapOf("order[followedCount]" to "desc")
            )
            val mangas = response.data.map { dto ->
                val local = mangaDao.getMangaById(dto.id)
                dto.toDomain(
                    inLibrary = local?.inLibrary ?: false,
                    libraryCategory = local?.toDomain()?.libraryCategory,
                    lastReadChapterId = local?.lastReadChapterId,
                    lastReadChapterNum = local?.lastReadChapterNum,
                    lastReadPage = local?.lastReadPage ?: 0,
                    lastReadTimestamp = local?.lastReadTimestamp ?: 0L
                )
            }
            Result.success(mangas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMangasByGenre(tagId: String, offset: Int, limit: Int, status: String?): Result<List<Manga>> = withContext(Dispatchers.IO) {
        try {
            val statusList = if (!status.isNullOrBlank() && status != "all") listOf(status) else null
            val response = api.searchManga(
                includedTags = listOf(tagId),
                status = statusList,
                translatedLanguages = listOf("es-la", "es"),
                limit = limit,
                offset = offset,
                orderMap = mapOf("order[rating]" to "desc", "order[followedCount]" to "desc")
            )
            val mangas = response.data.map { dto ->
                val local = mangaDao.getMangaById(dto.id)
                dto.toDomain(
                    inLibrary = local?.inLibrary ?: false,
                    libraryCategory = local?.toDomain()?.libraryCategory,
                    lastReadChapterId = local?.lastReadChapterId,
                    lastReadChapterNum = local?.lastReadChapterNum,
                    lastReadPage = local?.lastReadPage ?: 0,
                    lastReadTimestamp = local?.lastReadTimestamp ?: 0L
                )
            }
            Result.success(mangas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchManga(
        query: String,
        tagIds: List<String>,
        status: String?,
        offset: Int,
        limit: Int
    ): Result<List<Manga>> = withContext(Dispatchers.IO) {
        try {
            val titleParam = if (query.isNotBlank()) query else null
            val tagsParam = if (tagIds.isNotEmpty()) tagIds else null
            val statusList = if (!status.isNullOrBlank() && status != "all") listOf(status) else null
            val response = api.searchManga(
                title = titleParam,
                includedTags = tagsParam,
                status = statusList,
                translatedLanguages = listOf("es-la", "es"),
                limit = limit,
                offset = offset,
                orderMap = if (titleParam != null) mapOf("order[relevance]" to "desc") else mapOf("order[followedCount]" to "desc")
            )
            val mangas = response.data.map { dto ->
                val local = mangaDao.getMangaById(dto.id)
                dto.toDomain(
                    inLibrary = local?.inLibrary ?: false,
                    libraryCategory = local?.toDomain()?.libraryCategory,
                    lastReadChapterId = local?.lastReadChapterId,
                    lastReadChapterNum = local?.lastReadChapterNum,
                    lastReadPage = local?.lastReadPage ?: 0,
                    lastReadTimestamp = local?.lastReadTimestamp ?: 0L
                )
            }

            val mirrorResults = if (titleParam != null && offset == 0) {
                try {
                    com.kawaii.mangareader.data.remote.extractor.SpanishMirrorExtractor.searchSpanishMirror(titleParam)
                } catch (e: Exception) {
                    emptyList()
                }
            } else emptyList()

            val combined = (mangas + mirrorResults).distinctBy { it.title.trim().lowercase() }
            Result.success(combined)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMangaDetails(mangaId: String): Result<Manga> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMangaById(mangaId)
            val local = mangaDao.getMangaById(mangaId)
            val manga = response.data.toDomain(
                inLibrary = local?.inLibrary ?: false,
                libraryCategory = local?.toDomain()?.libraryCategory,
                lastReadChapterId = local?.lastReadChapterId,
                lastReadChapterNum = local?.lastReadChapterNum,
                lastReadPage = local?.lastReadPage ?: 0,
                lastReadTimestamp = local?.lastReadTimestamp ?: 0L
            )
            if (local == null) {
                mangaDao.insertOrUpdate(MangaEntity.fromDomain(manga))
            }
            Result.success(manga)
        } catch (e: Exception) {
            val local = mangaDao.getMangaById(mangaId)
            if (local != null) {
                Result.success(local.toDomain())
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getRandomManga(): Result<Manga> = withContext(Dispatchers.IO) {
        try {
            val response = api.getRandomManga()
            val local = mangaDao.getMangaById(response.data.id)
            val manga = response.data.toDomain(
                inLibrary = local?.inLibrary ?: false,
                libraryCategory = local?.toDomain()?.libraryCategory,
                lastReadChapterId = local?.lastReadChapterId,
                lastReadChapterNum = local?.lastReadChapterNum,
                lastReadPage = local?.lastReadPage ?: 0,
                lastReadTimestamp = local?.lastReadTimestamp ?: 0L
            )
            Result.success(manga)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAvailableTags(): Result<List<MangaTag>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getTags()
            val tags = response.data.mapNotNull { dto ->
                val name = dto.attributes.name["es"] ?: dto.attributes.name["en"] ?: "Tag"
                if (name.contains("loli", ignoreCase = true) || name.contains("shota", ignoreCase = true)) {
                    null
                } else {
                    MangaTag(
                        id = dto.id,
                        name = name,
                        group = dto.attributes.group
                    )
                }
            }
            Result.success(tags)
        } catch (e: Exception) {
            Result.success(MangaTag.POPULAR_GENRES)
        }
    }

    override fun observeManga(mangaId: String): Flow<Manga?> {
        return mangaDao.observeMangaById(mangaId).map { it?.toDomain() }
    }

    override suspend fun updateMangaProgress(
        manga: Manga,
        chapterId: String,
        chapterNumber: String,
        pageIndex: Int
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val local = mangaDao.getMangaById(manga.id)
        val entity = if (local != null) {
            local.copy(
                lastReadChapterId = chapterId,
                lastReadChapterNum = chapterNumber,
                lastReadPage = pageIndex,
                lastReadTimestamp = now
            )
        } else {
            MangaEntity.fromDomain(manga).copy(
                lastReadChapterId = chapterId,
                lastReadChapterNum = chapterNumber,
                lastReadPage = pageIndex,
                lastReadTimestamp = now
            )
        }
        mangaDao.insertOrUpdate(entity)
    }
}

fun MangaDataDto.toDomain(
    inLibrary: Boolean = false,
    libraryCategory: com.kawaii.mangareader.domain.model.LibraryCategory? = null,
    lastReadChapterId: String? = null,
    lastReadChapterNum: String? = null,
    lastReadPage: Int = 0,
    lastReadTimestamp: Long = 0L
): Manga {
    val titleStr = attributes.title["es"]
        ?: attributes.title["es-la"]
        ?: attributes.title["en"]
        ?: attributes.title.values.firstOrNull()
        ?: "Título desconocido"

    val descStr = attributes.description["es"]
        ?: attributes.description["es-la"]
        ?: attributes.description["en"]
        ?: attributes.description.values.firstOrNull()
        ?: ""

    val altList = attributes.altTitles.mapNotNull { it["es"] ?: it["en"] ?: it.values.firstOrNull() }

    val coverFileName = relationships.find { it.type == "cover_art" }?.attributes?.fileName
    val coverUrl = MangaDexUrlBuilder.getCoverUrl(id, coverFileName)

    val authorName = relationships.find { it.type == "author" }?.attributes?.name ?: ""
    val artistName = relationships.find { it.type == "artist" }?.attributes?.name ?: ""

    val tagsList = attributes.tags.mapNotNull {
        val name = it.attributes.name["es"] ?: it.attributes.name["en"] ?: ""
        if (name.contains("loli", ignoreCase = true) || name.contains("shota", ignoreCase = true)) {
            null
        } else {
            MangaTag(
                id = it.id,
                name = name,
                group = it.attributes.group
            )
        }
    }

    return Manga(
        id = id,
        title = titleStr,
        altTitles = altList,
        description = descStr,
        coverUrl = coverUrl,
        status = attributes.status ?: "ongoing",
        author = authorName,
        artist = artistName,
        tags = tagsList,
        contentRating = attributes.contentRating ?: "safe",
        publicationDemographic = attributes.publicationDemographic,
        originalLanguage = attributes.originalLanguage,
        inLibrary = inLibrary,
        libraryCategory = libraryCategory,
        latestChapter = attributes.lastChapter ?: attributes.latestUploadedChapter,
        lastReadChapterId = lastReadChapterId,
        lastReadChapterNum = lastReadChapterNum,
        lastReadPage = lastReadPage,
        lastReadTimestamp = lastReadTimestamp
    )
}
