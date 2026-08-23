package com.kawaii.mangareader.domain.model

data class Page(
    val index: Int,
    val imageUrl: String,
    val localFilePath: String? = null,
    val isDownloaded: Boolean = false
) {
    val displayUrl: String
        get() = localFilePath?.let { "file://$it" } ?: imageUrl
}
