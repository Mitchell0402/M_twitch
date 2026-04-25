package dev.mitchell.mtwitch.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF8F7CFF),
    secondary = Color(0xFF4BD4B8),
    background = Color(0xFF101014),
    surface = Color(0xFF181820),
    surfaceVariant = Color(0xFF242432),
    onPrimary = Color.White,
    onSecondary = Color(0xFF07130F),
    onBackground = Color(0xFFF5F3FF),
    onSurface = Color(0xFFF5F3FF),
    onSurfaceVariant = Color(0xFFC9C5D8),
)

private val LightScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF5E4AE3),
    secondary = Color(0xFF007C69),
    background = Color(0xFFFAF9FF),
    surface = Color.White,
    surfaceVariant = Color(0xFFE7E3F5),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF17151F),
    onSurface = Color(0xFF17151F),
    onSurfaceVariant = Color(0xFF4C485C),
)

@Composable
fun MtwitchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content,
    )
}
