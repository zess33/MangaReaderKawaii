package com.kawaii.mangareader.domain.repository

import com.kawaii.mangareader.domain.model.LibraryCategory
import com.kawaii.mangareader.domain.model.Manga
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    fun getLibraryMangas(): Flow<List<Manga>>
    fun getMangasByCategory(category: LibraryCategory): Flow<List<Manga>>
    suspend fun addToLibrary(manga: Manga, category: LibraryCategory = LibraryCategory.FAVORITES)
    suspend fun updateCategory(mangaId: String, category: LibraryCategory)
    suspend fun removeFromLibrary(mangaId: String)
    suspend fun removeBatchFromLibrary(mangaIds: List<String>)
    suspend fun moveBatchToCategory(mangaIds: List<String>, category: LibraryCategory)
    suspend fun isMangaInLibrary(mangaId: String): Boolean
}
