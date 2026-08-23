package com.kawaii.mangareader.domain.repository

import com.kawaii.mangareader.domain.model.Manga
import com.kawaii.mangareader.domain.model.MangaTag
import kotlinx.coroutines.flow.Flow

interface MangaRepository {
    suspend fun getPopularMangas(offset: Int = 0, limit: Int = 20, status: String? = null): Result<List<Manga>>
    suspend fun getMangasByGenre(tagId: String, offset: Int = 0, limit: Int = 20, status: String? = null): Result<List<Manga>>
    suspend fun searchManga(query: String, tagIds: List<String> = emptyList(), status: String? = null, offset: Int = 0, limit: Int = 20): Result<List<Manga>>
    suspend fun getMangaDetails(mangaId: String): Result<Manga>
    suspend fun getRandomManga(): Result<Manga>
    suspend fun getAvailableTags(): Result<List<MangaTag>>
    fun observeManga(mangaId: String): Flow<Manga?>
}
