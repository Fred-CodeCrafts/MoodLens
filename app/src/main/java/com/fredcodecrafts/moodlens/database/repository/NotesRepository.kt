package com.fredcodecrafts.moodlens.database.repository


import com.fredcodecrafts.moodlens.database.dao.NotesDao
import com.fredcodecrafts.moodlens.database.entities.Note

class NotesRepository(
    private val notesDao: NotesDao
) {

    suspend fun insert(note: Note) {
        notesDao.insert(note)
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
