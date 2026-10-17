package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SyhakalDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF03161C),
    primaryContainer = Color(0xFF073844),
    onPrimaryContainer = Color(0xFF86F7FF),

    secondary = NeonPurple,
    onSecondary = Color(0xFF28003A),
    secondaryContainer = Color(0xFF4C006C),
    onSecondaryContainer = Color(0xFFF3B4FF),

    tertiary = CyberPink,
    onTertiary = Color(0xFF3B0018),
    tertiaryContainer = Color(0xFF6B0031),
    onTertiaryContainer = Color(0xFFFFB2CB),

    background = CyberBackground,
    onBackground = TextPrimary,

    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextSecondary,

    error = ErrorRed,
    onError = Color.White
)

@Composable
fun SyhakalTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SyhakalDarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Retain alias for template compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SyhakalTheme(content = content)
}
