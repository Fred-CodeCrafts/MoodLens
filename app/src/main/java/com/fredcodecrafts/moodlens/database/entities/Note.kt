package com.fredcodecrafts.moodlens.database.entities


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes_offline")
data class Note(
    @PrimaryKey val noteId: String,
    val entryId: String,
    val content: String
)
