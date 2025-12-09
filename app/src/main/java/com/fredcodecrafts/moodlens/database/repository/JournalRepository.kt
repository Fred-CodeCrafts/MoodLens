package com.fredcodecrafts.moodlens.database.repository

import com.fredcodecrafts.moodlens.database.dao.JournalDao
import com.fredcodecrafts.moodlens.database.dao.MessagesDao
import com.fredcodecrafts.moodlens.database.dao.NotesDao
import com.fredcodecrafts.moodlens.database.dao.MoodScanStatDao
import com.fredcodecrafts.moodlens.database.entities.JournalEntry
import com.fredcodecrafts.moodlens.database.entities.MoodScanStat
import com.fredcodecrafts.moodlens.database.entities.Note
import com.fredcodecrafts.moodlens.utils.SupabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JournalRepository(
    private val journalDao: JournalDao,
    private val notesDao: NotesDao,
    private val messagesDao: MessagesDao,
    private val moodStatsDao: MoodScanStatDao
) {

    // ------------------- JOURNAL -------------------
    suspend fun insertEntry(entry: JournalEntry) {
        journalDao.insert(entry)
        // Fire-and-forget sync
        CoroutineScope(Dispatchers.IO).launch {
            SupabaseClient.upsertJournal(entry)
        }
    }

    suspend fun insertEntries(entries: List<JournalEntry>) = journalDao.insertAll(entries)

    suspend fun getAllEntries() = journalDao.getAllEntries()

    suspend fun getEntriesForUser(userId: String) = journalDao.getEntriesForUser(userId)

    // ✅ Added this to support Map features
    suspend fun getEntriesWithLocation(userId: String) = journalDao.getEntriesWithLocation(userId)

    suspend fun getEntryById(entryId: String) = journalDao.getEntryById(entryId)

    suspend fun deleteEntry(entryId: String) {
        // Deletes notes associated with the entry first to keep data clean
        notesDao.deleteNotesByEntryId(entryId)
        journalDao.deleteEntry(entryId)
    }


    // -------------------- NOTES ---------------------
    suspend fun getNotesForEntry(entryId: String): List<Note> =
        notesDao.getNotesForEntry(entryId)

    suspend fun getAllNotes(): List<Note> =
        notesDao.getAllNotes()

    suspend fun getNotesForUser(userId: String): List<Note> =
        notesDao.getNotesForUser(userId)

    suspend fun insertNote(note: Note) =
        notesDao.insert(note)


    // -------------------- MOOD SCAN STATS ---------------------
    suspend fun getAllMoodStats(): List<MoodScanStat> =
        moodStatsDao.getAllStats()

    suspend fun getMoodStatsForUser(userId: String): List<MoodScanStat> =
        moodStatsDao.getStatsForUser(userId)

    suspend fun getMoodStatsForUserOnDate(userId: String, date: Long): MoodScanStat? =
        moodStatsDao.getStatForUserOnDate(userId, date)

    suspend fun insertMoodStat(stat: MoodScanStat) =
        moodStatsDao.insert(stat)

    suspend fun insertMoodStats(stats: List<MoodScanStat>) =
        moodStatsDao.insertAll(stats)


    // -------------------- MESSAGES (OPTIONAL) ---------------------
    // Add message-related functions when needed
    
    
    // -------------------- SYNC / BACKUP ---------------------
    // -------------------- SYNC / BACKUP ---------------------

    suspend fun pullFromSupabase() {
        val entries = SupabaseClient.getAllJournals()
        if (entries.isNotEmpty()) journalDao.insertAll(entries)
        
        val notes = SupabaseClient.getAllNotes()
        if (notes.isNotEmpty()) notesDao.insertAll(notes)
        
        val stats = SupabaseClient.getAllMoodStats()
        if (stats.isNotEmpty()) moodStatsDao.insertAll(stats)
        
        val messages = SupabaseClient.getAllMessages()
        if (messages.isNotEmpty()) messagesDao.insertAll(messages)
    }

    suspend fun syncAllData() {
        // Step 1: Pull from Supabase -> Room
        pullFromSupabase()

        // Step 2: Push from Room -> Supabase
        val allEntries = journalDao.getAllEntries()
        allEntries.forEach { SupabaseClient.upsertJournal(it) }

        val allNotes = notesDao.getAllNotes()
        allNotes.forEach { SupabaseClient.upsertNote(it) }

        val allStats = moodStatsDao.getAllStats()
        allStats.forEach { SupabaseClient.upsertMoodScanStat(it) }
        
        val allMessages = messagesDao.getAllMessages()
        allMessages.forEach { SupabaseClient.upsertMessage(it) }
    }
}