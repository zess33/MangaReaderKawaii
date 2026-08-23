package com.kawaii.mangareader.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChapterListResponseDto(
    val result: String = "ok",
    val response: String = "collection",
    val data: List<ChapterDataDto> = emptyList(),
    val limit: Int = 0,
    val offset: Int = 0,
    val total: Int = 0
)

@Serializable
data class ChapterDataDto(
    val id: String,
    val type: String = "chapter",
    val attributes: ChapterAttributesDto,
    val relationships: List<RelationshipDto> = emptyList()
)

@Serializable
data class ChapterAttributesDto(
    val volume: String? = null,
    val chapter: String? = null,
    val title: String? = null,
    val translatedLanguage: String = "es-la",
    val externalUrl: String? = null,
    val publishAt: String? = null,
    val readableAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val pages: Int = 0,
    val version: Int = 1
)

@Serializable
data class AtHomeServerResponseDto(
    val result: String = "ok",
    val baseUrl: String,
    val chapter: AtHomeChapterDto
)

@Serializable
data class AtHomeChapterDto(
    val hash: String,
    val data: List<String> = emptyList(),
    val dataSaver: List<String> = emptyList()
)
