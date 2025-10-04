package com.fredcodecrafts.moodlens.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries_offline")
data class JournalEntry(
    @PrimaryKey val entryId: String,
    val userId: String,
    val mood: String, // happy, sad, anxious, calm, excited, tired
    val timestamp: Long
)
