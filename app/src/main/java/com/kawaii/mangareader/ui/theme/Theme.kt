package com.kawaii.mangareader.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.kawaii.mangareader.domain.model.AppTheme

private val SakuraPinkLight = lightColorScheme(
    primary = SakuraPinkPrimary,
    onPrimary = Color.White,
    primaryContainer = SakuraPinkTertiary,
    onPrimaryContainer = SakuraPinkTextPrimaryLight,
    secondary = SakuraPinkSecondary,
    onSecondary = SakuraPinkTextPrimaryLight,
    background = SakuraPinkBackgroundLight,
    onBackground = SakuraPinkTextPrimaryLight,
    surface = SakuraPinkSurfaceLight,
    onSurface = SakuraPinkTextPrimaryLight,
    surfaceVariant = SakuraPinkTertiary,
    onSurfaceVariant = SakuraPinkTextSecondaryLight
)

private val SakuraPinkDark = darkColorScheme(
    primary = SakuraPinkPrimary,
    onPrimary = Color.Black,
    primaryContainer = SakuraPinkSurfaceDark,
    onPrimaryContainer = SakuraPinkSecondary,
    secondary = SakuraPinkSecondary,
    onSecondary = Color.Black,
    background = SakuraPinkBackgroundDark,
    onBackground = SakuraPinkTextPrimaryDark,
    surface = SakuraPinkSurfaceDark,
    onSurface = SakuraPinkTextPrimaryDark,
    surfaceVariant = SakuraPinkBackgroundDark,
    onSurfaceVariant = SakuraPinkTextSecondaryDark
)

private val LavenderLight = lightColorScheme(
    primary = LavenderPrimary,
    onPrimary = Color.White,
    primaryContainer = LavenderTertiary,
    onPrimaryContainer = LavenderTextPrimaryLight,
    secondary = LavenderSecondary,
    onSecondary = LavenderTextPrimaryLight,
    background = LavenderBackgroundLight,
    onBackground = LavenderTextPrimaryLight,
    surface = LavenderSurfaceLight,
    onSurface = LavenderTextPrimaryLight,
    surfaceVariant = LavenderTertiary,
    onSurfaceVariant = LavenderTextSecondaryLight
)

private val LavenderDark = darkColorScheme(
    primary = LavenderPrimary,
    onPrimary = Color.Black,
    primaryContainer = LavenderSurfaceDark,
    onPrimaryContainer = LavenderSecondary,
    secondary = LavenderSecondary,
    onSecondary = Color.Black,
    background = LavenderBackgroundDark,
    onBackground = LavenderTextPrimaryDark,
    surface = LavenderSurfaceDark,
    onSurface = LavenderTextPrimaryDark,
    surfaceVariant = LavenderBackgroundDark,
    onSurfaceVariant = LavenderTextSecondaryDark
)

private val PeachLight = lightColorScheme(
    primary = PeachPrimary,
    onPrimary = Color.White,
    primaryContainer = PeachTertiary,
    onPrimaryContainer = PeachTextPrimaryLight,
    secondary = PeachSecondary,
    onSecondary = PeachTextPrimaryLight,
    background = PeachBackgroundLight,
    onBackground = PeachTextPrimaryLight,
    surface = PeachSurfaceLight,
    onSurface = PeachTextPrimaryLight,
    surfaceVariant = PeachTertiary,
    onSurfaceVariant = PeachTextSecondaryLight
)

private val PeachDark = darkColorScheme(
    primary = PeachPrimary,
    onPrimary = Color.Black,
    primaryContainer = PeachSurfaceDark,
    onPrimaryContainer = PeachSecondary,
    secondary = PeachSecondary,
    onSecondary = Color.Black,
    background = PeachBackgroundDark,
    onBackground = PeachTextPrimaryDark,
    surface = PeachSurfaceDark,
    onSurface = PeachTextPrimaryDark,
    surfaceVariant = PeachBackgroundDark,
    onSurfaceVariant = PeachTextSecondaryDark
)

private val MintLight = lightColorScheme(
    primary = MintPrimary,
    onPrimary = Color.White,
    primaryContainer = MintTertiary,
    onPrimaryContainer = MintTextPrimaryLight,
    secondary = MintSecondary,
    onSecondary = MintTextPrimaryLight,
    background = MintBackgroundLight,
    onBackground = MintTextPrimaryLight,
    surface = MintSurfaceLight,
    onSurface = MintTextPrimaryLight,
    surfaceVariant = MintTertiary,
    onSurfaceVariant = MintTextSecondaryLight
)

private val MintDark = darkColorScheme(
    primary = MintPrimary,
    onPrimary = Color.Black,
    primaryContainer = MintSurfaceDark,
    onPrimaryContainer = MintSecondary,
    secondary = MintSecondary,
    onSecondary = Color.Black,
    background = MintBackgroundDark,
    onBackground = MintTextPrimaryDark,
    surface = MintSurfaceDark,
    onSurface = MintTextPrimaryDark,
    surfaceVariant = MintBackgroundDark,
    onSurfaceVariant = MintTextSecondaryDark
)

private val PastelDarkModeScheme = darkColorScheme(
    primary = PastelDarkPrimary,
    onPrimary = Color.Black,
    primaryContainer = PastelDarkSurface,
    onPrimaryContainer = PastelDarkSecondary,
    secondary = PastelDarkSecondary,
    onSecondary = Color.Black,
    background = PastelDarkBackground,
    onBackground = PastelDarkTextPrimary,
    surface = PastelDarkSurface,
    onSurface = PastelDarkTextPrimary,
    surfaceVariant = PastelDarkSurface,
    onSurfaceVariant = PastelDarkTextSecondary
)

// Stars Theme Scheme 🌟
private val StarsLight = lightColorScheme(
    primary = StarsPrimary,
    onPrimary = Color.Black,
    primaryContainer = StarsTertiary,
    onPrimaryContainer = StarsTextPrimaryLight,
    secondary = StarsSecondary,
    onSecondary = Color.White,
    background = StarsBackgroundLight,
    onBackground = StarsTextPrimaryLight,
    surface = StarsSurfaceLight,
    onSurface = StarsTextPrimaryLight,
    surfaceVariant = StarsTertiary,
    onSurfaceVariant = StarsTextSecondaryLight
)

private val StarsDark = darkColorScheme(
    primary = StarsPrimary,
    onPrimary = Color.Black,
    primaryContainer = StarsSurfaceDark,
    onPrimaryContainer = StarsSecondary,
    secondary = StarsSecondary,
    onSecondary = Color.Black,
    background = StarsBackgroundDark,
    onBackground = StarsTextPrimaryDark,
    surface = StarsSurfaceDark,
    onSurface = StarsTextPrimaryDark,
    surfaceVariant = StarsBackgroundDark,
    onSurfaceVariant = StarsTextSecondaryDark
)

// Capybara Theme Scheme 🥔
private val CapybaraLight = lightColorScheme(
    primary = CapybaraPrimary,
    onPrimary = Color.White,
    primaryContainer = CapybaraTertiary,
    onPrimaryContainer = CapybaraTextPrimaryLight,
    secondary = CapybaraSecondary,
    onSecondary = CapybaraTextPrimaryLight,
    background = CapybaraBackgroundLight,
    onBackground = CapybaraTextPrimaryLight,
    surface = CapybaraSurfaceLight,
    onSurface = CapybaraTextPrimaryLight,
    surfaceVariant = CapybaraTertiary,
    onSurfaceVariant = CapybaraTextSecondaryLight
)

private val CapybaraDark = darkColorScheme(
    primary = CapybaraPrimary,
    onPrimary = Color.Black,
    primaryContainer = CapybaraSurfaceDark,
    onPrimaryContainer = CapybaraSecondary,
    secondary = CapybaraSecondary,
    onSecondary = Color.Black,
    background = CapybaraBackgroundDark,
    onBackground = CapybaraTextPrimaryDark,
    surface = CapybaraSurfaceDark,
    onSurface = CapybaraTextPrimaryDark,
    surfaceVariant = CapybaraBackgroundDark,
    onSurfaceVariant = CapybaraTextSecondaryDark
)

// Strawberry Cow Theme Scheme 🐮
private val StrawberryCowLight = lightColorScheme(
    primary = StrawberryCowPrimary,
    onPrimary = Color.White,
    primaryContainer = StrawberryCowTertiary,
    onPrimaryContainer = StrawberryCowTextPrimaryLight,
    secondary = StrawberryCowSecondary,
    onSecondary = StrawberryCowTextPrimaryLight,
    background = StrawberryCowBackgroundLight,
    onBackground = StrawberryCowTextPrimaryLight,
    surface = StrawberryCowSurfaceLight,
    onSurface = StrawberryCowTextPrimaryLight,
    surfaceVariant = StrawberryCowTertiary,
    onSurfaceVariant = StrawberryCowTextSecondaryLight
)

private val StrawberryCowDark = darkColorScheme(
    primary = StrawberryCowPrimary,
    onPrimary = Color.Black,
    primaryContainer = StrawberryCowSurfaceDark,
    onPrimaryContainer = StrawberryCowSecondary,
    secondary = StrawberryCowSecondary,
    onSecondary = Color.Black,
    background = StrawberryCowBackgroundDark,
    onBackground = StrawberryCowTextPrimaryDark,
    surface = StrawberryCowSurfaceDark,
    onSurface = StrawberryCowTextPrimaryDark,
    surfaceVariant = StrawberryCowBackgroundDark,
    onSurfaceVariant = StrawberryCowTextSecondaryDark
)

// Cow Print Theme Scheme 🐄
private val CowPrintLight = lightColorScheme(
    primary = CowPrintPrimary,
    onPrimary = Color.White,
    primaryContainer = CowPrintTertiary,
    onPrimaryContainer = CowPrintTextPrimaryLight,
    secondary = CowPrintSecondary,
    onSecondary = Color.White,
    background = CowPrintBackgroundLight,
    onBackground = CowPrintTextPrimaryLight,
    surface = CowPrintSurfaceLight,
    onSurface = CowPrintTextPrimaryLight,
    surfaceVariant = CowPrintTertiary,
    onSurfaceVariant = CowPrintTextSecondaryLight
)

private val CowPrintDark = darkColorScheme(
    primary = CowPrintPrimary,
    onPrimary = Color.White,
    primaryContainer = CowPrintSurfaceDark,
    onPrimaryContainer = CowPrintSecondary,
    secondary = CowPrintSecondary,
    onSecondary = Color.Black,
    background = CowPrintBackgroundDark,
    onBackground = CowPrintTextPrimaryDark,
    surface = CowPrintSurfaceDark,
    onSurface = CowPrintTextPrimaryDark,
    surfaceVariant = CowPrintBackgroundDark,
    onSurfaceVariant = CowPrintTextSecondaryDark
)

// Penguin Theme Scheme 🐧
private val PenguinLight = lightColorScheme(
    primary = PenguinPrimary,
    onPrimary = Color.White,
    primaryContainer = PenguinTertiary,
    onPrimaryContainer = PenguinTextPrimaryLight,
    secondary = PenguinSecondary,
    onSecondary = PenguinTextPrimaryLight,
    background = PenguinBackgroundLight,
    onBackground = PenguinTextPrimaryLight,
    surface = PenguinSurfaceLight,
    onSurface = PenguinTextPrimaryLight,
    surfaceVariant = PenguinTertiary,
    onSurfaceVariant = PenguinTextSecondaryLight
)

private val PenguinDark = darkColorScheme(
    primary = PenguinPrimary,
    onPrimary = Color.Black,
    primaryContainer = PenguinSurfaceDark,
    onPrimaryContainer = PenguinSecondary,
    secondary = PenguinSecondary,
    onSecondary = Color.Black,
    background = PenguinBackgroundDark,
    onBackground = PenguinTextPrimaryDark,
    surface = PenguinSurfaceDark,
    onSurface = PenguinTextPrimaryDark,
    surfaceVariant = PenguinBackgroundDark,
    onSurfaceVariant = PenguinTextSecondaryDark
)

// Panda Theme Scheme 🐼
private val PandaLight = lightColorScheme(
    primary = PandaPrimary,
    onPrimary = Color.White,
    primaryContainer = PandaTertiary,
    onPrimaryContainer = PandaTextPrimaryLight,
    secondary = PandaSecondary,
    onSecondary = Color.White,
    background = PandaBackgroundLight,
    onBackground = PandaTextPrimaryLight,
    surface = PandaSurfaceLight,
    onSurface = PandaTextPrimaryLight,
    surfaceVariant = PandaTertiary,
    onSurfaceVariant = PandaTextSecondaryLight
)

private val PandaDark = darkColorScheme(
    primary = PandaPrimary,
    onPrimary = Color.White,
    primaryContainer = PandaSurfaceDark,
    onPrimaryContainer = PandaSecondary,
    secondary = PandaSecondary,
    onSecondary = Color.Black,
    background = PandaBackgroundDark,
    onBackground = PandaTextPrimaryDark,
    surface = PandaSurfaceDark,
    onSurface = PandaTextPrimaryDark,
    surfaceVariant = PandaBackgroundDark,
    onSurfaceVariant = PandaTextSecondaryDark
)

@Composable
fun MangaReaderKawaiiTheme(
    appTheme: AppTheme = AppTheme.SAKURA_PINK,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.SAKURA_PINK -> if (darkTheme) SakuraPinkDark else SakuraPinkLight
        AppTheme.PASTEL_LAVENDER -> if (darkTheme) LavenderDark else LavenderLight
        AppTheme.PEACHY_CREAM -> if (darkTheme) PeachDark else PeachLight
        AppTheme.MINT_GREEN -> if (darkTheme) MintDark else MintLight
        AppTheme.PASTEL_DARK -> PastelDarkModeScheme
        AppTheme.STARLIGHT_STARS -> if (darkTheme) StarsDark else StarsLight
        AppTheme.COZY_CAPYBARA -> if (darkTheme) CapybaraDark else CapybaraLight
        AppTheme.STRAWBERRY_COW -> if (darkTheme) StrawberryCowDark else StrawberryCowLight
        AppTheme.COW_PRINT -> if (darkTheme) CowPrintDark else CowPrintLight
        AppTheme.ARCTIC_PENGUIN -> if (darkTheme) PenguinDark else PenguinLight
        AppTheme.BAMBOO_PANDA -> if (darkTheme) PandaDark else PandaLight
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme && appTheme != AppTheme.PASTEL_DARK
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KawaiiTypography,
        content = content
    )
}
