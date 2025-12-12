package com.fredcodecrafts.moodlens.database.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fredcodecrafts.moodlens.database.entities.JournalEntry
import com.fredcodecrafts.moodlens.database.entities.MoodScanStat
import com.fredcodecrafts.moodlens.database.entities.Note
import com.fredcodecrafts.moodlens.database.repository.JournalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JournalViewModel(
    private val journalRepository: JournalRepository,
    private val userId: String
) : ViewModel() {

    // ---------------------- STATE ----------------------
    private val _entries = MutableStateFlow<List<JournalEntry>>(emptyList())
    val entries = _entries.asStateFlow()

    private val _notesMap = MutableStateFlow<Map<String, List<Note>>>(emptyMap())
    val notesMap = _notesMap.asStateFlow()

    private val _stats = MutableStateFlow<List<MoodScanStat>>(emptyList())
    val stats = _stats.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()


    // ---------------------- INIT LOAD ----------------------
    init {
        refreshAll()
    }


    // ---------------------- LOAD EVERYTHING ----------------------
    fun refreshAll() {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true

            val userEntries = journalRepository.getEntriesForUser(userId)
            _entries.value = userEntries

            val allNotes = journalRepository.getNotesForUser(userId)
            _notesMap.value = allNotes.groupBy { it.entryId }

            _stats.value = journalRepository.getMoodStatsForUser(userId)

            _loading.value = false
        }
    }


    // ---------------------- ADD NOTE ----------------------
    fun addNote(entryId: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newNote = Note(
                noteId = java.util.UUID.randomUUID().toString(),
                entryId = entryId,
                content = content.trim()
            )

            journalRepository.insertNote(newNote)

            // update notes group
            val updated = _notesMap.value.toMutableMap()
            updated[entryId] = (updated[entryId] ?: emptyList()) + newNote
            _notesMap.value = updated
        }
    }


    // ---------------------- DELETE ENTRY ----------------------
    fun deleteEntry(entryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            journalRepository.deleteEntry(entryId)

            _entries.value = _entries.value.filterNot { it.entryId == entryId }

            val updatedMap = _notesMap.value.toMutableMap()
            updatedMap.remove(entryId)
            _notesMap.value = updatedMap
        }
    }


    // ---------------------- BACKUP / SYNC ----------------------
    fun backupData() {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true // Optional: show loading indicator during sync
            try {
                journalRepository.syncAllData()
                // Done syncing
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }


    // ---------------------- FACTORY INSIDE ----------------------
    class Factory(
        private val repository: JournalRepository,
        private val userId: String
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(JournalViewModel::class.java)) {
                return JournalViewModel(repository, userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.simpleName}")
        }
    }
}
