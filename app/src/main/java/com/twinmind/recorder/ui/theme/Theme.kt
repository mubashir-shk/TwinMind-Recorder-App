package com.twinmind.recorder.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// TwinMind-inspired color palette
val Primary = Color(0xFF6C63FF)
val PrimaryVariant = Color(0xFF4D44DB)
val Secondary = Color(0xFF03DAC6)
val Background = Color(0xFF0F0F1A)
val Surface = Color(0xFF1A1A2E)
val SurfaceVariant = Color(0xFF252540)
val OnBackground = Color(0xFFE8E8F0)
val OnSurface = Color(0xFFE8E8F0)
val OnSurfaceVariant = Color(0xFF9898B0)
val RecordingRed = Color(0xFFFF4F4F)
val RecordingRedLight = Color(0xFFFF7575)
val SuccessGreen = Color(0xFF4CAF50)
val WarningOrange = Color(0xFFFF9800)
val ErrorRed = Color(0xFFCF6679)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryVariant,
    secondary = Secondary,
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    onBackground = OnBackground,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    error = ErrorRed
)

@Composable
fun TwinMindTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
