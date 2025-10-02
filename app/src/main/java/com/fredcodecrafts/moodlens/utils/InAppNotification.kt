package com.fredcodecrafts.moodlens.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fredcodecrafts.moodlens.R
import kotlinx.coroutines.delay

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
    // Auto-dismiss functionality
    LaunchedEffect(data.id) {
        if (data.duration > 0) {
            delay(data.duration)
            onDismiss()
        }
    }

    val (backgroundColor, textColor, iconRes) = when (data.type) {
        NotificationType.SUCCESS -> Triple(
            Color(0xFF4CAF50),
            Color.White,
            R.drawable.ic_success
        )
        NotificationType.ERROR -> Triple(
            Color(0xFFF44336),
            Color.White,
            R.drawable.ic_error
        )
        NotificationType.WARNING -> Triple(
            Color(0xFFFF9800),
            Color.White,
            R.drawable.ic_warning
        )
        NotificationType.INFO -> Triple(
            Color(0xFF2196F3),
            Color.White,
            R.drawable.ic_info
        )
    }

    Surface(
        modifier = modifier.padding(16.dp),
        color = backgroundColor,
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Icon and Text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.padding(end = 12.dp)
                )

                Column {
                    if (data.title.isNotEmpty()) {
                        Text(
                            text = data.title,
                            color = textColor,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = data.message,
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Action Button (if provided)
            data.actionLabel?.let { actionLabel ->
                TextButton(
                    onClick = {
                        data.onAction?.invoke()
                        onDismiss()
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text(
                        text = actionLabel,
                        color = textColor,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}