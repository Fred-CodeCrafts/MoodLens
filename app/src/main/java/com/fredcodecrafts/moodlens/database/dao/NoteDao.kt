package com.fredcodecrafts.moodlens.database.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fredcodecrafts.moodlens.database.entities.Note

@Dao
interface NotesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<Note>)

    @Query("SELECT * FROM notes")
    suspend fun getAllNotes(): List<Note>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Query("SELECT * FROM notes WHERE entryId = :entryId")
    suspend fun getNotesForEntry(entryId: String): List<Note>
}
