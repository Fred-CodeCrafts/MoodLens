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
    private val moodRepo: MoodScanStatRepository
) : ViewModel() {

    private val _moodStat = MutableStateFlow<MoodScanStat?>(null)
    val moodStat: StateFlow<MoodScanStat?> = _moodStat

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _moodStat.value = moodRepo.getAllStats().firstOrNull()
        }
    }
}

class MainMenuViewModelFactory(
    private val repo: MoodScanStatRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainMenuViewModel(repo) as T
    }
}
