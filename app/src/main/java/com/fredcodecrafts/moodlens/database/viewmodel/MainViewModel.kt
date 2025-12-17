package com.fredcodecrafts.moodlens.database.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fredcodecrafts.moodlens.database.repository.JournalRepository
import com.fredcodecrafts.moodlens.database.repository.MessagesRepository
import com.fredcodecrafts.moodlens.database.repository.MoodScanStatRepository
import com.fredcodecrafts.moodlens.database.repository.NotesRepository
import kotlinx.coroutines.launch
import android.util.Log

class MainViewModel(
    private val journalRepo: JournalRepository,
    private val messagesRepo: MessagesRepository,
    private val notesRepo: NotesRepository,
    private val moodStatsRepo: MoodScanStatRepository
) : ViewModel() {

    init {
        // Sync on app launch
        syncNow()
    }

    fun syncNow() {
        viewModelScope.launch {
            Log.d("MainViewModel", "Starting Bidirectional Sync...")
            try {
                // Parallel execution could be better, but sequential is safer for now.
                
                // Journals
                journalRepo.fetchAndSyncJournals()
                journalRepo.pushAllJournals()
                
                // Notes
                notesRepo.fetchAndSyncNotes()
                notesRepo.pushAllNotes()
                
                // Mood Stats
                moodStatsRepo.fetchAndSyncMoodStats()
                moodStatsRepo.pushAllStats()
                
                // Messages
                messagesRepo.fetchAndSyncMessages()
                messagesRepo.pushAllMessages()

                Log.d("MainViewModel", "Sync Completed Successfully.")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Sync Failed", e)
            }
        }
    }
}

class MainViewModelFactory(
    private val journalRepo: JournalRepository,
    private val messagesRepo: MessagesRepository,
    private val notesRepo: NotesRepository,
    private val moodStatsRepo: MoodScanStatRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(journalRepo, messagesRepo, notesRepo, moodStatsRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

