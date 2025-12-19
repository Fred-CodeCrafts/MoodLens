package com.fredcodecrafts.moodlens.database.repository



import android.util.Log
import com.fredcodecrafts.moodlens.database.dao.NotesDao
import com.fredcodecrafts.moodlens.database.entities.Note
import com.fredcodecrafts.moodlens.utils.SessionManager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


class NotesRepository(
    private val notesDao: NotesDao
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

    suspend fun insert(note: Note) {
        Log.d("NotesRepository", "Inserting local note: ${note.noteId}")
        notesDao.insert(note)
        Log.d("NotesRepository", "Local insert OK: ${note.noteId}")
        
        try {
             upsertNote(note)
        } catch (e: Exception) {
             Log.e("NotesRepository", "Remote sync failed, local saved", e)
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

    suspend fun getNotesForUser(userId: String): List<Note> {
        return notesDao.getNotesForUser(userId)
    }

    suspend fun deleteNotesForEntry(entryId: String) {
        notesDao.deleteNotesByEntryId(entryId)
    }

    // Logic to ensure ONE note per entry (Overwrite if exists)
    suspend fun saveNoteForEntry(entryId: String, content: String) {
        val existingNotes = notesDao.getNotesForEntry(entryId)
        if (existingNotes.isNotEmpty()) {
            // Overwrite the first existing note
            // We use the SAME noteId to ensure we update the existing record
            val existing = existingNotes.first()
            val updated = existing.copy(content = content)
            insert(updated) // This calls dao.insert(REPLACE) + sync
            
            // Clean up duplicates if any (though usually shouldn't happen with this logic)
            if (existingNotes.size > 1) {
                // Delete others? For now, just focus on the first one.
                // Ideally we delete others, but let's stick to safe update.
            }
        } else {
            // Create new
            val newNote = Note(
                noteId = java.util.UUID.randomUUID().toString(),
                entryId = entryId,
                content = content
            )
            insert(newNote)
        }
    }

    // ------------------- SUPABASE LOGIC -------------------

    // ------------------- FIREBASE SYNC -------------------
    private val firebaseDb = com.google.firebase.database.FirebaseDatabase.getInstance()
    private val notesRef = firebaseDb.getReference("notes")

    private fun upsertNote(note: Note) {
        val userId = SessionManager.currentUserId ?: return
        // Path: notes/{userId}/{noteId}
        notesRef.child(userId).child(note.noteId).setValue(note)
             .addOnSuccessListener {
                 Log.d("NotesRepository", "Note synced to Firebase")
             }
             .addOnFailureListener { e ->
                 Log.e("NotesRepository", "Failed to sync note", e)
             }
    }

    suspend fun fetchAndSyncNotes() {
        val userId = SessionManager.currentUserId ?: return
        
        notesRef.child(userId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val notes = mutableListOf<Note>()
                for (child in snapshot.children) {
                    try {
                        val note = child.getValue(Note::class.java)
                        if (note != null) notes.add(note)
                    } catch (e: Exception) {
                        Log.e("NotesRepository", "Error parsing note: ${e.message}")
                    }
                }
                if (notes.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        notesDao.insertAll(notes)
                    }
                }
            }
        }.addOnFailureListener { e ->
            Log.e("NotesRepository", "Error fetching notes", e)
        }
    }

    suspend fun pushAllNotes() {
        val userId = SessionManager.currentUserId ?: return
        val allNotes = notesDao.getNotesForUser(userId)
        allNotes.forEach { upsertNote(it) }
    }



    @Serializable
    private data class RemoteNote(
        val note_id: String,
        val entry_id: String,
        val content: String
    )
}

