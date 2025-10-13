package com.fredcodecrafts.moodlens.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import kotlin.comparisons.then

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
    textColorOverride: Color? = null,
    backgroundColorOverride: Color? = null,
    borderColorOverride: Color? = null,
    cornerRadius: Int = 12,
    horizontalPadding: Int = 8,
    verticalPadding: Int = 4,
    textSize: Int = 12,
    onClick: (() -> Unit)? = null
) {
    // Determine colors
    val (backgroundColor, borderColor, textColor) = when (type) {
        BadgeType.Default -> Triple(
            backgroundColorOverride ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            borderColorOverride ?: Color.Transparent,
            textColorOverride ?: MaterialTheme.colorScheme.primary
        )
        BadgeType.Secondary -> Triple(
            backgroundColorOverride ?: MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
            borderColorOverride ?: Color.Transparent,
            textColorOverride ?: MaterialTheme.colorScheme.secondary
        )
        BadgeType.Destructive -> Triple(
            backgroundColorOverride ?: MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
            borderColorOverride ?: Color.Transparent,
            textColorOverride ?: MaterialTheme.colorScheme.error
        )
        BadgeType.Outline -> Triple(
            backgroundColorOverride ?: Color.Transparent,
            borderColorOverride ?: MaterialTheme.colorScheme.outline,
            textColorOverride ?: MaterialTheme.colorScheme.onSurface
        )
    }

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
            style = MaterialTheme.typography.labelSmall.copy(fontSize = textSize.sp),
            modifier = modifier
                .scale(scaleAnim)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(cornerRadius.dp)
                )
                .then(
                    if (type == BadgeType.Outline)
                        Modifier.border(1.dp, borderColor, RoundedCornerShape(cornerRadius.dp))
                    else Modifier
                )
                .padding(horizontal = horizontalPadding.dp, vertical = verticalPadding.dp)
                .let { if (onClick != null) it.clickable { onClick() } else it }
        )
    }
}
