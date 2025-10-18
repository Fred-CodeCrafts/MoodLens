package com.fredcodecrafts.moodlens.utils

fun getMoodEmoji(mood: String): String {
    return when (mood.lowercase()) {
        "happy" -> "😊"
        "sad" -> "😢"
        "anxious" -> "😰"
        "calm" -> "😌"
        "excited" -> "🤩"
        "tired" -> "😴"
        "neutral" -> "😐"
        "angry" -> "😠"
        "surprised" -> "😲"
        else -> "😐"
    }
}

fun getTipIcon(tip: String): String {
    return when {
        tip.contains("stress") -> "💙"
        tip.contains("notes") || tip.contains("note") -> "📝"
        tip.contains("breathing") -> "🧘"
        else -> "💡"
    }
}