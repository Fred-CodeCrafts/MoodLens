package com.fredcodecrafts.moodlens.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.fredcodecrafts.moodlens.R
import com.fredcodecrafts.moodlens.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import kotlin.math.abs


@Composable
fun InAppNotification(
    state: NotificationState,
    modifier: Modifier = Modifier
) {
    val notification = state.notification

    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(durationMillis = 300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = 250)
        ),
        modifier = modifier
    ) {
        notification?.let { data ->
            NotificationContent(
                data = data,
                onDismiss = { state.dismissNotification() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun NotificationContent(
    data: NotificationData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    // Auto-dismiss after duration
    LaunchedEffect(data.id) {
        if (data.duration > 0) {
            delay(data.duration)
            onDismiss()
        }
    }

    val textColor = NotificationText
    val iconRes = when (data.type) {
        NotificationType.SUCCESS -> R.drawable.ic_success
        NotificationType.ERROR -> R.drawable.ic_error
        NotificationType.WARNING -> R.drawable.ic_warning
        NotificationType.INFO -> R.drawable.ic_info
    }
    val iconColor = when (data.type) {
        NotificationType.SUCCESS -> NotificationSuccess
        NotificationType.ERROR -> NotificationError
        NotificationType.WARNING -> NotificationWarning
        NotificationType.INFO -> NotificationInfo
    }


    Surface(
        modifier = modifier
            .padding(16.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        // If swiped enough, dismiss
                        if (abs(offsetX.value) > size.width * 0.3f) {
                            scope.launch {
                                offsetX.animateTo(
                                    targetValue = if (offsetX.value > 0) size.width.toFloat() else -size.width.toFloat(),
                                    animationSpec = tween(300)
                                )
                                onDismiss()
                            }
                        } else {
                            // Snap back if not swiped enough
                            scope.launch {
                                offsetX.animateTo(0f, tween(300))
                            }
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount)
                        }
                    }
                )
            }
            .offset { IntOffset(offsetX.value.toInt(), 0) },
        color = MainBackground,
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 8.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.padding(end = 12.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                if (data.title.isNotEmpty()) {
                    Text(
                        text = data.title,
                        color = textColor,
                        style = AppTypography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = data.message,
                    color = textColor,
                    style = AppTypography.bodyMedium
                )
            }

            data.actionLabel?.let { actionLabel ->
                TextButton(
                    onClick = {
                        data.onAction?.invoke()
                        onDismiss()
                    }
                ) {
                    Text(
                        text = actionLabel,
                        color = textColor,
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
