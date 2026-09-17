package com.bydlauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

enum class ThemeMode { DARK, DARKER }

private val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = BackgroundDeep,
    primaryContainer = AccentCyanDim,
    onPrimaryContainer = TextPrimary,
    secondary = TextSecondary,
    onSecondary = TextPrimary,
    background = BackgroundDeep,
    onBackground = TextPrimary,
    surface = BackgroundSurface,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundCard,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
)

private val DarkerColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = BackgroundDeepDarker,
    primaryContainer = AccentCyanDim,
    onPrimaryContainer = TextPrimary,
    secondary = TextSecondary,
    onSecondary = TextPrimary,
    background = BackgroundDeepDarker,
    onBackground = TextPrimary,
    surface = BackgroundSurfaceDarker,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundCardDarker,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
)

@Composable
fun BYDLauncherTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (themeMode == ThemeMode.DARKER) DarkerColorScheme else DarkColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
