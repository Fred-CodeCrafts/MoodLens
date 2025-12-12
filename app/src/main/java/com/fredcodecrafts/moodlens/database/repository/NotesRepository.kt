package com.fredcodecrafts.moodlens.database.repository


import com.fredcodecrafts.moodlens.database.dao.NotesDao
import com.fredcodecrafts.moodlens.database.entities.Note
import com.fredcodecrafts.moodlens.utils.SupabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotesRepository(
    private val notesDao: NotesDao
) {

    suspend fun insert(note: Note) {
        notesDao.insert(note)
        CoroutineScope(Dispatchers.IO).launch {
             SupabaseClient.upsertNote(note)
        }
    }

    suspend fun insertAll(notes: List<Note>) {
        notesDao.insertAll(notes)
    }

    suspend fun getAllNotes(): List<Note> {
        return notesDao.getAllNotes()
    }

    suspend fun getNotesForEntry(entryId: String): List<Note> {
        return notesDao.getNotesForEntry(entryId)
    }

    suspend fun deleteNotesForEntry(entryId: String) {
        notesDao.deleteNotesByEntryId(entryId)
    }
}
