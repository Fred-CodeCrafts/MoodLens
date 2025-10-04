package com.fredcodecrafts.moodlens.utils

// Extension functions for easy notification showing
fun NotificationState.showSuccess(
    message: String,
    title: String = "Success",
    duration: Long = 3000L,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    showNotification(
        NotificationData(
            title = title,
            message = message,
            type = NotificationType.SUCCESS,
            duration = duration,
            actionLabel = actionLabel,
            onAction = onAction
        )
    )
}

fun NotificationState.showError(
    message: String,
    title: String = "Error",
    duration: Long = 5000L,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    showNotification(
        NotificationData(
            title = title,
            message = message,
            type = NotificationType.ERROR,
            duration = duration,
            actionLabel = actionLabel,
            onAction = onAction
        )
    )
}

fun NotificationState.showInfo(
    message: String,
    title: String = "Info",
    duration: Long = 4000L,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    showNotification(
        NotificationData(
            title = title,
            message = message,
            type = NotificationType.INFO,
            duration = duration,
            actionLabel = actionLabel,
            onAction = onAction
        )
    )
}

fun NotificationState.showWarning(
    message: String,
    title: String = "Warning",
    duration: Long = 4000L,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    showNotification(
        NotificationData(
            title = title,
            message = message,
            type = NotificationType.WARNING,
            duration = duration,
            actionLabel = actionLabel,
            onAction = onAction
        )
    )
}

// MoodLens specific notification functions
fun NotificationState.showReflectionSaved() {
    showSuccess(
        title = "Reflection Saved",
        message = "Your reflection has been saved successfully!",
        actionLabel = "View",
        onAction = {
            // Navigate to reflections screen
        }
    )
}

fun NotificationState.showJournalEntrySaved() {
    showSuccess(
        title = "Journal Saved",
        message = "Your journal entry has been saved successfully!",
        duration = 3000L
    )
}

fun NotificationState.showMoodAnalyzed(mood: String) {
    showInfo(
        title = "Mood Analysis Complete",
        message = "Detected mood: $mood. Review your insights in the journal.",
        actionLabel = "Review",
        onAction = {
            // Navigate to insights screen
        }
    )
}

fun NotificationState.showCameraError() {
    showError(
        title = "Camera Error",
        message = "Unable to access camera. Please check permissions.",
        actionLabel = "Settings",
        onAction = {
            // Open app settings
        }
    )
}

fun NotificationState.showNetworkError() {
    showError(
        title = "Connection Error",
        message = "Please check your internet connection and try again.",
        actionLabel = "Retry",
        onAction = {
            // Retry action
        }
    )
}

fun NotificationState.showReflectionComplete() {
    showInfo(
        title = "Reflection Complete",
        message = "Take a moment to review your insights and emotional patterns.",
        duration = 5000L,
        actionLabel = "View Insights",
        onAction = {
            // Navigate to insights screen
        }
    )
}