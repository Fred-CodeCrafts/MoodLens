package com.fredcodecrafts.moodlens.database.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fredcodecrafts.moodlens.database.entities.JournalEntry
import com.fredcodecrafts.moodlens.database.entities.MoodScanStat
import com.fredcodecrafts.moodlens.database.repository.JournalRepository
import com.fredcodecrafts.moodlens.database.repository.MoodScanStatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CameraScanViewModel(
    private val journalRepo: JournalRepository,
    private val statsRepo: MoodScanStatRepository,
    private val userId: String
) : ViewModel() {

    private val _detectedEmotion = MutableStateFlow<String?>(null)
    val detectedEmotion = _detectedEmotion.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress = _scanProgress.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    // ---------------------- SIMULATED SCAN LOGIC ----------------------
    fun startScan() {
        viewModelScope.launch {
            _isScanning.value = true
            _scanProgress.value = 0f

            repeat(20) {
                kotlinx.coroutines.delay(75)
                _scanProgress.value += 0.05f
            }

            // TODO replace with MLKit or TensorFlow later
            val finalEmotion = listOf("happy", "sad", "anxious", "calm", "excited", "tired").random()

            _detectedEmotion.value = finalEmotion
            _isScanning.value = false

            saveScanResult(finalEmotion)
        }
    }

    // ---------------------- DATABASE WRITE ----------------------
    private fun saveScanResult(emotion: String) {
        viewModelScope.launch {

            // Insert Journal Entry
            val entry = JournalEntry(
                entryId = UUID.randomUUID().toString(),
                userId = userId,
                mood = emotion,
                timestamp = System.currentTimeMillis()
            )
            journalRepo.insertEntry(entry)

            // Insert / Update Stats
            val today = getTodayMillis()
            val existingStat = statsRepo.getStatForUserOnDate(userId, today)

            if (existingStat == null) {
                statsRepo.insert(
                    MoodScanStat(
                        statId = UUID.randomUUID().toString(),
                        userId = userId,
                        date = today,
                        dailyScans = 1,
                        weekStreak = 1,
                        canAccessInsights = true
                    )
                )
            } else {
                statsRepo.insert(
                    existingStat.copy(
                        dailyScans = existingStat.dailyScans + 1,
                        weekStreak = existingStat.weekStreak + 1
                    )
                )
            }
        }
    }

    private fun getTodayMillis(): Long {
        val now = java.time.LocalDate.now()
        return now.toEpochDay()
    }

    fun resetScan() {
        _detectedEmotion.value = null
        _scanProgress.value = 0f
        _isScanning.value = false
    }

    // -------------------------------------------------------------------
    // FACTORY  (inside same file & package)
    // -------------------------------------------------------------------
    class Factory(
        private val journalRepo: JournalRepository,
        private val statsRepo: MoodScanStatRepository,
        private val userId: String
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CameraScanViewModel::class.java)) {
                return CameraScanViewModel(journalRepo, statsRepo, userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
