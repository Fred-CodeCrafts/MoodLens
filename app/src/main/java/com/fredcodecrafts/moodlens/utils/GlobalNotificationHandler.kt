package com.fredcodecrafts.moodlens.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
fun GlobalNotificationHandler(
    state: NotificationState,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val notification = state.notification

    if (notification != null) {
        Popup(
            alignment = Alignment.TopCenter,
            properties = PopupProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                excludeFromSystemGesture = true
            ),
            onDismissRequest = {
                state.dismissNotification()
                onDismiss()
            }
        ) {
            InAppNotification(
                state = state,
                modifier = modifier
            )
        }
    }
}