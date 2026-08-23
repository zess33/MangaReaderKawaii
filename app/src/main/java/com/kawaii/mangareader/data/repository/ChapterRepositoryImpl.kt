package com.kawaii.mangareader.data.repository

import com.kawaii.mangareader.data.local.dao.ChapterDao
import com.kawaii.mangareader.data.local.dao.MangaDao
import com.kawaii.mangareader.data.local.entity.ChapterEntity
import com.kawaii.mangareader.data.remote.MangaDexClient
import com.kawaii.mangareader.data.remote.MangaDexUrlBuilder
import com.kawaii.mangareader.data.remote.api.MangaDexApi
import com.kawaii.mangareader.data.remote.dto.ChapterDataDto
import com.kawaii.mangareader.domain.model.Chapter
import com.kawaii.mangareader.domain.model.Page
import com.kawaii.mangareader.domain.repository.ChapterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

class ChapterRepositoryImpl(
    private val api: MangaDexApi = MangaDexClient.api,
    private val chapterDao: ChapterDao,
    private val mangaDao: MangaDao
) : ChapterRepository {

    override suspend fun getChaptersForManga(
        mangaId: String,
        offset: Int,
        limit: Int
    ): Result<List<Chapter>> = withContext(Dispatchers.IO) {
        try {
            val allFetchedChapters = mutableListOf<ChapterDataDto>()
            var currentOffset = 0
            val batchLimit = 100
            var total = Int.MAX_VALUE

            // Infinite loop pagination: fetch all chapters until total is reached
            while (currentOffset < total) {
                val response = api.getMangaFeed(
                    mangaId = mangaId,
                    translatedLanguages = listOf("es-la", "es"),
                    limit = batchLimit,
                    offset = currentOffset,
                    orderMap = mapOf("order[chapter]" to "asc")
                )

                total = response.total
                if (response.data.isEmpty()) break
                allFetchedChapters.addAll(response.data)
                currentOffset += batchLimit
                if (currentOffset >= total) break
            }

            // Fallback: If no Spanish chapters exist on MangaDex, fetch English/All so it's never empty
            if (allFetchedChapters.isEmpty()) {
                currentOffset = 0
                total = Int.MAX_VALUE
                while (currentOffset < total) {
                    val response = api.getMangaFeed(
                        mangaId = mangaId,
                        translatedLanguages = listOf("en"),
                        limit = batchLimit,
                        offset = currentOffset,
                        orderMap = mapOf("order[chapter]" to "asc")
                    )
                    total = response.total
                    if (response.data.isEmpty()) break
                    allFetchedChapters.addAll(response.data)
                    currentOffset += batchLimit
                    if (currentOffset >= total) break
                }
            }

            val localChapters = chapterDao.getChaptersByMangaId(mangaId).associateBy { it.id }

            val chapters = allFetchedChapters.map { dto ->
                val local = localChapters[dto.id]
                dto.toDomain(
                    mangaId = mangaId,
                    isDownloaded = local?.isDownloaded ?: false,
                    downloadPath = local?.downloadPath,
                    isRead = local?.isRead ?: false,
                    lastReadPage = local?.lastReadPage ?: 0
                )
            }

            // Save basic chapter metadata to local DB
            val entities = chapters.map { ChapterEntity.fromDomain(it) }
            chapterDao.insertOrUpdateChapters(entities)

            Result.success(chapters)
        } catch (e: Exception) {
            val local = chapterDao.getChaptersByMangaId(mangaId)
            if (local.isNotEmpty()) {
                Result.success(local.map { it.toDomain() })
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getChapterPages(
        chapterId: String,
        isDataSaver: Boolean
    ): Result<List<Page>> = withContext(Dispatchers.IO) {
        try {
            val localChapter = chapterDao.getChapterById(chapterId)

            // Check if downloaded offline
            if (localChapter != null && localChapter.isDownloaded && !localChapter.downloadPath.isNullOrBlank()) {
                val dir = File(localChapter.downloadPath)
                if (dir.exists() && dir.isDirectory) {
                    val files = dir.listFiles { file -> file.extension in listOf("jpg", "jpeg", "png", "webp") }
                        ?.sortedBy { it.name }
                    if (!files.isNullOrEmpty()) {
                        val offlinePages = files.mapIndexed { index, file ->
                            Page(
                                index = index,
                                imageUrl = "",
                                localFilePath = file.absolutePath,
                                isDownloaded = true
                            )
                        }
                        return@withContext Result.success(offlinePages)
                    }
                }
            }

            if (chapterId.startsWith("inmanga_")) {
                val mirrorPages = com.kawaii.mangareader.data.remote.extractor.SpanishMirrorExtractor.extractPagesFromMirror(chapterId)
                if (mirrorPages.isNotEmpty()) {
                    return@withContext Result.success(mirrorPages)
                }
            }

            // Fetch online pages from MangaDex@Home
            try {
                val serverResponse = api.getAtHomeServer(chapterId)
                val baseUrl = serverResponse.baseUrl
                val hash = serverResponse.chapter.hash
                val filenames = if (isDataSaver && serverResponse.chapter.dataSaver.isNotEmpty()) {
                    serverResponse.chapter.dataSaver
                } else {
                    serverResponse.chapter.data
                }

                if (filenames.isNotEmpty()) {
                    val pages = filenames.mapIndexed { index, filename ->
                        val pageUrl = MangaDexUrlBuilder.getPageUrl(baseUrl, hash, filename, isDataSaver)
                        Page(
                            index = index,
                            imageUrl = pageUrl,
                            localFilePath = null,
                            isDownloaded = false
                        )
                    }
                    return@withContext Result.success(pages)
                }
            } catch (ignored: Exception) {
                val mirrorPages = com.kawaii.mangareader.data.remote.extractor.SpanishMirrorExtractor.extractPagesFromMirror(chapterId)
                if (mirrorPages.isNotEmpty()) {
                    return@withContext Result.success(mirrorPages)
                }
            }

            Result.failure(Exception("No se pudieron cargar las páginas de este capítulo."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markChapterAsRead(mangaId: String, chapterId: String, isRead: Boolean) = withContext(Dispatchers.IO) {
        chapterDao.updateReadStatus(chapterId, isRead)
    }

    override suspend fun saveReadingProgress(
        mangaId: String,
        chapterId: String,
        chapterNumber: String,
        chapterTitle: String?,
        pageIndex: Int,
        totalPages: Int
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        chapterDao.updateLastReadPage(chapterId, pageIndex)
        mangaDao.updateReadingProgress(mangaId, chapterId, chapterNumber, pageIndex, now)

        if (totalPages > 0 && pageIndex >= totalPages - 1) {
            chapterDao.updateReadStatus(chapterId, true)
        }
    }

    override fun observeChapters(mangaId: String): Flow<List<Chapter>> {
        return chapterDao.observeChaptersByMangaId(mangaId).map { list -> list.map { it.toDomain() } }
    }
}

fun ChapterDataDto.toDomain(
    mangaId: String,
    isDownloaded: Boolean = false,
    downloadPath: String? = null,
    isRead: Boolean = false,
    lastReadPage: Int = 0
): Chapter {
    val scanGroupRel = relationships.find { it.type == "scanlation_group" }
    val scanGroupId = scanGroupRel?.id
    val scanGroup = scanGroupRel?.attributes?.name

    return Chapter(
        id = id,
        mangaId = mangaId,
        chapterNumber = attributes.chapter ?: "0",
        volume = attributes.volume,
        title = attributes.title,
        translatedLanguage = attributes.translatedLanguage,
        scanlationGroupId = scanGroupId,
        scanlationGroup = scanGroup,
        publishAt = attributes.publishAt,
        pagesCount = attributes.pages,
        isDownloaded = isDownloaded,
        downloadPath = downloadPath,
        isRead = isRead,
        lastReadPage = lastReadPage
    )
}
