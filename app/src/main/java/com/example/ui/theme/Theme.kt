package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val EpicColorScheme = darkColorScheme(
    primary = EpicBlue,
    onPrimary = EpicWhite,
    primaryContainer = EpicBluePressed,
    onPrimaryContainer = EpicWhite,
    secondary = EpicSurfaceGray,
    onSecondary = EpicTextPrimary,
    background = EpicVoid,
    onBackground = EpicTextPrimary,
    surface = EpicCarbon,
    onSurface = EpicTextPrimary,
    surfaceVariant = EpicElevated,
    onSurfaceVariant = EpicTextSecondary,
    outline = EpicBorderGray,
    outlineVariant = EpicSurfaceGray,
    error = EpicError,
    onError = EpicWhite
)

@Composable
fun EpicLibraryTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EpicColorScheme,
        typography = EpicTypography,
        shapes = Shapes,
        content = content
    )
}
