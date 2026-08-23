package com.kawaii.mangareader.domain.model

enum class AppTheme(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val primaryHex: Long,
    val secondaryHex: Long,
    val backgroundLightHex: Long,
    val backgroundDarkHex: Long
) {
    SAKURA_PINK(
        title = "Sakura Pink",
        subtitle = "Cerezo en flor y dulzura",
        emoji = "🌸",
        primaryHex = 0xFFFF8EAA,
        secondaryHex = 0xFFFFC2D1,
        backgroundLightHex = 0xFFFFF8F9,
        backgroundDarkHex = 0xFF2D1F24
    ),
    PASTEL_LAVENDER(
        title = "Pastel Lavender",
        subtitle = "Lilas soñadores y calma",
        emoji = "💜",
        primaryHex = 0xFFB794F4,
        secondaryHex = 0xFFD6BCFA,
        backgroundLightHex = 0xFFFAF8FF,
        backgroundDarkHex = 0xFF221E2D
    ),
    PEACHY_CREAM(
        title = "Peachy Cream",
        subtitle = "Melocotón cálido y crema",
        emoji = "🍑",
        primaryHex = 0xFFFFB088,
        secondaryHex = 0xFFFFE0D2,
        backgroundLightHex = 0xFFFFFBF8,
        backgroundDarkHex = 0xFF2D231E
    ),
    MINT_GREEN(
        title = "Mint Green",
        subtitle = "Menta fresca y serenidad",
        emoji = "🌿",
        primaryHex = 0xFF76D7C4,
        secondaryHex = 0xFFC4F1E8,
        backgroundLightHex = 0xFFF6FCFA,
        backgroundDarkHex = 0xFF1B2A26
    ),
    PASTEL_DARK(
        title = "Pastel Dark Mode",
        subtitle = "Noche estrellada y confort",
        emoji = "🌙",
        primaryHex = 0xFFE8A2C8,
        secondaryHex = 0xFF9F7AEA,
        backgroundLightHex = 0xFFFFF8F9,
        backgroundDarkHex = 0xFF18151D
    ),
    STARLIGHT_STARS(
        title = "Estrellas Mágicas",
        subtitle = "Destellos dorados y cosmos",
        emoji = "🌟",
        primaryHex = 0xFFFFC107,
        secondaryHex = 0xFFB388FF,
        backgroundLightHex = 0xFFFCFAFF,
        backgroundDarkHex = 0xFF13111C
    ),
    COZY_CAPYBARA(
        title = "Capibara Relax",
        subtitle = "Caramelo cálido y yuzu",
        emoji = "🥔",
        primaryHex = 0xFFD49A6A,
        secondaryHex = 0xFFFFD54F,
        backgroundLightHex = 0xFFFAF6F0,
        backgroundDarkHex = 0xFF241C16
    ),
    STRAWBERRY_COW(
        title = "Vaca Fresa Kawaii",
        subtitle = "Malteada de fresa y ternura",
        emoji = "🐮",
        primaryHex = 0xFFFF85A1,
        secondaryHex = 0xFFFBB1BD,
        backgroundLightHex = 0xFFFFF5F7,
        backgroundDarkHex = 0xFF291B20
    ),
    COW_PRINT(
        title = "Manchas de Vaca",
        subtitle = "Blanco leche y manchas negras",
        emoji = "🐄",
        primaryHex = 0xFF374151,
        secondaryHex = 0xFF9CA3AF,
        backgroundLightHex = 0xFFF9FAFB,
        backgroundDarkHex = 0xFF111827
    ),
    ARCTIC_PENGUIN(
        title = "Pingüino Polar",
        subtitle = "Hielo ártico y azul marino",
        emoji = "🐧",
        primaryHex = 0xFF00B4D8,
        secondaryHex = 0xFF90E0EF,
        backgroundLightHex = 0xFFF0F9FF,
        backgroundDarkHex = 0xFF0B1924
    ),
    BAMBOO_PANDA(
        title = "Panda Tierno",
        subtitle = "Bambú fresco y pelaje suave",
        emoji = "🐼",
        primaryHex = 0xFF2D6A4F,
        secondaryHex = 0xFF74C69D,
        backgroundLightHex = 0xFFF4FAF6,
        backgroundDarkHex = 0xFF121E17
    );

    companion object {
        fun fromName(name: String?): AppTheme {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: SAKURA_PINK
        }
    }
}
