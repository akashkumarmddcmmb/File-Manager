package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.model.AccentColorType
import com.example.model.ThemeMode

@Composable
fun FilesTheme(
    themeMode: ThemeMode,
    accentColor: AccentColorType,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = when {
        // Material You Dynamic Colors
        accentColor == AccentColorType.SYSTEM_DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Custom Static Accents
        else -> {
            val primary = when (accentColor) {
                AccentColorType.SYSTEM_DYNAMIC -> Color(0xFF1A73E8) // Fallback Blue
                AccentColorType.EMERALD -> Color(0xFF00C853)
                AccentColorType.OCEAN -> Color(0xFF00B0FF)
                AccentColorType.ROYAL -> Color(0xFF6200EE)
                AccentColorType.SUNSET -> Color(0xFFFF3D00)
                AccentColorType.GOLD -> Color(0xFFFFD600)
                AccentColorType.ARCTIC -> Color(0xFF00E5FF)
                AccentColorType.CUSTOM -> Color(0xFF7C4DFF) // Custom Purple Accent
            }

            if (darkTheme) {
                // High contrast dark background for battery optimization
                darkColorScheme(
                    primary = primary,
                    secondary = primary.copy(alpha = 0.8f),
                    background = Color(0xFF0D0F12), // Deep Pitch Black-Grey OLED look
                    surface = Color(0xFF161A22),
                    surfaceVariant = Color(0xFF1E2430),
                    onPrimary = Color.White,
                    onBackground = Color(0xFFE2E2E2),
                    onSurface = Color(0xFFF1F1F1),
                    onSurfaceVariant = Color(0xFFC4C7CC)
                )
            } else {
                lightColorScheme(
                    primary = primary,
                    secondary = primary.copy(alpha = 0.8f),
                    background = Color(0xFFF8F9FA),
                    surface = Color.White,
                    surfaceVariant = Color(0xFFEDF1F7),
                    onPrimary = Color.White,
                    onBackground = Color(0xFF1F1F1F),
                    onSurface = Color(0xFF2C2C2C),
                    onSurfaceVariant = Color(0xFF5F6368)
                )
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
