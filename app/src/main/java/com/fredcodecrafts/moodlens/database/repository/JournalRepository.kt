package com.fredcodecrafts.moodlens.database.repository

import com.fredcodecrafts.moodlens.database.dao.JournalDao
import com.fredcodecrafts.moodlens.database.dao.MessagesDao
import com.fredcodecrafts.moodlens.database.dao.NotesDao
import com.fredcodecrafts.moodlens.database.dao.MoodScanStatDao
import com.fredcodecrafts.moodlens.database.entities.JournalEntry
import com.fredcodecrafts.moodlens.database.entities.MoodScanStat
import com.fredcodecrafts.moodlens.database.entities.Note
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class JournalRepository(
    private val supabase: SupabaseClient,
    private val journalDao: JournalDao,
    private val notesDao: NotesDao,
    private val messagesDao: MessagesDao,
    private val moodStatsDao: MoodScanStatDao
) {

    // ---------------------------------------------------------------------
    // JOURNAL ENTRIES (Local first → Remote sync)
    // ---------------------------------------------------------------------

    suspend fun insertEntry(entry: JournalEntry) {
        // 1. LOCAL FIRST
        journalDao.insert(entry)

        // 2. REMOTE SYNC (background)
        runCatching {
            supabase.from("journal_entries").insert(
                mapOf(
                    "entry_id" to entry.entryId,
                    "user_id" to entry.userId,
                    "mood" to entry.mood,
                    "timestamp" to entry.timestamp,
                    "location_name" to entry.locationName,
                    "latitude" to entry.latitude,
                    "longitude" to entry.longitude,
                    "ai_reflection" to entry.aiReflection
                )
            )
        }
    }

    suspend fun insertEntries(entries: List<JournalEntry>) {
        journalDao.insertAll(entries)

        runCatching {
            supabase.from("journal_entries").insert(
                entries.map {
                    mapOf(
                        "entry_id" to it.entryId,
                        "user_id" to it.userId,
                        "mood" to it.mood,
                        "timestamp" to it.timestamp,
                        "location_name" to it.locationName,
                        "latitude" to it.latitude,
                        "longitude" to it.longitude,
                        "ai_reflection" to it.aiReflection
                    )
                }
            )
        }
    }

    suspend fun getAllEntries(): List<JournalEntry> {
        val local = journalDao.getAllEntries()

        runCatching {
            val remote = supabase.from("journal_entries")
                .select()
                .decodeList<JournalEntry>()

            journalDao.insertAll(remote)
        }

        return local
    }

    suspend fun getEntriesForUser(userId: String): List<JournalEntry> {
        val local = journalDao.getEntriesForUser(userId)

        runCatching {
            val remote = supabase.from("journal_entries")
                .select()
                .eq("user_id", userId)
                .decodeList<JournalEntry>()

            journalDao.insertAll(remote)
        }

        return local
    }

    suspend fun getEntriesWithLocation(userId: String): List<JournalEntry> =
        journalDao.getEntriesWithLocation(userId)

    suspend fun getEntryById(entryId: String): JournalEntry? =
        journalDao.getEntryById(entryId)

    suspend fun deleteEntry(entryId: String) {
        // LOCAL DELETE
        notesDao.deleteNotesByEntryId(entryId)
        journalDao.deleteEntry(entryId)

        // REMOTE DELETE
        runCatching {
            supabase.from("journal_entries")
                .delete()
                .eq("entry_id", entryId)
        }
    }


    // ---------------------------------------------------------------------
    // NOTES (Local first → Remote sync)
    // ---------------------------------------------------------------------

    suspend fun insertNote(note: Note) {
        notesDao.insert(note)

        runCatching {
            supabase.from("notes").insert(
                mapOf(
                    "note_id" to note.noteId,
                    "entry_id" to note.entryId,
                    "content" to note.content
                )
            )
        }
    }

    suspend fun getNotesForEntry(entryId: String): List<Note> {
        val local = notesDao.getNotesForEntry(entryId)

        runCatching {
            val remote = supabase.from("notes")
                .select()
                .eq("entry_id", entryId)
                .decodeList<Note>()

            notesDao.insertAll(remote)
        }

        return local
    }

    suspend fun getAllNotes(): List<Note> {
        val local = notesDao.getAllNotes()

        runCatching {
            val remote = supabase.from("notes")
                .select()
                .decodeList<Note>()

            notesDao.insertAll(remote)
        }

        return local
    }


    // ---------------------------------------------------------------------
    // MOOD SCAN STATS (Local first → Remote sync)
    // ---------------------------------------------------------------------

    suspend fun insertMoodStat(stat: MoodScanStat) {
        moodStatsDao.insert(stat)

        runCatching {
            supabase.from("mood_scan_stats").insert(
                mapOf(
                    "stat_id" to stat.statId,
                    "user_id" to stat.userId,
                    "date" to stat.date,
                    "daily_scans" to stat.dailyScans,
                    "week_streak" to stat.weekStreak,
                    "can_access_insights" to stat.canAccessInsights
                )
            )
        }
    }

    suspend fun insertMoodStats(stats: List<MoodScanStat>) {
        moodStatsDao.insertAll(stats)

        runCatching {
            supabase.from("mood_scan_stats").insert(
                stats.map {
                    mapOf(
                        "stat_id" to it.statId,
                        "user_id" to it.userId,
                        "date" to it.date,
                        "daily_scans" to it.dailyScans,
                        "week_streak" to it.weekStreak,
                        "can_access_insights" to it.canAccessInsights
                    )
                }
            )
        }
    }

    suspend fun getAllMoodStats(): List<MoodScanStat> {
        val local = moodStatsDao.getAllStats()

        runCatching {
            val remote = supabase.from("mood_scan_stats")
                .select()
                .decodeList<MoodScanStat>()

            moodStatsDao.insertAll(remote)
        }

        return local
    }

    suspend fun getMoodStatsForUserOnDate(userId: String, date: Long): MoodScanStat? {
        val local = moodStatsDao.getStatForUserOnDate(userId, date)

        runCatching {
            val remote = supabase.from("mood_scan_stats")
                .select()
                .eq("user_id", userId)
                .eq("date", date)
                .decodeList<MoodScanStat>()

            if (remote.isNotEmpty()) {
                moodStatsDao.insert(remote.first())
            }
        }

        return local
    }

    // ---------------------------------------------------------------------
    // MESSAGES (You can add sync logic when needed)
    // ---------------------------------------------------------------------
}
