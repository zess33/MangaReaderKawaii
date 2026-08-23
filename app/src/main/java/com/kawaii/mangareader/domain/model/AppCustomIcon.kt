package com.kawaii.mangareader.domain.model

import com.kawaii.mangareader.R

enum class AppCustomIcon(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val drawableRes: Int,
    val aliasName: String
) {
    DEFAULT(
        id = "default",
        displayName = "🌸 Sakura Pink",
        subtitle = "Icono clásico de la app",
        drawableRes = R.drawable.ic_sakura_icon,
        aliasName = "com.kawaii.mangareader.MainActivity"
    ),
    TOMOE_1(
        id = "tomoe_1",
        displayName = "🪭 Tomoe Elegante",
        subtitle = "Con abanico misterioso",
        drawableRes = R.drawable.ic_tomoe_1,
        aliasName = "com.kawaii.mangareader.MainActivityTomoe1"
    ),
    TOMOE_2(
        id = "tomoe_2",
        displayName = "🦊 Tomoe Zorro",
        subtitle = "Con orejas de zorro y bufanda",
        drawableRes = R.drawable.ic_tomoe_2,
        aliasName = "com.kawaii.mangareader.MainActivityTomoe2"
    ),
    TOMOE_3(
        id = "tomoe_3",
        displayName = "✨ Tomoe Chibi",
        subtitle = "Versión chibi tierna",
        drawableRes = R.drawable.ic_tomoe_3,
        aliasName = "com.kawaii.mangareader.MainActivityTomoe3"
    );

    companion object {
        fun fromId(id: String): AppCustomIcon = entries.find { it.id == id } ?: DEFAULT
    }
}
