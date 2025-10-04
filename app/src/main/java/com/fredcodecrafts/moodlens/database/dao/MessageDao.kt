package com.fredcodecrafts.moodlens.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fredcodecrafts.moodlens.database.entities.Message

@Dao
interface MessagesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: Message)

    @Query("SELECT * FROM messages_offline WHERE entryId = :entryId ORDER BY timestamp ASC")
    suspend fun getMessagesForEntry(entryId: String): List<Message>
}
