package com.arogyamitra.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ArogyaPrimary,
    onPrimary = BackgroundDark,
    primaryContainer = ArogyaPrimary,
    onPrimaryContainer = BackgroundDark,
    secondary = TealAccent,
    onSecondary = BackgroundDark,
    tertiary = BlueAccent,
    onTertiary = TextPrimaryDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceGlass,
    onSurfaceVariant = TextSecondaryDark,
    error = StatusError,
    onError = TextPrimaryDark,
    outline = BorderDark,
    outlineVariant = BorderPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = ArogyaPrimary,
    onPrimary = BackgroundDark,
    primaryContainer = ArogyaPrimary,
    onPrimaryContainer = BackgroundDark,
    secondary = TealAccent,
    onSecondary = BackgroundDark,
    tertiary = BlueAccent,
    onTertiary = TextPrimaryLight,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceGlassWhite,
    onSurfaceVariant = TextSecondaryLight,
    error = StatusError,
    onError = TextPrimaryDark,
    outline = BorderLight,
    outlineVariant = BorderPrimary
)

@Composable
fun ArogyaMitraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ArogyaTypography,
        content = content
    )
}
