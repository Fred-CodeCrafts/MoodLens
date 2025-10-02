package com.fredcodecrafts.moodlens.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember

data class NotificationData(
    val id: String = System.currentTimeMillis().toString(),
    val title: String = "",
    val message: String,
    val type: NotificationType = NotificationType.INFO,
    val duration: Long = 4000L,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)

enum class NotificationType {
    SUCCESS, ERROR, INFO, WARNING
}

class NotificationState {
    var notification by mutableStateOf<NotificationData?>(null)
        private set

    fun showNotification(data: NotificationData) {
        notification = data
    }

    fun dismissNotification() {
        notification = null
    }
}

@Composable
fun rememberNotificationState(): NotificationState {
    return remember { NotificationState() }
}