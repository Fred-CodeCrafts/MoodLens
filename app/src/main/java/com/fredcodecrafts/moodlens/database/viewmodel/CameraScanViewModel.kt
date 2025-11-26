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


    // -------------------------------------------------------------------------
    // EXTERNAL CONTROL FUNCTIONS FOR ANALYZER (NEW)
    // -------------------------------------------------------------------------

    /** Called from FaceEmotionAnalyzer — sets the emotion text */
    fun setDetectedEmotion(emotion: String?) {
        _detectedEmotion.value = emotion
    }

    /** Called from analyzer — updates progress circle */
    fun setScanProgress(progress: Float) {
        _scanProgress.value = progress
    }

    /** Called from analyzer — start/stop animation state */
    fun setScanningActive(active: Boolean) {
        _isScanning.value = active
    }



    // ---------------------- SIMULATED SCAN LOGIC ----------------------
    fun startScan(
        currentLocation: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        viewModelScope.launch {
            _isScanning.value = true
            _scanProgress.value = 0f

            repeat(20) {
                kotlinx.coroutines.delay(75)
                _scanProgress.value += 0.05f
            }

            val finalEmotion = listOf("happy", "sad", "anxious", "calm", "excited", "tired").random()

            _detectedEmotion.value = finalEmotion
            _isScanning.value = false

            saveScanResult(finalEmotion, currentLocation, latitude, longitude)
        }
    }


    // ---------------------- DATABASE WRITE ----------------------
    private fun saveScanResult(
        emotion: String,
        locationName: String?,
        lat: Double?,
        lng: Double?
    ) {
        viewModelScope.launch {

            val entry = JournalEntry(
                entryId = UUID.randomUUID().toString(),
                userId = userId,
                mood = emotion,
                timestamp = System.currentTimeMillis(),
                locationName = locationName,
                latitude = lat,
                longitude = lng
            )
            journalRepo.insertEntry(entry)

            updateMoodStats()
        }
    }

    private suspend fun updateMoodStats() {
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
    // FACTORY
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
