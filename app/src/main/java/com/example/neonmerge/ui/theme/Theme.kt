package com.example.neonmerge.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NeonColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DarkNavyBackground,
    primaryContainer = Color(0xFF0C2433),
    onPrimaryContainer = NeonCyan,
    secondary = NeonViolet,
    onSecondary = DarkNavyBackground,
    secondaryContainer = Color(0xFF24153B),
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary = NeonMagenta,
    onTertiary = DarkNavyBackground,
    background = DarkNavyBackground,
    onBackground = TextPrimary,
    surface = DarkNavySurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkNavyElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkNavyBorder,
    outlineVariant = Color(0xFF1B243B)
)

@Composable
fun NeonMergeTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = NeonColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkNavyBackground.toArgb()
            window.navigationBarColor = DarkNavyBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}