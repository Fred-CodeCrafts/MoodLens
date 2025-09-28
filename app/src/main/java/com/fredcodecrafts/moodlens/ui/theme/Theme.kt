package com.fredcodecrafts.moodlens.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = MainPurple,
    onPrimary = MainBackground,
    background = MainBackground,
    onBackground = TextPrimary,
    secondary = LightPurple,
    onSecondary = TextPrimary
)

private val DarkColors = darkColorScheme(
    primary = MainPurple,
    onPrimary = MainBackground,
    background = TextPrimary,
    onBackground = MainBackground,
    secondary = DarkPurple,
    onSecondary = MainBackground
)

@Composable
fun MoodLensTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
