package com.fredcodecrafts.moodlens.database.repository


import com.fredcodecrafts.moodlens.database.dao.MessagesDao
import com.fredcodecrafts.moodlens.database.entities.Message

class MessagesRepository(
    private val messagesDao: MessagesDao
) {
    suspend fun insert(message: Message) {
        messagesDao.insert(message)
    }

    suspend fun insertAll(messages: List<Message>) {
        messagesDao.insertAll(messages)
    }

    suspend fun getMessagesForEntry(entryId: String): List<Message> {
        return messagesDao.getMessagesForEntry(entryId)
    }

    suspend fun getAllMessages(): List<Message> {
        return messagesDao.getAllMessages()
    }
}
