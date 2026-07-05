package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ImhoDarkPrimary,
    secondary = ImhoDarkSecondary,
    tertiary = ImhoDarkTertiary,
    background = ImhoBgDark,
    surface = ImhoSurfaceDark,
    onPrimary = ImhoBgDark,
    onSecondary = ImhoBgDark,
    onTertiary = ImhoBgDark,
    onBackground = ImhoBgLight,
    onSurface = ImhoBgLight
)

private val LightColorScheme = lightColorScheme(
    primary = ImhoBluePrimary,
    secondary = ImhoBlueSecondary,
    tertiary = ImhoBlueTertiary,
    background = ImhoBgLight,
    surface = ImhoSurfaceLight,
    onPrimary = ImhoSurfaceLight,
    onSecondary = ImhoSurfaceLight,
    onTertiary = ImhoSurfaceLight,
    onBackground = ImhoBgDark,
    onSurface = ImhoBgDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branding colors consistent
    content: @Composable () -> Unit,
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
