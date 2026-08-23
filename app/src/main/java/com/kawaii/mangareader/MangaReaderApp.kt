package com.kawaii.mangareader

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.kawaii.mangareader.data.local.AppDatabase
import com.kawaii.mangareader.data.local.preference.UserPreferencesManager
import com.kawaii.mangareader.data.remote.MangaDexClient
import com.kawaii.mangareader.data.worker.DownloadManager

class MangaReaderApp : Application(), ImageLoaderFactory {

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    val preferencesManager: UserPreferencesManager by lazy {
        UserPreferencesManager(this)
    }

    val downloadManager: DownloadManager by lazy {
        DownloadManager(this)
    }

    override fun onCreate() {
        super.onCreate()
        com.kawaii.mangareader.data.worker.MangaUpdateCheckWorker.schedulePeriodicCheck(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient(MangaDexClient.httpClient)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("manga_image_cache"))
                    .maxSizePercent(0.10)
                    .build()
            }
            .respectCacheHeaders(false)
            .networkCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .build()
    }
}
