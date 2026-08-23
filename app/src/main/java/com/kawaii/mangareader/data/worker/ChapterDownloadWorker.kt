package com.kawaii.mangareader.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.kawaii.mangareader.data.local.AppDatabase
import com.kawaii.mangareader.data.remote.MangaDexClient
import com.kawaii.mangareader.data.remote.MangaDexUrlBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class ChapterDownloadWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_MANGA_ID = "manga_id"
        const val KEY_CHAPTER_ID = "chapter_id"
        const val KEY_CHAPTER_NUM = "chapter_num"
        const val KEY_MANGA_TITLE = "manga_title"
        const val CHANNEL_ID = "manga_downloads_channel"
        const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val mangaId = inputData.getString(KEY_MANGA_ID) ?: return@withContext Result.failure()
        val chapterId = inputData.getString(KEY_CHAPTER_ID) ?: return@withContext Result.failure()
        val chapterNum = inputData.getString(KEY_CHAPTER_NUM) ?: "0"
        val mangaTitle = inputData.getString(KEY_MANGA_TITLE) ?: "Manga"

        createNotificationChannel()
        showNotification(mangaTitle, chapterNum, 0, 100)

        val database = AppDatabase.getDatabase(context)
        val chapterDao = database.chapterDao()

        try {
            // 1. Fetch server pages from MangaDex
            val serverResponse = MangaDexClient.api.getAtHomeServer(chapterId)
            val baseUrl = serverResponse.baseUrl
            val hash = serverResponse.chapter.hash
            val filenames = serverResponse.chapter.data

            if (filenames.isEmpty()) {
                return@withContext Result.failure()
            }

            // 2. Prepare target directory
            val baseDownloadsDir = File(context.filesDir, "downloads")
            val chapterDir = File(baseDownloadsDir, "$mangaId/$chapterId")
            if (!chapterDir.exists()) {
                chapterDir.mkdirs()
            }

            val total = filenames.size
            val client = MangaDexClient.httpClient

            // 3. Download each page file
            filenames.forEachIndexed { index, filename ->
                val pageUrl = MangaDexUrlBuilder.getPageUrl(baseUrl, hash, filename, isDataSaver = false)
                val ext = filename.substringAfterLast('.', "jpg")
                val pageFile = File(chapterDir, "page_${String.format("%03d", index + 1)}.$ext")

                if (!pageFile.exists() || pageFile.length() == 0L) {
                    val request = Request.Builder().url(pageUrl).build()
                    val response = client.newCall(request).execute()
                    if (!response.isSuccessful) {
                        response.close()
                        throw Exception("Failed to download page $index: HTTP ${response.code}")
                    }

                    response.body?.byteStream()?.use { input ->
                        FileOutputStream(pageFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                val progress = ((index + 1) * 100) / total
                try {
                    setProgress(workDataOf("progress" to progress))
                    showNotification(mangaTitle, chapterNum, index + 1, total)
                } catch (e: Exception) {
                    // Ignore
                }
            }

            // 4. Update chapter record in database
            chapterDao.updateDownloadStatus(chapterId, isDownloaded = true, downloadPath = chapterDir.absolutePath)

            showCompletedNotification(mangaTitle, chapterNum)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Descargas de Manga",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Progreso de descarga de capítulos"
                }
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                manager?.createNotificationChannel(channel)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun showNotification(title: String, chapter: String, current: Int, total: Int) {
        try {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Descargando $title")
                .setContentText("Capítulo $chapter ($current/$total páginas)")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setProgress(total, current, false)
                .setOngoing(true)
                .build()
            manager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun showCompletedNotification(title: String, chapter: String) {
        try {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Descarga completada ✨")
                .setContentText("$title - Capítulo $chapter listo para leer offline")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setAutoCancel(true)
                .build()
            manager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            // Ignore
        }
    }
}
