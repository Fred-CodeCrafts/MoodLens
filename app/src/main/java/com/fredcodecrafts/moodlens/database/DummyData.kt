package com.fredcodecrafts.moodlens.database

import com.fredcodecrafts.moodlens.database.entities.*
import java.util.*

object DummyData {

    val users = listOf(
        User(userId = "user1", googleId = "google_123")
    )

    val journalEntries = listOf(
        JournalEntry(
            entryId = "entry1",
            userId = "user1",
            mood = "happy",
            timestamp = Date().time
        ),
        JournalEntry(
            entryId = "entry2",
            userId = "user1",
            mood = "sad",
            timestamp = Date(System.currentTimeMillis() - 86400000L).time // 1 day ago
        )
    )

    val notes = listOf(
        Note(noteId = "note1", entryId = "entry1", content = "Had a great workout!"),
        Note(noteId = "note2", entryId = "entry2", content = "Felt lonely today")
    )

    val messages = listOf(
        Message(
            messageId = "msg1",
            entryId = "entry1",
            text = "Hi! How are you feeling today?",
            isUser = false,
            timestamp = Date().time
        ),
        Message(
            messageId = "msg2",
            entryId = "entry1",
            text = "I'm feeling happy!",
            isUser = true,
            timestamp = Date().time
        )
    )

    val moodScanStats = listOf(
        MoodScanStat(
            statId = "stat1",
            userId = "user1",
            date = Date().time,
            dailyScans = 2,
            weekStreak = 1,
            canAccessInsights = false
        )
    )
}
