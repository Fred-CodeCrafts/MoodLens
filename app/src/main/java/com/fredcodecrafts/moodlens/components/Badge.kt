package com.fredcodecrafts.moodlens.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class BadgeType {
    Default,
    Secondary,
    Destructive,
    Outline
}

@Composable
fun AppBadge(
    text: String,
    type: BadgeType = BadgeType.Default,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
) {
    // Theme-aware colors
    val (backgroundColor, borderColor, textColor) = when (type) {
        BadgeType.Default -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            Color.Transparent,
            MaterialTheme.colorScheme.primary
        )
        BadgeType.Secondary -> Triple(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
            Color.Transparent,
            MaterialTheme.colorScheme.secondary
        )
        BadgeType.Destructive -> Triple(
            MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
            Color.Transparent,
            MaterialTheme.colorScheme.error
        )
        BadgeType.Outline -> Triple(
            Color.Transparent,
            MaterialTheme.colorScheme.outline,
            MaterialTheme.colorScheme.onSurface
        )
    }

    // Animate appearance
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(visible) { isVisible = visible }

    AnimatedVisibility(visible = isVisible) {
        val scaleAnim by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0.8f,
            animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
            label = "badge-scale"
        )

        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
            modifier = modifier
                .scale(scaleAnim)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .then(
                    if (type == BadgeType.Outline) {
                        Modifier.border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    } else Modifier
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
