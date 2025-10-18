package com.fredcodecrafts.moodlens.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey val entryId: String,
    val userId: String,
    val mood: String, // happy, sad, anxious, calm, excited, tired
    val timestamp: Long,
    val location: String? = null, // 🆕 optional field for city, coordinates, etc.
    val aiReflection : String? = null
)