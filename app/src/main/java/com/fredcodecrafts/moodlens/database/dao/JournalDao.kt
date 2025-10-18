package com.fredcodecrafts.moodlens.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fredcodecrafts.moodlens.database.entities.JournalEntry

@Dao
interface JournalDao {

    // ✅ Insert or update a single entry
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: JournalEntry)

    // ✅ Insert or update multiple entries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<JournalEntry>)

    // ✅ Get all journal entries
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    suspend fun getAllEntries(): List<JournalEntry>

    // ✅ Get all entries for a specific user
    @Query("SELECT * FROM journal_entries WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getEntriesForUser(userId: String): List<JournalEntry>

    // ✅ Optional: Get a single entry by ID (useful for edit/detail)
    @Query("SELECT * FROM journal_entries WHERE entryId = :entryId LIMIT 1")
    suspend fun getEntryById(entryId: String): JournalEntry?

    // ✅ Optional: Delete a specific entry
    @Query("DELETE FROM journal_entries WHERE entryId = :entryId")
    suspend fun deleteEntry(entryId: String)
}
