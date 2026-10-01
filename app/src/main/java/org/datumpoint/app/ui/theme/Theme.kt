package org.datumpoint.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF0E5C4A),
    onPrimary = Color.White,
    secondary = Color(0xFF50675F),
    tertiary = Color(0xFF7B5F00),
    background = Color(0xFFF8FBF8),
    surface = Color(0xFFFFFFFF),
    error = Color(0xFFB3261E),
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF8FE3C0),
    onPrimary = Color(0xFF00382C),
    secondary = Color(0xFFB7CCC3),
    tertiary = Color(0xFFF6D365),
    background = Color(0xFF101412),
    surface = Color(0xFF171D1A),
    error = Color(0xFFFFB4AB),
)

@Composable
fun DatumPointTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content,
    )
}
