package com.fredcodecrafts.moodlens.utils

import android.util.Log
import com.fredcodecrafts.moodlens.database.entities.JournalEntry
import com.fredcodecrafts.moodlens.database.entities.Message
import com.fredcodecrafts.moodlens.database.entities.Note
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object SupabaseClient {
    private const val SUPABASE_URL = "https://cglkbjwuvmakmamkcfww.supabase.co"
    private const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNnbGtiand1dm1ha21hbWtjZnd3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjMyOTM1ODYsImV4cCI6MjA3ODg2OTU4Nn0.Yt2I8ELwfUT3sKD9PEMy5JgNGAbhnZ_gCXRN-m2a5Y8"

    val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
    }

    // ------------------- UPSERT (PUSH) -------------------
    suspend fun upsertJournal(entry: JournalEntry) {
        val token = SessionManager.accessToken ?: return
        try {
            val response = client.post("$SUPABASE_URL/rest/v1/journal_entries") {
                header("Authorization", "Bearer $token")
                header("apikey", ANON_KEY)
                header("Prefer", "resolution=merge-duplicates")
                contentType(ContentType.Application.Json)
                setBody(
                    RemoteJournalEntry(
                        entry_id = entry.entryId,
                        user_id = entry.userId,
                        mood = entry.mood,
                        timestamp = entry.timestamp,
                        location_name = entry.locationName,
                        latitude = entry.latitude,
                        longitude = entry.longitude,
                        ai_reflection = entry.aiReflection
                    )
                )
            }
            if (response.status.value !in 200..299) {
                Log.e("SupabaseClient", "Failed to sync journal: ${response.bodyAsText()}")
            } else {
                Log.d("SupabaseClient", "Journal synced: ${entry.entryId}")
            }
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Error syncing journal", e)
        }
    }

    suspend fun upsertNote(note: Note) {
        val token = SessionManager.accessToken ?: return
        try {
            val response = client.post("$SUPABASE_URL/rest/v1/notes") {
                header("Authorization", "Bearer $token")
                header("apikey", ANON_KEY)
                header("Prefer", "resolution=merge-duplicates")
                contentType(ContentType.Application.Json)
                setBody(
                    RemoteNote(
                        note_id = note.noteId,
                        entry_id = note.entryId,
                        content = note.content
                    )
                )
            }
            if (response.status.value !in 200..299) {
                Log.e("SupabaseClient", "Failed to sync note: ${response.bodyAsText()}")
            } else {
                Log.d("SupabaseClient", "Note synced.")
            }
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Error syncing note", e)
        }
    }

    suspend fun upsertMessage(message: Message) {
        val token = SessionManager.accessToken ?: return
        try {
            val response = client.post("$SUPABASE_URL/rest/v1/messages") {
                header("Authorization", "Bearer $token")
                header("apikey", ANON_KEY)
                header("Prefer", "resolution=merge-duplicates")
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
                 Log.e("SupabaseClient", "Failed to sync message: ${response.bodyAsText()}")
            } else {
                 Log.d("SupabaseClient", "Message synced.")
            }
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Error syncing message", e)
        }
    }

    suspend fun upsertMoodScanStat(stat: com.fredcodecrafts.moodlens.database.entities.MoodScanStat) {
        val token = SessionManager.accessToken ?: return
        try {
            val response = client.post("$SUPABASE_URL/rest/v1/mood_scan_stats") {
                header("Authorization", "Bearer $token")
                header("apikey", ANON_KEY)
                header("Prefer", "resolution=merge-duplicates")
                contentType(ContentType.Application.Json)
                setBody(
                    RemoteMoodScanStat(
                        stat_id = stat.statId,
                        user_id = stat.userId,
                        date = stat.date,
                        daily_scans = stat.dailyScans,
                        week_streak = stat.weekStreak,
                        can_access_insights = stat.canAccessInsights
                    )
                )
            }
            if (response.status.value !in 200..299) {
                 Log.e("SupabaseClient", "Failed to sync stat: ${response.bodyAsText()}")
            } else {
                 Log.d("SupabaseClient", "Stat synced.")
            }
        } catch (e: Exception) {
             Log.e("SupabaseClient", "Error syncing stat", e)
        }
    }

    // ------------------- FETCH (PULL) -------------------
    suspend fun getAllJournals(): List<JournalEntry> {
        val token = SessionManager.accessToken ?: return emptyList()
        return try {
            val response = client.get("$SUPABASE_URL/rest/v1/journal_entries?select=*") {
                header("Authorization", "Bearer $token")
                header("apikey", ANON_KEY)
            }
            val remoteList: List<RemoteJournalEntry> = response.body()
            remoteList.map {
                JournalEntry(
                    entryId = it.entry_id,
                    userId = it.user_id,
                    mood = it.mood,
                    timestamp = it.timestamp,
                    locationName = it.location_name,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    aiReflection = it.ai_reflection
                )
            }
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Error fetching journals", e)
            emptyList()
        }
    }

    suspend fun getAllNotes(): List<Note> {
        val token = SessionManager.accessToken ?: return emptyList()
        return try {
            val response = client.get("$SUPABASE_URL/rest/v1/notes?select=*") {
                header("Authorization", "Bearer $token")
                header("apikey", ANON_KEY)
            }
            val remoteList: List<RemoteNote> = response.body()
             remoteList.map {
                Note(
                    noteId = it.note_id,
                    entryId = it.entry_id,
                    content = it.content
                )
            }
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Error fetching notes", e)
            emptyList()
        }
    }

    suspend fun getAllMessages(): List<Message> {
        val token = SessionManager.accessToken ?: return emptyList()
        return try {
            val response = client.get("$SUPABASE_URL/rest/v1/messages?select=*") {
                header("Authorization", "Bearer $token")
                header("apikey", ANON_KEY)
            }
            val remoteList: List<RemoteMessage> = response.body()
            remoteList.map {
                Message(
                    messageId = it.message_id,
                    entryId = it.entry_id,
                    text = it.text,
                    isUser = it.is_user,
                    timestamp = it.timestamp
                )
            }
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Error fetching messages", e)
            emptyList()
        }
    }

     suspend fun getAllMoodStats(): List<com.fredcodecrafts.moodlens.database.entities.MoodScanStat> {
        val token = SessionManager.accessToken ?: return emptyList()
        return try {
            val response = client.get("$SUPABASE_URL/rest/v1/mood_scan_stats?select=*") {
                header("Authorization", "Bearer $token")
                header("apikey", ANON_KEY)
            }
            val remoteList: List<RemoteMoodScanStat> = response.body()
            remoteList.map {
                com.fredcodecrafts.moodlens.database.entities.MoodScanStat(
                    statId = it.stat_id,
                    userId = it.user_id,
                    date = it.date,
                    dailyScans = it.daily_scans,
                    weekStreak = it.week_streak,
                    canAccessInsights = it.can_access_insights
                )
            }
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Error fetching stats", e)
            emptyList()
        }
    }


    @Serializable
    private data class RemoteJournalEntry(
        val entry_id: String,
        val user_id: String,
        val mood: String,
        val timestamp: Long,
        val location_name: String?,
        val latitude: Double?,
        val longitude: Double?,
        val ai_reflection: String?
    )

    @Serializable
    private data class RemoteNote(
        val note_id: String,
        val entry_id: String,
        val content: String
    )

    @Serializable
    private data class RemoteMoodScanStat(
        val stat_id: String,
        val user_id: String,
        val date: Long,
        val daily_scans: Int,
        val week_streak: Int,
        val can_access_insights: Boolean
    )

    @Serializable
    private data class RemoteMessage(
        val message_id: String,
        val entry_id: String,
        val text: String,
        val is_user: Boolean,
        val timestamp: Long
    )
}
