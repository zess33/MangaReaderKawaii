package com.kawaii.mangareader.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class MangaListResponseDto(
    val result: String = "ok",
    val response: String = "collection",
    val data: List<MangaDataDto> = emptyList(),
    val limit: Int = 0,
    val offset: Int = 0,
    val total: Int = 0
)

@Serializable
data class MangaDetailResponseDto(
    val result: String = "ok",
    val response: String = "entity",
    val data: MangaDataDto
)

@Serializable
data class MangaDataDto(
    val id: String,
    val type: String,
    val attributes: MangaAttributesDto,
    val relationships: List<RelationshipDto> = emptyList()
)

@Serializable
data class RelationshipDto(
    val id: String,
    val type: String, // "cover_art", "author", "artist", "scanlation_group"
    val related: String? = null,
    val attributes: RelationshipAttributesDto? = null
)

@Serializable
data class RelationshipAttributesDto(
    val fileName: String? = null, // for cover_art
    val name: String? = null // for author, artist, scanlation_group
)
