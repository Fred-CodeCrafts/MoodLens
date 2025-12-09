package com.fredcodecrafts.moodlens.database.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fredcodecrafts.moodlens.database.repository.JournalRepository
import kotlinx.coroutines.launch
import android.util.Log

class MainViewModel(
    private val journalRepo: JournalRepository
) : ViewModel() {

    init {
        // Sync on app launch
        syncNow()
    }

    fun syncNow() {
        viewModelScope.launch {
            Log.d("MainViewModel", "Starting Bidirectional Sync...")
            try {
                journalRepo.syncAllData()
                Log.d("MainViewModel", "Sync Completed Successfully.")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Sync Failed", e)
            }
        }
    }
}

class MainViewModelFactory(
    private val journalRepo: JournalRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(journalRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
