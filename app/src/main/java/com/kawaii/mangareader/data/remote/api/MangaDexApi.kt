package com.kawaii.mangareader.data.remote.api

import com.kawaii.mangareader.data.remote.dto.AtHomeServerResponseDto
import com.kawaii.mangareader.data.remote.dto.ChapterListResponseDto
import com.kawaii.mangareader.data.remote.dto.MangaDetailResponseDto
import com.kawaii.mangareader.data.remote.dto.MangaListResponseDto
import com.kawaii.mangareader.data.remote.dto.TagListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface MangaDexApi {

    @GET("manga")
    suspend fun searchManga(
        @Query("title") title: String? = null,
        @Query("includedTags[]") includedTags: List<String>? = null,
        @Query("status[]") status: List<String>? = null, // "ongoing", "completed"
        @Query("availableTranslatedLanguage[]") translatedLanguages: List<String> = listOf("es-la", "es"),
        @Query("contentRating[]") contentRatings: List<String> = listOf("safe", "suggestive", "erotica", "pornographic"),
        @Query("includes[]") includes: List<String> = listOf("cover_art", "author", "artist"),
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @QueryMap orderMap: Map<String, String> = mapOf("order[followedCount]" to "desc")
    ): MangaListResponseDto

    @GET("manga/{id}")
    suspend fun getMangaById(
        @Path("id") mangaId: String,
        @Query("includes[]") includes: List<String> = listOf("cover_art", "author", "artist")
    ): MangaDetailResponseDto

    @GET("manga/random")
    suspend fun getRandomManga(
        @Query("includedTags[]") includedTags: List<String>? = null,
        @Query("contentRating[]") contentRatings: List<String> = listOf("safe", "suggestive", "erotica", "pornographic"),
        @Query("includes[]") includes: List<String> = listOf("cover_art", "author", "artist")
    ): MangaDetailResponseDto

    @GET("manga/{id}/feed")
    suspend fun getMangaFeed(
        @Path("id") mangaId: String,
        @Query("translatedLanguage[]") translatedLanguages: List<String> = listOf("es-la", "es"),
        @Query("includes[]") includes: List<String> = listOf("scanlation_group", "user"),
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
        @Query("contentRating[]") contentRatings: List<String> = listOf("safe", "suggestive", "erotica", "pornographic"),
        @QueryMap orderMap: Map<String, String> = mapOf("order[chapter]" to "asc")
    ): ChapterListResponseDto

    @GET("at-home/server/{chapterId}")
    suspend fun getAtHomeServer(
        @Path("chapterId") chapterId: String
    ): AtHomeServerResponseDto

    @GET("manga/tag")
    suspend fun getTags(): TagListResponseDto
}
