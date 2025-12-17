package com.fredcodecrafts.moodlens.database.repository


import android.util.Log
import com.fredcodecrafts.moodlens.database.dao.JournalDao
import com.fredcodecrafts.moodlens.database.dao.MessagesDao
import com.fredcodecrafts.moodlens.database.dao.MoodScanStatDao
import com.fredcodecrafts.moodlens.database.dao.NotesDao
import com.fredcodecrafts.moodlens.database.entities.JournalEntry
import com.fredcodecrafts.moodlens.database.entities.MoodScanStat
import com.fredcodecrafts.moodlens.database.entities.Note
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
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class JournalRepository(
    private val journalDao: JournalDao,
    private val notesDao: NotesDao,
    private val messagesDao: MessagesDao,
    private val moodStatsDao: MoodScanStatDao
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

    // ------------------- JOURNAL -------------------
    suspend fun insertEntry(entry: JournalEntry) {
        Log.d("JournalRepository", "Inserting local journal: ${entry.entryId}")
        journalDao.insert(entry)
        Log.d("JournalRepository", "Local insert OK: ${entry.entryId}")
        
        try {
            upsertJournal(entry)
        } catch (e: Exception) {
            Log.e("JournalRepository", "Remote sync failed, local saved", e)
        }
    }

    suspend fun insertEntries(entries: List<JournalEntry>) = journalDao.insertAll(entries)
    suspend fun getAllEntries() = journalDao.getAllEntries()
    suspend fun getEntriesForUser(userId: String) = journalDao.getEntriesForUser(userId)
    suspend fun getEntriesWithLocation(userId: String) = journalDao.getEntriesWithLocation(userId)
    suspend fun getEntryById(entryId: String) = journalDao.getEntryById(entryId)

    suspend fun deleteEntry(entryId: String) {
        // Deletes notes associated with the entry first to keep data clean
        notesDao.deleteNotesByEntryId(entryId)
        journalDao.deleteEntry(entryId)
    }

    // -------------------- NOTES (Pass-through to DAO) ---------------------
    suspend fun getNotesForEntry(entryId: String): List<Note> = notesDao.getNotesForEntry(entryId)
    suspend fun getAllNotes(): List<Note> = notesDao.getAllNotes()
    suspend fun getNotesForUser(userId: String): List<Note> = notesDao.getNotesForUser(userId)
    suspend fun insertNote(note: Note) = notesDao.insert(note) // Note: Sync should happen in NotesRepository now

    // -------------------- MOOD SCAN STATS (Pass-through to DAO) ---------------------
    suspend fun getAllMoodStats(): List<MoodScanStat> = moodStatsDao.getAllStats()
    suspend fun getMoodStatsForUser(userId: String): List<MoodScanStat> = moodStatsDao.getStatsForUser(userId)
    suspend fun getMoodStatsForUserOnDate(userId: String, date: Long): MoodScanStat? = moodStatsDao.getStatForUserOnDate(userId, date)
    suspend fun insertMoodStat(stat: MoodScanStat) = moodStatsDao.insert(stat) // Note: Sync should happen in MoodScanStatRepository now
    suspend fun insertMoodStats(stats: List<MoodScanStat>) = moodStatsDao.insertAll(stats)


    // -------------------- SUPABASE SYNC (JOURNAL ONLY) ---------------------

    private suspend fun upsertJournal(entry: JournalEntry) {
        val token = SessionManager.accessToken ?: return
        try {
            val response = client.post("${SupabaseConfig.SUPABASE_URL}/rest/v1/${SupabaseConfig.SCHEMA}.journal_entries") {
                header("Authorization", "Bearer $token")
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Prefer", "resolution=merge-duplicates")
                // Schema headers
                header("Accept-Profile", SupabaseConfig.SCHEMA)
                header("Content-Profile", SupabaseConfig.SCHEMA)
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
                Log.e("JournalRepository", "Failed to sync journal: ${response.bodyAsText()}")
            } else {
                Log.d("JournalRepository", "Journal synced: ${entry.entryId}")
            }
        } catch (e: Exception) {
            Log.e("JournalRepository", "Error syncing journal", e)
        }
    }

    suspend fun fetchAndSyncJournals() {
        val token = SessionManager.accessToken ?: return
        try {
            val response = client.get("${SupabaseConfig.SUPABASE_URL}/rest/v1/${SupabaseConfig.SCHEMA}.journal_entries?select=*") {
                header("Authorization", "Bearer $token")
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Accept-Profile", SupabaseConfig.SCHEMA)
            }
            val remoteList: List<RemoteJournalEntry> = response.body()
            val entries = remoteList.map {
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
            if (entries.isNotEmpty()) {
                journalDao.insertAll(entries)
            }
        } catch (e: Exception) {
            Log.e("JournalRepository", "Error fetching journals", e)
        }
    }

    suspend fun pushAllJournals() {
        val userId = SessionManager.currentUserId ?: return
        val allEntries = journalDao.getEntriesForUser(userId)
        allEntries.forEach { upsertJournal(it) }
    }



    // Deprecated methods to avoid compilation errors during refactor transition,
    // but MainViewModel will be updated to use specific repos.
    suspend fun syncAllData() {
        // Ideally this should be empty or strictly sync journals,
        // but we will move orchestration to MainViewModel.
        fetchAndSyncJournals()
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
}