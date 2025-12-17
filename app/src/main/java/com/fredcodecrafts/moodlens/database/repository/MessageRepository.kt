package com.fredcodecrafts.moodlens.database.repository



import android.util.Log
import com.fredcodecrafts.moodlens.database.dao.MessagesDao
import com.fredcodecrafts.moodlens.database.entities.Message
import com.fredcodecrafts.moodlens.utils.SessionManager
import com.fredcodecrafts.moodlens.utils.SupabaseConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class MessagesRepository(
    private val messagesDao: MessagesDao
) {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
    }

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

    private suspend fun upsertMessage(message: Message) {
        val token = SessionManager.accessToken ?: return
        try {
            val response = client.post("${SupabaseConfig.SUPABASE_URL}/rest/v1/${SupabaseConfig.SCHEMA}.messages") {
                header("Authorization", "Bearer $token")
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Prefer", "resolution=merge-duplicates")
                header("Accept-Profile", SupabaseConfig.SCHEMA)
                header("Content-Profile", SupabaseConfig.SCHEMA)
                contentType(ContentType.Application.Json)
                setBody(
                    RemoteMessage(
                        message_id = message.messageId,
                        entry_id = message.entryId,
                        text = message.text,
                        is_user = message.isUser,
                        timestamp = message.timestamp
                    )
                )
            }
             if (response.status.value !in 200..299) {
                 Log.e("MessagesRepository", "Failed to sync message: ${response.bodyAsText()}")
            } else {
                 Log.d("MessagesRepository", "Message synced.")
            }
        } catch (e: Exception) {
            Log.e("MessagesRepository", "Error syncing message", e)
        }
    }

    suspend fun fetchAndSyncMessages() {
        val token = SessionManager.accessToken ?: return
        try {
            val response = client.get("${SupabaseConfig.SUPABASE_URL}/rest/v1/${SupabaseConfig.SCHEMA}.messages?select=*") {
                header("Authorization", "Bearer $token")
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Accept-Profile", SupabaseConfig.SCHEMA)
            }
            val remoteList: List<RemoteMessage> = response.body()
            val messages = remoteList.map {
                Message(
                    messageId = it.message_id,
                    entryId = it.entry_id,
                    text = it.text,
                    isUser = it.is_user,
                    timestamp = it.timestamp
                )
            }
            if (messages.isNotEmpty()) {
                messagesDao.insertAll(messages)
            }
        } catch (e: Exception) {
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

