package com.fredcodecrafts.moodlens.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages_offline")
data class Message(
    @PrimaryKey val messageId: String,
    val entryId: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long
)
