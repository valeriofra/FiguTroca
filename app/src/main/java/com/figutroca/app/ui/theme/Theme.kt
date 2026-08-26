package com.figutroca.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Blue40,
    onPrimary = Color.White,
    primaryContainer = Blue90,
    onPrimaryContainer = Blue10,
    secondary = Red40,
    onSecondary = Color.White,
    secondaryContainer = Red90,
    onSecondaryContainer = Color(0xFF40000A),
    tertiary = SunsetOrange,
    onTertiary = Color.White,
    background = Cool99,
    onBackground = Cool10,
    surface = Cool99,
    onSurface = Cool10,
    surfaceVariant = Cool90,
    onSurfaceVariant = Cool20,
)

private val DarkColors = darkColorScheme(
    primary = Blue80,
    onPrimary = Blue20,
    primaryContainer = Blue30,
    onPrimaryContainer = Blue90,
    secondary = Red80,
    onSecondary = Color(0xFF5F0016),
    secondaryContainer = Red40,
    onSecondaryContainer = Red90,
    tertiary = SunsetOrange,
    onTertiary = Color(0xFF3A1700),
    background = Cool10,
    onBackground = Cool90,
    surface = Cool10,
    onSurface = Cool90,
    surfaceVariant = Cool20,
    onSurfaceVariant = Cool90,
)

@Composable
fun FiguTrocaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Off by default so the World Cup 2026 brand palette always shows,
    // instead of the device's Material You wallpaper colors.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                colorScheme.background.luminance() > 0.5f
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
