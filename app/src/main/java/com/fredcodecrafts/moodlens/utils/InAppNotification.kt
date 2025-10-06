package com.fredcodecrafts.moodlens.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.fredcodecrafts.moodlens.R
import com.fredcodecrafts.moodlens.ui.theme.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight

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
    LaunchedEffect(data.id) {
        if (data.duration > 0) {
            delay(data.duration)
            onDismiss()
        }
    }

    val textColor = TextPrimary
    val iconRes = when (data.type) {
        NotificationType.SUCCESS -> R.drawable.ic_success
        NotificationType.ERROR -> R.drawable.ic_error
        NotificationType.WARNING -> R.drawable.ic_warning
        NotificationType.INFO -> R.drawable.ic_info
    }

    Surface(
        modifier = modifier.padding(16.dp),
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
                tint = textColor,
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
