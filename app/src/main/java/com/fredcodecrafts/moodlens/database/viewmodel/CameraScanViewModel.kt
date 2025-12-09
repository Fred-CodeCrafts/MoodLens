package com.fredcodecrafts.moodlens.database.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fredcodecrafts.moodlens.database.entities.JournalEntry
import com.fredcodecrafts.moodlens.database.entities.MoodScanStat
import com.fredcodecrafts.moodlens.database.repository.JournalRepository
import com.fredcodecrafts.moodlens.database.repository.MoodScanStatRepository
import com.fredcodecrafts.moodlens.ml.EmotionClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CameraScanViewModel(
    private val journalRepo: JournalRepository,
    private val statsRepo: MoodScanStatRepository,
    private val userId: String,
    private val emotionClassifier: EmotionClassifier
) : ViewModel() {

    private val _detectedEmotion = MutableStateFlow<String?>(null)
    val detectedEmotion = _detectedEmotion.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress = _scanProgress.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private var lastAnalysisTime = 0L
    private val ANALYSIS_INTERVAL = 1000L // Analyze every 1 second

    // ---------------------- REAL SCAN LOGIC ----------------------
    // ---------------------- REAL SCAN LOGIC ----------------------
    fun startScan() {
        // No-op or just reset state. 
        // In manual mode, the UI calls takePhoto directly.
        _isScanning.value = true
        _scanProgress.value = 0f
        _detectedEmotion.value = null
    }

    fun analyzeImage(bitmap: Bitmap, currentLocation: String? = null, lat: Double? = null, lng: Double? = null) {
        // Set scanning state to show loading UI
        _isScanning.value = true
        _scanProgress.value = 0.2f
        
        android.util.Log.d("CameraScanViewModel", "Analyzing captured photo...")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Simulate progress update
                _scanProgress.value = 0.5f

                // Usage of new simpler API
                val predictionIndex = emotionClassifier.predict(bitmap)
                val emotion = emotionClassifier.getEmotionLabel(predictionIndex)
                
                if (emotion != null) {
                    android.util.Log.d("CameraScanViewModel", "Emotion detected: $emotion (Index: $predictionIndex)")
                    // If we got a valid emotion, stop scanning and save
                    viewModelScope.launch(Dispatchers.Main) {
                        _detectedEmotion.value = emotion
                        _isScanning.value = false
                        _scanProgress.value = 1f
                        
                        saveScanResult(emotion, currentLocation, lat, lng)
                    }
                } else {
                    android.util.Log.d("CameraScanViewModel", "No emotion detected or invalid index: $predictionIndex")
                    viewModelScope.launch(Dispatchers.Main) {
                        _isScanning.value = false
                        _scanProgress.value = 0f
                        // Optional: Show error message
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CameraScanViewModel", "Error analyzing image", e)
                viewModelScope.launch(Dispatchers.Main) {
                    _isScanning.value = false
                    _scanProgress.value = 0f
                }
            }
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

            // Insert Journal Entry
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

            // Insert / Update Stats
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

    fun simulateScanResult() {
        viewModelScope.launch {
            _isScanning.value = true
            _scanProgress.value = 0.5f
            kotlinx.coroutines.delay(1000)
            
            val emotion = "happy" // Force happy for testing
            _detectedEmotion.value = emotion
            _isScanning.value = false
            _scanProgress.value = 1f
            
            saveScanResult(emotion, null, null, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        emotionClassifier.close()
    }

    // -------------------------------------------------------------------
    // FACTORY
    // -------------------------------------------------------------------
    class Factory(
        private val journalRepo: JournalRepository,
        private val statsRepo: MoodScanStatRepository,
        private val userId: String,
        private val context: Context
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CameraScanViewModel::class.java)) {
                val classifier = EmotionClassifier(context)
                return CameraScanViewModel(journalRepo, statsRepo, userId, classifier) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}