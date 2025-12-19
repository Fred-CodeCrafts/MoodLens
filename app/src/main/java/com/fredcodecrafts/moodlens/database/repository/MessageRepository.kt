package com.fredcodecrafts.moodlens.database.repository



import android.util.Log
import com.fredcodecrafts.moodlens.database.dao.MessagesDao
import com.fredcodecrafts.moodlens.database.entities.Message
import com.fredcodecrafts.moodlens.utils.SessionManager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


class MessagesRepository(
    private val messagesDao: MessagesDao
) {
//    private val client = HttpClient(OkHttp) {
//        install(ContentNegotiation) {
//            json(Json {
//                ignoreUnknownKeys = true
//                isLenient = true
//                encodeDefaults = true
//            })
//        }
//    }

    suspend fun insert(message: Message) {
        Log.d("MessagesRepository", "Inserting local message: ${message.messageId}")
        messagesDao.insert(message)
        
        try {
             upsertMessage(message)
        } catch (e: Exception) {
             Log.e("MessagesRepository", "Remote sync failed, local saved", e)
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

    // ------------------- SUPABASE LOGIC -------------------

    // ------------------- FIREBASE REALTIME DB SYNC -------------------
    private val firebaseDb = com.google.firebase.database.FirebaseDatabase.getInstance()
    private val messagesRef = firebaseDb.getReference("messages")

    private fun upsertMessage(message: Message) {
        val userId = SessionManager.currentUserId ?: return
        messagesRef.child(userId).child(message.messageId).setValue(message)
             .addOnSuccessListener {
                 Log.d("MessagesRepository", "Message synced to Firebase")
             }
             .addOnFailureListener { e ->
                 Log.e("MessagesRepository", "Failed to sync message", e)
             }
    }

    suspend fun fetchAndSyncMessages() {
        val userId = SessionManager.currentUserId ?: return
        
        messagesRef.child(userId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val messages = mutableListOf<Message>()
                for (child in snapshot.children) {
                    try {
                        val message = child.getValue(Message::class.java)
                        if (message != null) messages.add(message)
                    } catch (e: Exception) {
                        Log.e("MessagesRepository", "Error parsing message: ${e.message}")
                    }
                }
                if (messages.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        messagesDao.insertAll(messages)
                    }
                }
            }
        }.addOnFailureListener { e ->
            Log.e("MessagesRepository", "Error fetching messages", e)
        }
    }

    suspend fun pushAllMessages() {
        val userId = SessionManager.currentUserId ?: return
        val allMessages = messagesDao.getMessagesForUser(userId)
        allMessages.forEach { upsertMessage(it) }
    }



    @Serializable
    private data class RemoteMessage(
        val message_id: String,
        val entry_id: String,
        val text: String,
        val is_user: Boolean,
        val timestamp: Long
    )
}

