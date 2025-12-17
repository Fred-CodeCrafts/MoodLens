package com.fredcodecrafts.moodlens.database.repository



import android.util.Log
import com.fredcodecrafts.moodlens.database.dao.NotesDao
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class NotesRepository(
    private val notesDao: NotesDao
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

    private suspend fun upsertNote(note: Note) {
        val token = SessionManager.accessToken ?: return
        try {
            val response = client.post("${SupabaseConfig.SUPABASE_URL}/rest/v1/${SupabaseConfig.SCHEMA}.notes") {
                header("Authorization", "Bearer $token")
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Prefer", "resolution=merge-duplicates")
                header("Accept-Profile", SupabaseConfig.SCHEMA)
                header("Content-Profile", SupabaseConfig.SCHEMA)
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
                Log.e("NotesRepository", "Failed to sync note: ${response.bodyAsText()}")
            } else {
                Log.d("NotesRepository", "Note synced.")
            }
        } catch (e: Exception) {
            Log.e("NotesRepository", "Error syncing note", e)
        }
    }

    suspend fun fetchAndSyncNotes() {
        val token = SessionManager.accessToken ?: return
        try {
            val response = client.get("${SupabaseConfig.SUPABASE_URL}/rest/v1/${SupabaseConfig.SCHEMA}.notes?select=*") {
                header("Authorization", "Bearer $token")
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Accept-Profile", SupabaseConfig.SCHEMA)
            }
            val remoteList: List<RemoteNote> = response.body()
             val notes = remoteList.map {
                Note(
                    noteId = it.note_id,
                    entryId = it.entry_id,
                    content = it.content
                )
            }
            if (notes.isNotEmpty()) {
                notesDao.insertAll(notes)
            }
        } catch (e: Exception) {
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

