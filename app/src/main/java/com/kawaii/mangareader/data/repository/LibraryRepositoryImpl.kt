package com.kawaii.mangareader.data.repository

import com.kawaii.mangareader.data.local.dao.MangaDao
import com.kawaii.mangareader.data.local.entity.MangaEntity
import com.kawaii.mangareader.domain.model.LibraryCategory
import com.kawaii.mangareader.domain.model.Manga
import com.kawaii.mangareader.domain.repository.LibraryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LibraryRepositoryImpl(
    private val mangaDao: MangaDao
) : LibraryRepository {

    override fun getLibraryMangas(): Flow<List<Manga>> {
        return mangaDao.observeLibraryMangas().map { list -> list.map { it.toDomain() } }
    }

    override fun getMangasByCategory(category: LibraryCategory): Flow<List<Manga>> {
        return mangaDao.observeLibraryMangasByCategory(category.name).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun addToLibrary(manga: Manga, category: LibraryCategory) = withContext(Dispatchers.IO) {
        val entity = MangaEntity.fromDomain(
            manga.copy(inLibrary = true, libraryCategory = category)
        )
        mangaDao.insertOrUpdate(entity)
    }

    override suspend fun updateCategory(mangaId: String, category: LibraryCategory) = withContext(Dispatchers.IO) {
        mangaDao.updateLibraryStatus(mangaId, inLibrary = true, category = category.name)
    }

    override suspend fun removeFromLibrary(mangaId: String) = withContext(Dispatchers.IO) {
        mangaDao.updateLibraryStatus(mangaId, inLibrary = false, category = null)
    }

    override suspend fun removeBatchFromLibrary(mangaIds: List<String>) = withContext(Dispatchers.IO) {
        mangaDao.removeBatchFromLibrary(mangaIds)
    }

    override suspend fun moveBatchToCategory(mangaIds: List<String>, category: LibraryCategory) = withContext(Dispatchers.IO) {
        mangaDao.moveBatchToCategory(mangaIds, category.name)
    }

    override suspend fun isMangaInLibrary(mangaId: String): Boolean = withContext(Dispatchers.IO) {
        mangaDao.isMangaInLibrary(mangaId) == true
    }
}
