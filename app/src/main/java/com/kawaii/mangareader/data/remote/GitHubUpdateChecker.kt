package com.kawaii.mangareader.data.remote

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AppUpdateInfo(
    val hasUpdate: Boolean = false,
    val latestVersion: String = "",
    val currentVersion: String = "1.0.3",
    val releaseTitle: String = "",
    val releaseNotes: String = "",
    val downloadUrl: String = "",
    val releasePageUrl: String = ""
)

object GitHubUpdateChecker {

    var DEFAULT_REPO: String = "zess33/MangaReaderKawaii"
    const val CURRENT_APP_VERSION: String = "1.0.3"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun checkForUpdates(repo: String = DEFAULT_REPO): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.github.com/repos/$repo/releases/latest"
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "MangaReaderKawaii-App")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: No se encontraron releases o el repositorio es privado"))
            }

            val bodyString = response.body?.string() ?: return@withContext Result.failure(Exception("Respuesta vacía de GitHub"))
            val json = JSONObject(bodyString)

            val rawTagName = json.optString("tag_name", "").trim()
            val releaseTitle = json.optString("name", "Nueva versión disponible")
            val releaseNotes = json.optString("body", "Mejoras de rendimiento y corrección de errores.")
            val releasePageUrl = json.optString("html_url", "")

            var downloadUrl = ""
            val assets = json.optJSONArray("assets")
            if (assets != null && assets.length() > 0) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val assetName = asset.optString("name", "")
                    if (assetName.endsWith(".apk", ignoreCase = true)) {
                        downloadUrl = asset.optString("browser_download_url", "")
                        break
                    }
                }
            }

            if (downloadUrl.isBlank()) {
                downloadUrl = releasePageUrl
            }

            val hasUpdate = isNewerVersion(rawTagName, CURRENT_APP_VERSION)

            Result.success(
                AppUpdateInfo(
                    hasUpdate = hasUpdate,
                    latestVersion = extractVersionString(rawTagName),
                    currentVersion = CURRENT_APP_VERSION,
                    releaseTitle = releaseTitle,
                    releaseNotes = releaseNotes,
                    downloadUrl = downloadUrl,
                    releasePageUrl = releasePageUrl
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractVersionString(tag: String): String {
        val regex = Regex("""(\d+(\.\d+)*)""")
        val match = regex.find(tag)
        return match?.value ?: tag
    }

    private fun isNewerVersion(latestTag: String, current: String): Boolean {
        val latestNumbers = extractVersionNumbers(latestTag)
        val currentNumbers = extractVersionNumbers(current)

        if (latestNumbers.isEmpty()) return false

        val maxLen = maxOf(latestNumbers.size, currentNumbers.size)
        for (i in 0 until maxLen) {
            val l = latestNumbers.getOrElse(i) { 0 }
            val c = currentNumbers.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    private fun extractVersionNumbers(str: String): List<Int> {
        val regex = Regex("""(\d+(\.\d+)*)""")
        val match = regex.find(str)?.value ?: return emptyList()
        return match.split(".").mapNotNull { it.toIntOrNull() }
    }

    fun openDownloadUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
