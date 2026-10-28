package com.teqmed.teqruta.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TeqLightColorScheme = lightColorScheme(
    primary = TeqNavyPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6EAF8),
    onPrimaryContainer = TeqNavyDark,
    secondary = TeqTealSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2F1),
    onSecondaryContainer = Color(0xFF004D40),
    background = TeqLightBg,
    surface = Color.White,
    error = TeqErrorRed,
    onError = Color.White
)

@Composable
fun TeqRutaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TeqLightColorScheme,
        typography = Typography,
        content = content
    )
}
