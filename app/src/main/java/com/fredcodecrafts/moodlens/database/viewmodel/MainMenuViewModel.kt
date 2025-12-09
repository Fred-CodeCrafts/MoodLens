package com.fredcodecrafts.moodlens.database.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fredcodecrafts.moodlens.database.repository.MoodScanStatRepository
import com.fredcodecrafts.moodlens.database.entities.MoodScanStat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainMenuViewModel(
    private val moodRepo: MoodScanStatRepository,
    private val userId: String
) : ViewModel() {

    private val _moodStat = MutableStateFlow<MoodScanStat?>(null)
    val moodStat: StateFlow<MoodScanStat?> = _moodStat

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            // Get the first stat for the user if available, or list logic if needed.
            // Original code used .firstOrNull(), implying it expected a list but took one.
            // With getStatsForUser, we get a List<MoodScanStat>.
            // Assuming MainMenu displays aggregated data or the latest?
             // Checking MoodScanStat entity usage in MainMenu: it uses dailyScans, weekStreak from MoodScanStat.
             // Wait, MoodScanStat seems to be a single summary object per user/date?
             // DAO getStatsForUser returns List<MoodScanStat>.
             // If MainMenu expects a single aggregate, we might need to aggregate it or pick the latest.
             // For now, mirroring previous behavior: getAllStats().firstOrNull() -> getStatsForUser(userId).firstOrNull()
             // But actually, MoodScanStat seems to track streaks/counts.
            _moodStat.value = moodRepo.getStatsForUser(userId).firstOrNull()
        }
    }
}

class MainMenuViewModelFactory(
    private val repo: MoodScanStatRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainMenuViewModel(repo, userId) as T
    }
}
