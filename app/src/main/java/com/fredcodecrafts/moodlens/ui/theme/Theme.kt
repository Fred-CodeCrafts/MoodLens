package com.fredcodecrafts.moodlens.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = MainPurple,
    onPrimary = MainBackground,
    background = MainBackground,
    onBackground = TextPrimary,
    secondary = LightPurple,
    onSecondary = TextPrimary,
    surfaceVariant = CardBackground

)

private val DarkColors = darkColorScheme(
    primary = MainPurple,
    onPrimary = MainBackground,
    background = TextPrimary,
    onBackground = MainBackground,
    secondary = DarkPurple,
    onSecondary = MainBackground,
    surfaceVariant = CardBackground


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

val ColorScheme.CardBackgroundColor: Color
    @Composable
    get() = CardBackground

val ColorScheme.CardShadowColor: Color
    @Composable
    get() = CardShadow