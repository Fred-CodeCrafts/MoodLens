package com.fredcodecrafts.moodlens.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_scan_stats")
data class MoodScanStat(
    @PrimaryKey val statId: String,
    val userId: String,
    val date: Long,
    val dailyScans: Int,
    val weekStreak: Int,
    val canAccessInsights: Boolean
)
