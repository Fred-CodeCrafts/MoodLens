package com.fredcodecrafts.moodlens.ml.emotionPrediction

/**
 * Canonical FER-2013 order (must match model output order):
 * 0 = Angry
 * 1 = Disgust
 * 2 = Fear
 * 3 = Happy
 * 4 = Sad
 * 5 = Surprise
 * 6 = Neutral
 */
object EmotionLabels {
    val EMOTIONS = listOf(
        "angry",
        "disgust",
        "fear",
        "happy",
        "sad",
        "surprise",
        "neutral"
    )
}
