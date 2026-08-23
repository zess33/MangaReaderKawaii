package com.kawaii.mangareader.data.remote.extractor

import com.kawaii.mangareader.data.remote.MangaDexClient
import com.kawaii.mangareader.domain.model.Chapter
import com.kawaii.mangareader.domain.model.Manga
import com.kawaii.mangareader.domain.model.Page
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

object SpanishMirrorExtractor {

    private val client = MangaDexClient.httpClient

    /**
     * Searches alternative Spanish manga mirrors (InManga / TMO mirrors)
     */
    suspend fun searchSpanishMirror(query: String): List<Manga> = withContext(Dispatchers.IO) {
        val results = mutableListOf<Manga>()
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "https://inmanga.com/search/result?query=$encodedQuery"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 6a) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36")
                .header("Accept", "application/json, text/javascript, */*; q=0.01")
                .header("X-Requested-With", "XMLHttpRequest")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()

            // InManga returns JSON array or HTML cards
            if (body.startsWith("{") || body.startsWith("[")) {
                val jsonArray = if (body.startsWith("[")) JSONArray(body) else JSONObject(body).optJSONArray("result") ?: JSONArray()
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.optJSONObject(i) ?: continue
                    val id = item.optString("identification", "")
                    val title = item.optString("name", "")
                    if (id.isNotBlank() && title.isNotBlank()) {
                        results.add(
                            Manga(
                                id = "inmanga_$id",
                                title = title,
                                description = item.optString("description", "Disponible en Servidor Español"),
                                coverUrl = "https://pack-yak.inmanga.com/images/manga/$title/generalThumbnail",
                                status = "completed",
                                contentRating = "safe",
                                originalLanguage = "es",
                                inLibrary = false
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext results
    }

    /**
     * Extracts chapter pages from Spanish mirror servers
     */
    suspend fun extractPagesFromMirror(mirrorChapterId: String): List<Page> = withContext(Dispatchers.IO) {
        val pages = mutableListOf<Page>()
        try {
            val chapterIdentification = mirrorChapterId.removePrefix("inmanga_")
            val url = "https://inmanga.com/chapter/chapterIndexControls?identification=$chapterIdentification"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 6a) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36")
                .header("Accept", "*/*")
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return@withContext emptyList()

            // Regex match image URLs from pages container
            val imgRegex = Regex("data-src=[\"']([^\"']+)[\"']|<img[^>]+src=[\"']([^\"']+)[\"']")
            val matches = imgRegex.findAll(html)
            var index = 0
            for (match in matches) {
                val src = match.groups[1]?.value ?: match.groups[2]?.value
                if (src != null && !src.contains("logo") && !src.contains("banner") && !src.contains("icon")) {
                    val fullUrl = if (src.startsWith("http")) src else "https://pack-yak.inmanga.com$src"
                    pages.add(
                        Page(
                            index = index++,
                            imageUrl = fullUrl,
                            isDownloaded = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext pages
    }
}
