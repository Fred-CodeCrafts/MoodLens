package com.fredcodecrafts.moodlens.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fredcodecrafts.moodlens.database.entities.Message

@Dao
interface MessagesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<Message>)

    @Query("SELECT * FROM messages")
    suspend fun getAllMessages(): List<Message>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: Message)

    @Query("SELECT * FROM messages WHERE entryId = :entryId ORDER BY timestamp ASC")
    suspend fun getMessagesForEntry(entryId: String): List<Message>

    @Query("SELECT messages.* FROM messages INNER JOIN journal_entries ON messages.entryId = journal_entries.entryId WHERE journal_entries.userId = :userId")
    suspend fun getMessagesForUser(userId: String): List<Message>
}

