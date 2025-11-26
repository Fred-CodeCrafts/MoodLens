package com.fredcodecrafts.moodlens.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey val entryId: String,
    val userId: String,
    val mood: String, // happy, sad, anxious, calm, excited, tired
    val timestamp: Long,

    // 📍 LOCATION DATA
    val locationName: String? = null, // e.g. "Central Jakarta"
    val latitude: Double? = null,     // e.g. -6.2088
    val longitude: Double? = null,    // e.g. 106.8456

    val aiReflection : String? = null
)