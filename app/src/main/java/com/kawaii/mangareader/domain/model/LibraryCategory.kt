package com.kawaii.mangareader.domain.model

enum class LibraryCategory(val displayName: String, val emoji: String) {
    FAVORITES("Favoritos", "🌸"),
    READING("Leyendo", "📖"),
    PLAN_TO_READ("Pendientes", "⏳"),
    COMPLETED("Completados", "✨");

    companion object {
        fun fromName(name: String?): LibraryCategory {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: FAVORITES
        }
    }
}
