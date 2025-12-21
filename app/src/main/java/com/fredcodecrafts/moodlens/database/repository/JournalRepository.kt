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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


class JournalRepository(
    private val journalDao: JournalDao,
    private val notesDao: NotesDao,
    private val messagesDao: MessagesDao,
    private val moodStatsDao: MoodScanStatDao
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

    // -------------------- FIREBASE REALTIME DB SYNC ---------------------

    private val firebaseDb = com.google.firebase.database.FirebaseDatabase.getInstance()
    private val journalRef = firebaseDb.getReference("journals")

    private fun upsertJournal(entry: JournalEntry) {
        val userId = SessionManager.currentUserId
        if (userId == null) {
            Log.e("JournalRepository", "SKIPPING SYNC: User ID is null!")
            return
        }
        // Path: journals/{userId}/{entryId}
        journalRef.child(userId).child(entry.entryId).setValue(entry)
            .addOnSuccessListener {
                Log.d("JournalRepository", "Journal synced to Firebase: ${entry.entryId}")
            }
            .addOnFailureListener { e ->
                Log.e("JournalRepository", "Failed to sync journal to Firebase", e)
            }
    }

    suspend fun fetchAndSyncJournals() {
         // This is a one-time fetch. For Realtime, a ValueEventListener is better.
         // But for compatibility with existing architecture:
         val userId = SessionManager.currentUserId ?: return
         
         journalRef.child(userId).get().addOnSuccessListener { snapshot ->
             if (snapshot.exists()) {
                 val entries = mutableListOf<JournalEntry>()
                 for (child in snapshot.children) {
                     // Requires JournalEntry to have a no-arg constructor or be compatible.
                     // Safe way: Manual mapping or simpler mapping.
                     // Trying automatic mapping first.
                     try {
                         val entry = child.getValue(JournalEntry::class.java)
                         if (entry != null) {
                             entries.add(entry)
                         }
                     } catch (e: Exception) {
                         Log.e("JournalRepository", "Error parsing journal entry: ${e.message}")
                     }
                 }
                 
                 if (entries.isNotEmpty()) {
                     // Run DB op in coroutine
                     kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                         journalDao.insertAll(entries)
                         Log.d("JournalRepository", "Synced ${entries.size} journals from Firebase")
                     }
                 }
             }
         }.addOnFailureListener { e ->
             Log.e("JournalRepository", "Failed to fetch journals from Firebase", e)
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