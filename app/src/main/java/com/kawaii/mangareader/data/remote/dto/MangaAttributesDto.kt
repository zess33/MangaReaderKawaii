package com.kawaii.mangareader.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class MangaAttributesDto(
    val title: Map<String, String> = emptyMap(),
    val altTitles: List<Map<String, String>> = emptyList(),
    val description: Map<String, String> = emptyMap(),
    val isLocked: Boolean = false,
    val originalLanguage: String = "ja",
    val lastVolume: String? = null,
    val lastChapter: String? = null,
    val publicationDemographic: String? = null,
    val status: String? = "ongoing",
    val year: Int? = null,
    val contentRating: String? = "safe",
    val tags: List<TagDataDto> = emptyList(),
    val state: String? = "published",
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val latestUploadedChapter: String? = null
)

@Serializable
data class TagListResponseDto(
    val result: String = "ok",
    val response: String = "collection",
    val data: List<TagDataDto> = emptyList()
)

@Serializable
data class TagDataDto(
    val id: String,
    val type: String = "tag",
    val attributes: TagAttributesDto
)

@Serializable
data class TagAttributesDto(
    val name: Map<String, String> = emptyMap(),
    val description: Map<String, String> = emptyMap(),
    val group: String = "genre",
    val version: Int = 1
)
