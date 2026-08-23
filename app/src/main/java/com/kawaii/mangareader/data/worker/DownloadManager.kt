package com.kawaii.mangareader.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.kawaii.mangareader.domain.model.Chapter
import com.kawaii.mangareader.domain.model.Manga
import kotlinx.coroutines.flow.Flow
import java.io.File

class DownloadManager(private val context: Context) {
    private val workManager: WorkManager by lazy {
        try {
            WorkManager.getInstance(context)
        } catch (e: Exception) {
            val config = androidx.work.Configuration.Builder().build()
            try {
                WorkManager.initialize(context, config)
            } catch (ignored: Exception) {}
            WorkManager.getInstance(context)
        }
    }

    fun enqueueChapterDownload(manga: Manga, chapter: Chapter) {
        val workData = workDataOf(
            ChapterDownloadWorker.KEY_MANGA_ID to manga.id,
            ChapterDownloadWorker.KEY_CHAPTER_ID to chapter.id,
            ChapterDownloadWorker.KEY_CHAPTER_NUM to chapter.chapterNumber,
            ChapterDownloadWorker.KEY_MANGA_TITLE to manga.title
        )

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val downloadWork = OneTimeWorkRequestBuilder<ChapterDownloadWorker>()
            .setConstraints(constraints)
            .setInputData(workData)
            .addTag("download_${chapter.id}")
            .addTag("manga_${manga.id}")
            .build()

        workManager.enqueueUniqueWork(
            "download_chapter_${chapter.id}",
            ExistingWorkPolicy.KEEP,
            downloadWork
        )
    }

    fun observeDownloadStatus(chapterId: String): Flow<List<WorkInfo>> {
        return workManager.getWorkInfosForUniqueWorkFlow("download_chapter_$chapterId")
    }

    fun deleteDownloadedChapter(mangaId: String, chapterId: String) {
        val baseDownloadsDir = File(context.filesDir, "downloads")
        val chapterDir = File(baseDownloadsDir, "$mangaId/$chapterId")
        if (chapterDir.exists()) {
            chapterDir.deleteRecursively()
        }
    }
}
