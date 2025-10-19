package com.fredcodecrafts.moodlens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.fredcodecrafts.moodlens.database.AppDatabase
import com.fredcodecrafts.moodlens.database.entities.JournalEntry
import com.fredcodecrafts.moodlens.database.entities.Note
import com.fredcodecrafts.moodlens.database.entities.MoodScanStat
import com.fredcodecrafts.moodlens.database.DummyData
import com.fredcodecrafts.moodlens.navigation.Screen
import com.fredcodecrafts.moodlens.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
            val journalData = database.journalDao().getAllEntries()
            val notesData = database.notesDao().getAllNotes()
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

    // Background computation for insights
    val insightsDataState = produceState(
        initialValue = createDemoInsightsData().copy(wellnessScore = 50),
        key1 = journalEntries,
        key2 = notes,
        key3 = moodStats
    ) {
        value = withContext(Dispatchers.Default) {
            calculateInsights(journalEntries, notes, moodStats)
        }
    }

    val insightsData = insightsDataState.value

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Insights.route) { inclusive = true }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1A1A1A)
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Insights",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF1A1A1A)
                    )
                }

                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        // Most Common Mood
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(GradientPrimary, RoundedCornerShape(16.dp))
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val emoji = getMoodProfile(insightsData.mostCommonMood ?: "Neutral").emoji

                        Text(text = emoji, style = MaterialTheme.typography.displayLarge)
                        Text(
                            text = "Your Most Common Mood",
                            style = MaterialTheme.typography.titleMedium,
                            color = BadgeTextWhite
                        )
                        Text(
                            text = insightsData.mostCommonMood ?: "No data",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = BadgeTextWhite,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Surface(
                            color = WhiteTransparent30,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${insightsData.mostCommonMoodCount} times",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BadgeTextWhite,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Wellness Score
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(6.dp)
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
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Wellness Score",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                        }
                        Surface(
                            color = SuccessGreen,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${insightsData.wellnessScore}%",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = insightsData.wellnessScore / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MainPurple,
                        trackColor = LightGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Based on your emotional impact over time",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // This Week
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
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
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF1A1A1A)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = insightsData.weeklyCheckins.toString(),
                                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF7E57C2)
                            )
                            Text("Check-ins", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF757575))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = insightsData.weeklyNotes.toString(),
                                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF7E57C2)
                            )
                            Text("With Notes", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF757575))
                        }
                    }
                }
            }
        }

        // Mood Distribution
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text(
                        text = "Mood Distribution",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    val maxCount = insightsData.moodDistribution.values.maxOrNull() ?: 1
                    insightsData.moodDistribution.forEach { (mood, count) ->
                        MoodDistributionBar(mood, count, maxCount)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        // Personalized Tips
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(GradientWarm, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎯", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Personalized Tips",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        insightsData.personalizedTips.forEach { tip ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = getTipIcon(tip),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = tip,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ✅ Mood Distribution – counts only
@Composable
fun MoodDistributionBar(mood: String, count: Int, maxCount: Int) {
    val profile = getMoodProfile(mood)
    val progress = if (maxCount > 0) count.toFloat() / maxCount.toFloat() else 0f

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = profile.emoji, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(end = 8.dp))
        Text(
            text = mood.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.width(80.dp)
        )
        Box(
            modifier = Modifier.weight(1f).height(8.dp).background(LightGray, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier.fillMaxHeight().fillMaxWidth(progress)
                    .background(MainPurple, RoundedCornerShape(4.dp))
            )
        }
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

// ✅ Mood profile system
data class MoodProfile(val emoji: String, val score: Int)

fun getMoodProfile(mood: String): MoodProfile = when (mood.lowercase()) {
    "happy" -> MoodProfile("😊", 10)
    "calm" -> MoodProfile("😌", 5)
    "excited" -> MoodProfile("🤩", 8)
    "neutral" -> MoodProfile("😐", 0)
    "tired" -> MoodProfile("😴", -3)
    "stressed" -> MoodProfile("😖", -6)
    "anxious" -> MoodProfile("😰", -8)
    "sad" -> MoodProfile("😢", -10)
    "angry" -> MoodProfile("😠", -9)
    else -> MoodProfile("😶", 0)
}

fun getTipIcon(tip: String): String = when {
    tip.contains("stress", ignoreCase = true) -> "💙"
    tip.contains("notes", ignoreCase = true) || tip.contains("note", ignoreCase = true) -> "📝"
    tip.contains("breathing", ignoreCase = true) -> "🧘"
    else -> "💡"
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

// ✅ Insights calculation (background thread safe)
private fun calculateInsights(
    journalEntries: List<JournalEntry>,
    notes: List<Note>,
    moodStats: List<MoodScanStat>
): InsightsData {
    if (journalEntries.isEmpty()) return createDemoInsightsData()

    val oneWeekAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
    val weeklyEntries = journalEntries.filter { it.timestamp >= oneWeekAgo }

    val moodCounts = journalEntries.groupingBy { it.mood }.eachCount()
    val mostCommonMood = moodCounts.maxByOrNull { it.value }

    // Wellness score from mood impact values
    val totalImpact = journalEntries.sumOf { getMoodProfile(it.mood).score }
    val normalizedScore =
        ((totalImpact + (journalEntries.size * 10)) * 100) / (journalEntries.size * 20)
    val wellnessScore = normalizedScore.coerceIn(0, 100)

    val weeklyCheckins = weeklyEntries.size
    val weeklyNotes = weeklyEntries.count { entry -> notes.any { it.entryId == entry.entryId } }

    val moodDistribution = moodCounts.entries.sortedByDescending { it.value }.associate { it.key to it.value }

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
        moodDistribution = moodDistribution.mapKeys { it.key.replaceFirstChar { c -> c.uppercase() } },
        personalizedTips = personalizedTips
    )
}

private fun createDemoInsightsData(): InsightsData = InsightsData(
    mostCommonMood = "Happy",
    mostCommonMoodCount = 3,
    wellnessScore = 50,
    weeklyCheckins = 5,
    weeklyNotes = 1,
    moodDistribution = mapOf("Happy" to 3, "Sad" to 2, "Calm" to 1),
    personalizedTips = listOf("Regular mood tracking helps build self-awareness.")
)

private fun generatePersonalizedTips(
    journalEntries: List<JournalEntry>,
    wellnessScore: Int,
    entriesWithNotes: Int
): List<String> {
    val tips = mutableListOf<String>()
    if (wellnessScore < 30) tips.add("Consider adding more stress-relief activities to your daily routine.")
    if (entriesWithNotes < journalEntries.size / 2) tips.add("Adding notes to your moods helps identify patterns.")
    if (tips.isEmpty()) tips.add("Regular mood tracking helps build self-awareness.")
    return tips.take(3)
}
