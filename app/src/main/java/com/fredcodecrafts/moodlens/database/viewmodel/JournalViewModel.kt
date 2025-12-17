package com.fredcodecrafts.moodlens.database.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fredcodecrafts.moodlens.database.entities.JournalEntry
import com.fredcodecrafts.moodlens.database.entities.MoodScanStat
import com.fredcodecrafts.moodlens.database.entities.Note
import com.fredcodecrafts.moodlens.database.repository.JournalRepository
import com.fredcodecrafts.moodlens.database.repository.NotesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JournalViewModel(
    private val journalRepository: JournalRepository,
    private val notesRepository: NotesRepository, // Injected
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

            // Use NotesRepo for fetching
            val allNotes = notesRepository.getNotesForUser(userId) // Ensure this exists in NotesRepo or add it
            _notesMap.value = allNotes.groupBy { it.entryId }

            _stats.value = journalRepository.getMoodStatsForUser(userId)

            _loading.value = false
        }
    }


    // ---------------------- ADD NOTE (Upsert/Overwrite) ----------------------
    fun addNote(entryId: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Use the new single-note logic
            notesRepository.saveNoteForEntry(entryId, content.trim())

            // Refetch notes to update UI accurately (since we might have updated existing)
            // Or just fetch for this entry
            val updatedNotes = notesRepository.getNotesForUser(userId)
            _notesMap.value = updatedNotes.groupBy { it.entryId }
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
            _loading.value = true
            try {
                // Trigger sync on all repos
                journalRepository.pushAllJournals()
                notesRepository.pushAllNotes()
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
        private val journalRepo: JournalRepository,
        private val notesRepo: NotesRepository,
        private val userId: String
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(JournalViewModel::class.java)) {
                return JournalViewModel(journalRepo, notesRepo, userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.simpleName}")
        }
    }
}
