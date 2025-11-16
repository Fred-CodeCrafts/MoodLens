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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

// ----------------------------- DATA MODELS -----------------------------

data class InsightsData(
    val mostCommonMood: String? = null,
    val mostCommonMoodCount: Int = 0,
    val wellnessScore: Int = 0,
    val weeklyCheckins: Int = 0,
    val weeklyNotes: Int = 0,
    val moodDistribution: Map<String, Int> = emptyMap(),
    val personalizedTips: List<String> = emptyList()
)

data class JournalStats(
    val totalEntries: Int,
    val withNotes: Int,
    val daysTracked: Int
)

data class MoodProfile(val emoji: String, val score: Int)


// ----------------------------- VIEWMODEL -----------------------------

class InsightsViewModel(
    private val repository: JournalRepository,
    private val userId: String
) : ViewModel() {

    private val _journalEntries = MutableStateFlow<List<JournalEntry>>(emptyList())
    val journalEntries: StateFlow<List<JournalEntry>> = _journalEntries

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _moodStats = MutableStateFlow<List<MoodScanStat>>(emptyList())
    val moodStats: StateFlow<List<MoodScanStat>> = _moodStats

    private val _insights = MutableStateFlow(InsightsData())
    val insights: StateFlow<InsightsData> = _insights

    init {
        fetchData()
    }

    // ----------------------------- DATA LOADING -----------------------------

    private fun fetchData() {
        viewModelScope.launch {
            try {
                // Load journal entries for the user from repository
                val entries = repository.getEntriesForUser(userId)
                _journalEntries.value = entries

                // Load notes (collect notes for loaded entries)
                val notesList = entries.flatMap { entry ->
                    repository.getNotesForEntry(entry.entryId)
                }
                _notes.value = notesList

                // Load mood stats (global or user-specific depending on repo)
                val statsList = repository.getAllMoodStats()
                _moodStats.value = statsList

                // Compute insights on background thread
                computeInsights(entries, notesList, statsList)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Public method to force refresh (UI can call)
    fun refresh() {
        fetchData()
    }

    // ----------------------------- INSIGHTS LOGIC -----------------------------

    private suspend fun computeInsights(
        journalEntries: List<JournalEntry>,
        notes: List<Note>,
        moodStats: List<MoodScanStat>
    ) {
        withContext(Dispatchers.Default) {

            if (journalEntries.isEmpty()) {
                // keep demo/default if empty
                _insights.value = createDemoInsightsData()
                return@withContext
            }

            val oneWeekAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
            val weeklyEntries = journalEntries.filter { it.timestamp >= oneWeekAgo }

            // Most common mood
            val moodCounts = journalEntries.groupingBy { it.mood }.eachCount()
            val mostCommonMood = moodCounts.maxByOrNull { it.value }

            // Wellness score calculation (same logic as UI previously)
            val totalImpact = journalEntries.sumOf { getMoodProfile(it.mood).score }
            val normalizedScore =
                ((totalImpact + (journalEntries.size * 10)) * 100) / (journalEntries.size * 20)
            val wellnessScore = normalizedScore.coerceIn(0, 100)

            // Weekly stats
            val weeklyCheckins = weeklyEntries.size
            val weeklyNotes = weeklyEntries.count { entry ->
                notes.any { it.entryId == entry.entryId }
            }

            // Mood distribution
            val moodDistribution = moodCounts.entries
                .sortedByDescending { it.value }
                .associate {
                    it.key.replaceFirstChar { c -> c.uppercase() } to it.value
                }

            // Notes analysis
            val entriesWithNotes = journalEntries.count { entry ->
                notes.any { it.entryId == entry.entryId }
            }

            val personalizedTips =
                generatePersonalizedTips(journalEntries, wellnessScore, entriesWithNotes)

            // Update state
            _insights.value = InsightsData(
                mostCommonMood = mostCommonMood?.key?.replaceFirstChar { it.uppercase() },
                mostCommonMoodCount = mostCommonMood?.value ?: 0,
                wellnessScore = wellnessScore,
                weeklyCheckins = weeklyCheckins,
                weeklyNotes = weeklyNotes,
                moodDistribution = moodDistribution,
                personalizedTips = personalizedTips
            )
        }
    }

    // ----------------------------- UTILITY -----------------------------

    private fun generatePersonalizedTips(
        journalEntries: List<JournalEntry>,
        wellnessScore: Int,
        entriesWithNotes: Int
    ): List<String> {
        val tips = mutableListOf<String>()

        if (wellnessScore < 30)
            tips.add("Consider adding more stress-relief activities to your daily routine.")

        if (entriesWithNotes < journalEntries.size / 2)
            tips.add("Adding notes to your moods helps identify patterns.")

        if (tips.isEmpty())
            tips.add("Regular mood tracking helps build self-awareness.")

        return tips.take(3)
    }

    private fun getMoodProfile(mood: String): MoodProfile = when (mood.lowercase()) {
        "happy" -> MoodProfile("😊", 10)
        "neutral" -> MoodProfile("😐", 0)
        "sad" -> MoodProfile("😢", -10)
        "angry" -> MoodProfile("😠", -9)
        "disgust" -> MoodProfile("🤢", -8)
        "fear" -> MoodProfile("😱", -8)
        "surprise" -> MoodProfile("😮", 5)
        else -> MoodProfile("😶", 0)
    }

    // Demo fallback - identical shape used in UI
    private fun createDemoInsightsData(): InsightsData = InsightsData(
        mostCommonMood = "Happy",
        mostCommonMoodCount = 3,
        wellnessScore = 50,
        weeklyCheckins = 5,
        weeklyNotes = 1,
        moodDistribution = mapOf("Happy" to 3, "Sad" to 2, "Calm" to 1),
        personalizedTips = listOf("Regular mood tracking helps build self-awareness.")
    )
}

/**
 * Simple ViewModelFactory so Compose can create InsightsViewModel when given an AppDatabase.
 *
 * Assumes your JournalRepository has a constructor like `JournalRepository(appDatabase)` or a static
 * provider. If your repository constructor differs, adapt this factory accordingly.
 */
class InsightsViewModelFactory(
    private val repository: JournalRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InsightsViewModel::class.java)) {
            return InsightsViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
