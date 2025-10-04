package com.fredcodecrafts.moodlens.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions_offline")
data class Question(
    @PrimaryKey val questionId: String,
    val emotionLabel: String,  // Sadness, Anger, Anxiety, Happiness, Stress
    val type: String,          // "opening" or "prompt"
    val text: String
)
