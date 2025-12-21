package com.fredcodecrafts.moodlens.database

import com.fredcodecrafts.moodlens.database.entities.*
import java.util.*

object DummyData {

    private val UMN_LAT = -6.256717
    private val UMN_LON = 106.618221
    private const val LOC = "UMN"

    val journalEntries = listOf(
        // SAD (40%)
        JournalEntry(
            entryId = "entry_sad_1",
            userId = "user1",
            mood = "sad",
            timestamp = Date().time,
            locationName = LOC,
            latitude = UMN_LAT,
            longitude = UMN_LON,
            aiReflection = "Sadness is valid. It's okay to slow down and reflect."
        ),
        JournalEntry(
            entryId = "entry_sad_2",
            userId = "user1",
            mood = "sad",
            timestamp = Date().time,
            locationName = LOC,
            latitude = UMN_LAT,
            longitude = UMN_LON,
            aiReflection = "Take a deep breath. You’re not alone in this feeling."
        ),
        JournalEntry(
            entryId = "entry_sad_3",
            userId = "user1",
            mood = "sad",
            timestamp = Date().time,
            locationName = LOC,
            latitude = UMN_LAT,
            longitude = UMN_LON,
            aiReflection = "What’s been on your mind lately?"
        ),

        // STRESSED (35%)
        JournalEntry(
            entryId = "entry_stress_1",
            userId = "user1",
            mood = "stressed",
            timestamp = Date().time,
            locationName = LOC,
            latitude = UMN_LAT,
            longitude = UMN_LON,
            aiReflection = "Stress can feel heavy. Try to pause and breathe slowly."
        ),
        JournalEntry(
            entryId = "entry_stress_2",
            userId = "user1",
            mood = "stressed",
            timestamp = Date().time,
            locationName = LOC,
            latitude = UMN_LAT,
            longitude = UMN_LON,
            aiReflection = "Consider writing down the things overwhelming you."
        ),
        JournalEntry(
            entryId = "entry_stress_3",
            userId = "user1",
            mood = "stressed",
            timestamp = Date().time,
            locationName = LOC,
            latitude = UMN_LAT,
            longitude = UMN_LON,
            aiReflection = "Your mind seems full today. Let’s unpack it slowly."
        ),

        // ANGRY (20%)
        JournalEntry(
            entryId = "entry_angry_1",
            userId = "user1",
            mood = "angry",
            timestamp = Date().time,
            locationName = LOC,
            latitude = UMN_LAT,
            longitude = UMN_LON,
            aiReflection = "Anger often signals something deeper. What triggered it?"
        ),
        JournalEntry(
            entryId = "entry_angry_2",
            userId = "user1",
            mood = "angry",
            timestamp = Date().time,
            locationName = LOC,
            latitude = UMN_LAT,
            longitude = UMN_LON,
            aiReflection = "It’s okay to feel upset. Let’s walk through what happened."
        ),

        // OTHERS (< 5%) — for variety & map color testing
        JournalEntry(
            entryId = "entry_tired_1",
            userId = "user1",
            mood = "tired",
            timestamp = Date().time,
            locationName = LOC,
            latitude = UMN_LAT,
            longitude = UMN_LON,
            aiReflection = "It sounds like you need a moment to rest."
        )
    )

    val notes = listOf(
        Note("note_sad_1", "entry_sad_1", "Feeling down since morning."),
        Note("note_sad_2", "entry_sad_2", "Unmotivated today."),
        Note("note_sad_3", "entry_sad_3", "Need a break from everything."),
        Note("note_stress_1", "entry_stress_1", "Assignments piling up."),
        Note("note_stress_2", "entry_stress_2", "Too many deadlines."),
        Note("note_stress_3", "entry_stress_3", "Brain feels overloaded."),
        Note("note_angry_1", "entry_angry_1", "Group project drama again…"),
        Note("note_angry_2", "entry_angry_2", "People testing my patience today."),
        Note("note_tired_1", "entry_tired_1", "Stayed up too late.")
    )

    val messages = listOf(
        Message(
            messageId = "msg_sad_ai",
            entryId = "entry_sad_1",
            text = "I'm here for you. What’s troubling you today?",
            isUser = false,
            timestamp = Date().time
        ),
        Message(
            messageId = "msg_sad_user",
            entryId = "entry_sad_1",
            text = "I just feel mentally drained.",
            isUser = true,
            timestamp = Date().time
        )
    )

    val moodScanStats = listOf(
        MoodScanStat(
            statId = "stat_umn",
            userId = "user1",
            date = Date().time,
            dailyScans = 7,
            weekStreak = 3,
            canAccessInsights = true
        )
    )
}
