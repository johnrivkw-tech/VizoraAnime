package com.example.animetracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = VizoraViolet,
    onPrimary = Color(0xFFFFFFFF),
    secondary = VizoraPink,
    tertiary = VizoraCyan,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = Color(0xFFEDE7F6),
    onSurface = Color(0xFFEDE7F6),
    onSurfaceVariant = DarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = VizoraVioletDark,
    onPrimary = Color(0xFFFFFFFF),
    secondary = VizoraPink,
    tertiary = Color(0xFF0E7490),
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    onSurfaceVariant = LightOnSurfaceVariant
)

/**
 * App-wide theme. Follows the system dark/light setting by default.
 *
 * Dynamic color (Android 12+ wallpaper-based theming) defaults to OFF so the
 * app always shows its own violet/pink/cyan brand palette instead of
 * whatever colors happen to match the user's wallpaper — pass
 * dynamicColor = true to opt back into Material You if you'd rather follow
 * the system palette.
 */
@Composable
fun AnimeTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
