package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Files Official Emerald Green Palette (Matching the App Icon #00C853)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00C853),             // Official Emerald Green
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1B382B),    // Dark Emerald Pill Container
    onPrimaryContainer = Color(0xFF00C853),  // Vibrant Green Text & Icon
    secondary = Color(0xFF00C853),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1B382B),
    onSecondaryContainer = Color(0xFF00C853),
    tertiary = Color(0xFFF9AB00),            // Vivid Yellow
    background = Color(0xFF121316),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1E1F23),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF282A2F),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00C853),             // Official Emerald Green
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F5E9),    // Soft Light Green Container
    onPrimaryContainer = Color(0xFF004D20),
    secondary = Color(0xFF00C853),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8F5E9),
    onSecondaryContainer = Color(0xFF004D20),
    tertiary = Color(0xFFF9AB00),
    background = Color(0xFFF8F9FE),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFEEF0F8),
    onSurfaceVariant = Color(0xFF44474E),
    outline = Color(0xFF74777F)
)

@Composable
fun FilesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled dynamic system color so the app stays Files Emerald Green!
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    FilesTheme(darkTheme = darkTheme, dynamicColor = false, content = content)
}
