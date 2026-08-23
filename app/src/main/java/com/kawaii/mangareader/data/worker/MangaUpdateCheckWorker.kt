package com.kawaii.mangareader.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kawaii.mangareader.data.local.AppDatabase
import com.kawaii.mangareader.data.remote.MangaDexClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class MangaUpdateCheckWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "manga_periodic_update_check"
        const val CHANNEL_ID = "manga_updates_channel"
        const val NOTIFICATION_BASE_ID = 2000

        fun schedulePeriodicCheck(context: Context) {
            try {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val workRequest = PeriodicWorkRequestBuilder<MangaUpdateCheckWorker>(
                    6, TimeUnit.HOURS,
                    30, TimeUnit.MINUTES
                )
                    .setConstraints(constraints)
                    .build()

                val workManager = WorkManager.getInstance(context)
                workManager.enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getDatabase(context)
            val mangaDao = database.mangaDao()
            val chapterDao = database.chapterDao()

            val libraryMangas = mangaDao.getLibraryMangasSync()
            if (libraryMangas.isEmpty()) {
                return@withContext Result.success()
            }

            createNotificationChannel()

            val api = MangaDexClient.api
            var newChaptersFound = 0

            for (manga in libraryMangas) {
                try {
                    val localChapters = chapterDao.getChaptersByMangaId(manga.id)
                    val localLatestNum = localChapters.dmapNotNull { it.chapterNumber.toDoubleOrNull() }.maxOrNull() ?: 0.0

                    val feedResponse = api.getMangaFeed(
                        mangaId = manga.id,
                        translatedLanguages = listOf("es-la", "es"),
                        limit = 10,
                        offset = 0,
                        orderMap = mapOf("order[chapter]" to "desc")
                    )

                    val remoteLatest = feedResponse.data.firstOrNull()
                    val remoteChapterNum = remoteLatest?.attributes?.chapter?.toDoubleOrNull() ?: 0.0

                    if (remoteChapterNum > localLatestNum && localLatestNum > 0.0) {
                        newChaptersFound++
                        val chapterTitle = remoteLatest?.attributes?.title ?: "Capítulo ${remoteLatest?.attributes?.chapter ?: ""}"
                        showUpdateNotification(
                            notificationId = NOTIFICATION_BASE_ID + (manga.id.hashCode() % 1000),
                            mangaTitle = manga.title,
                            chapterInfo = chapterTitle
                        )
                    }
                } catch (e: Exception) {
                    // Continue checking other mangas
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Actualizaciones de Manga",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notificaciones cuando tus mangas favoritos tienen nuevos capítulos"
                }
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                manager?.createNotificationChannel(channel)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun showUpdateNotification(notificationId: Int, mangaTitle: String, chapterInfo: String) {
        try {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("🌸 ¡Nuevo capítulo disponible!")
                .setContentText("$mangaTitle - $chapterInfo")
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setAutoCancel(true)
                .build()
            manager.notify(notificationId, notification)
        } catch (e: Exception) {
            // Ignore
        }
    }
}
