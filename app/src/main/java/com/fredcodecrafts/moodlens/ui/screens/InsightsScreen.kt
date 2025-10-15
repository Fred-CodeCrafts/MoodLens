package com.fredcodecrafts.moodlens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite // Available icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fredcodecrafts.moodlens.database.AppDatabase
import com.fredcodecrafts.moodlens.database.entities.JournalEntry
import com.fredcodecrafts.moodlens.database.entities.Note
import com.fredcodecrafts.moodlens.database.entities.MoodScanStat
import com.fredcodecrafts.moodlens.database.DummyData
import java.util.concurrent.TimeUnit

@Composable
fun InsightsScreen(
    navController: NavHostController,
    database: AppDatabase
) {
    var journalEntries by remember { mutableStateOf<List<JournalEntry>>(emptyList()) }
    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var moodStats by remember { mutableStateOf<List<MoodScanStat>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val journalData = database.journalDao().getAllEntries() // Fixed DAO name
            val notesData = database.notesDao().getAllNotes() // Fixed DAO name
            val statsData = database.moodScanStatDao().getAllStats()

            journalEntries = journalData.ifEmpty { DummyData.journalEntries }
            notes = notesData.ifEmpty { DummyData.notes }
            moodStats = statsData.ifEmpty { DummyData.moodScanStats }
        } catch (e: Exception) {
            journalEntries = DummyData.journalEntries
            notes = DummyData.notes
            moodStats = DummyData.moodScanStats
        }
        isLoading = false
    }

    val insightsData = calculateInsights(journalEntries, notes, moodStats)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "Insights",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Most Common Mood Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF9575CD) // Purple
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Emoji
                    Text(
                        text = getMoodEmoji(insightsData.mostCommonMood ?: "Anxious"),
                        style = MaterialTheme.typography.displayLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Your Most Common Mood",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = insightsData.mostCommonMood ?: "No data",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Surface(
                        color = Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${insightsData.mostCommonMoodCount} times",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Wellness Score Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite, // FIXED: Using Assessment icon
                                contentDescription = null,
                                tint = Color(0xFF66BB6A),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Wellness Score",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color(0xFF1A1A1A)
                            )
                        }

                        Surface(
                            color = Color(0xFF66BB6A),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${insightsData.wellnessScore}%",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { insightsData.wellnessScore / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = Color(0xFF7E57C2),
                        trackColor = Color(0xFFE0E0E0)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Based on positive mood entries this period",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575)
                    )
                }
            }
        }

        // This Week Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color(0xFF7E57C2),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "This Week",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFF1A1A1A)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = insightsData.weeklyCheckins.toString(),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF7E57C2)
                            )
                            Text(
                                text = "Check-ins",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF757575)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = insightsData.weeklyNotes.toString(),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF7E57C2)
                            )
                            Text(
                                text = "With Notes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }
            }
        }

        // Mood Distribution Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Mood Distribution",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val maxCount = insightsData.moodDistribution.values.maxOrNull() ?: 1

                    insightsData.moodDistribution.forEach { (mood, count) ->
                        MoodDistributionBar(
                            mood = mood,
                            count = count,
                            maxCount = maxCount
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        // Personalized Tips Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFCDD2) // Light pink/peach
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🎯",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Personalized Tips",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFFD32F2F) // Darker red for better contrast
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    insightsData.personalizedTips.forEach { tip ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = getTipIcon(tip),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFD32F2F),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MoodDistributionBar(
    mood: String,
    count: Int,
    maxCount: Int
) {
    val progress = if (maxCount > 0) count.toFloat() / maxCount.toFloat() else 0f

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = getMoodEmoji(mood),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(end = 8.dp)
        )

        Text(
            text = mood,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.width(80.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .background(Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(
                        Color(0xFF7E57C2),
                        RoundedCornerShape(4.dp)
                    )
            )
        }

        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF1A1A1A),
            modifier = Modifier
                .padding(start = 12.dp)
                .width(24.dp)
        )
    }
}

fun getMoodEmoji(mood: String): String {
    return when (mood.lowercase()) {
        "happy" -> "😊"
        "sad" -> "😢"
        "anxious" -> "😰"
        "calm" -> "😌"
        "excited" -> "🤩"
        "tired" -> "😴"
        else -> "😐"
    }
}

fun getTipIcon(tip: String): String {
    return when {
        tip.contains("stress") -> "💙"
        tip.contains("notes") || tip.contains("note") -> "📝"
        tip.contains("breathing") -> "🧘"
        else -> "💡"
    }
}

data class InsightsData(
    val mostCommonMood: String?,
    val mostCommonMoodCount: Int,
    val wellnessScore: Int,
    val weeklyCheckins: Int,
    val weeklyNotes: Int,
    val moodDistribution: Map<String, Int>,
    val personalizedTips: List<String>
)

private fun calculateInsights(
    journalEntries: List<JournalEntry>,
    notes: List<Note>,
    moodStats: List<MoodScanStat>
): InsightsData {
    if (journalEntries.isNotEmpty()) {
        return createDemoInsightsData()
    }

    val oneWeekAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
    val weeklyEntries = journalEntries.filter { it.timestamp >= oneWeekAgo }

    val moodCounts = journalEntries.groupingBy { it.mood }.eachCount()
    val mostCommonMood = moodCounts.maxByOrNull { it.value }

    val positiveMoods = listOf("happy", "calm", "excited")
    val positiveEntries = journalEntries.count { it.mood.lowercase() in positiveMoods }
    val wellnessScore = if (journalEntries.isNotEmpty()) {
        (positiveEntries * 100) / journalEntries.size
    } else 0

    val weeklyCheckins = weeklyEntries.size
    val weeklyNotes = weeklyEntries.count { entry ->
        notes.any { it.entryId == entry.entryId }
    }

    val moodDistribution = moodCounts.entries
        .sortedByDescending { it.value }
        .associate { it.key to it.value }

    val entriesWithNotes = journalEntries.count { journalEntry ->
        notes.any { it.entryId == journalEntry.entryId }
    }
    val personalizedTips = generatePersonalizedTips(journalEntries, wellnessScore, entriesWithNotes)

    return InsightsData(
        mostCommonMood = mostCommonMood?.key?.replaceFirstChar { it.uppercase() },
        mostCommonMoodCount = mostCommonMood?.value ?: 0,
        wellnessScore = wellnessScore,
        weeklyCheckins = weeklyCheckins,
        weeklyNotes = weeklyNotes,
        moodDistribution = moodDistribution.mapKeys { it.key.replaceFirstChar { char -> char.uppercase() } },
        personalizedTips = personalizedTips
    )
}

private fun createDemoInsightsData(): InsightsData {
    return InsightsData(
        mostCommonMood = "Anxious",
        mostCommonMoodCount = 2,
        wellnessScore = 20,
        weeklyCheckins = 5,
        weeklyNotes = 0,
        moodDistribution = mapOf(
            "Anxious" to 3,
            "Sad" to 1,
            "Excited" to 1,
            "Tired" to 1,
            "Happy" to 2
        ),
        personalizedTips = listOf(
            "Consider adding more stress-relief activities to your daily routine.",
            "Adding notes to your moods helps identify patterns and triggers."
        )
    )
}

private fun generatePersonalizedTips(
    journalEntries: List<JournalEntry>,
    wellnessScore: Int,
    entriesWithNotes: Int
): List<String> {
    val tips = mutableListOf<String>()

    if (wellnessScore < 30) {
        tips.add("Consider adding more stress-relief activities to your daily routine.")
    }

    if (entriesWithNotes < journalEntries.size / 2) {
        tips.add("Adding notes to your moods helps identify patterns and triggers.")
    }

    if (tips.isEmpty()) {
        tips.add("Regular mood tracking helps build self-awareness.")
    }

    return tips.take(3)
}