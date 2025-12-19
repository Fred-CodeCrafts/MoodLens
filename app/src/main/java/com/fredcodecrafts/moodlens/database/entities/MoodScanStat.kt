package com.fredcodecrafts.moodlens.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_scan_stats")
data class MoodScanStat(
    @PrimaryKey val statId: String = "",
    val userId: String = "",
    val date: Long = 0L,
    val dailyScans: Int = 0,
    val weekStreak: Int = 0,
    val canAccessInsights: Boolean = false
)
