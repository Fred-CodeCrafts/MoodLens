package com.fredcodecrafts.moodlens.database.repository


import com.fredcodecrafts.moodlens.database.dao.MessagesDao
import com.fredcodecrafts.moodlens.database.entities.Message
import com.fredcodecrafts.moodlens.utils.SupabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessagesRepository(
    private val messagesDao: MessagesDao
) {
    suspend fun insert(message: Message) {
        messagesDao.insert(message)
        CoroutineScope(Dispatchers.IO).launch {
             SupabaseClient.upsertMessage(message)
        }
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
