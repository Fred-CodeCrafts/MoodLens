package com.fredcodecrafts.moodlens.utils

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.runtime.*
import kotlinx.coroutines.launch

import androidx.compose.animation.core.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.alpha

@Composable
fun Modifier.floatAnimation(
    amplitude: Float = 10f,
    durationMillis: Int = 3000
): Modifier {
    val transition = rememberInfiniteTransition()
    val offsetY by transition.animateFloat(
        initialValue = 0f,
        targetValue = -amplitude,
        animationSpec = infiniteRepeatable(
            // <-- explicit generic here
            animation = tween<Float>(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    return this.graphicsLayer { translationY = offsetY }
}

@Composable
fun Modifier.pulseSoftAnimation(durationMillis: Int = 2000): Modifier {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween<Float>(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    return this.alpha(alpha)
}



@Composable
fun Modifier.fadeInAnimation(
    durationMillis: Int = 300
): Modifier {
    val scope = rememberCoroutineScope()
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.95f) }

    LaunchedEffect(Unit) {
        scope.launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis, easing = LinearOutSlowInEasing)
            )
        }
        scope.launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis)
            )
        }
    }

    return this.graphicsLayer {
        this.alpha = alpha.value
        this.scaleX = scale.value
        this.scaleY = scale.value
    }
}

// SLIDE-UP — entry motion
@Composable
fun Modifier.slideUpAnimation(
    durationMillis: Int = 400
): Modifier {
    val scope = rememberCoroutineScope()
    val offsetY = remember { Animatable(100f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scope.launch {
            offsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
            )
        }
        scope.launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis)
            )
        }
    }

    return this.graphicsLayer {
        translationY = offsetY.value
        this.alpha = alpha.value
    }
}