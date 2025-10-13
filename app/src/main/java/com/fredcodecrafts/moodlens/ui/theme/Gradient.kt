package com.fredcodecrafts.moodlens.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun gradientPrimary(): Brush {
    val scheme = MaterialTheme.colorScheme
    return Brush.verticalGradient(
        colors = listOf(
            scheme.primary,
            scheme.secondary.copy(alpha = 0.8f)
        )
    )
}
