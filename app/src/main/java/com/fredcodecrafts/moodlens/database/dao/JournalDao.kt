package com.fredcodecrafts.moodlens.database.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fredcodecrafts.moodlens.database.entities.JournalEntry

@Dao
interface JournalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: JournalEntry)

    @Query("SELECT * FROM journal_entries_offline WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getEntriesForUser(userId: String): List<JournalEntry>
}
