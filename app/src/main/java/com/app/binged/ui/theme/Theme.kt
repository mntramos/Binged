package com.app.binged.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BingedColorScheme = darkColorScheme(
    primary = BingedAccent,
    onPrimary = TextOnPrimary,
    primaryContainer = BingedAccentDim,
    secondary = TextSecondary,
    tertiary = TextSecondary,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkOutline,
    error = BingedRed,
    onError = TextOnPrimary
)

@Composable
fun BingedTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BingedColorScheme,
        typography = Typography,
        content = content
    )
}