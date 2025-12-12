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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.fredcodecrafts.moodlens.database.AppDatabase
import com.fredcodecrafts.moodlens.database.entities.JournalEntry
import com.fredcodecrafts.moodlens.database.entities.Note
import com.fredcodecrafts.moodlens.database.entities.MoodScanStat
import com.fredcodecrafts.moodlens.database.repository.JournalRepository
import com.fredcodecrafts.moodlens.database.viewmodel.InsightsViewModel
import com.fredcodecrafts.moodlens.database.viewmodel.InsightsViewModelFactory
import com.fredcodecrafts.moodlens.database.viewmodel.InsightsData
import com.fredcodecrafts.moodlens.navigation.Screen
import com.fredcodecrafts.moodlens.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@Composable
fun InsightsScreen(
    navController: NavHostController,
    database: AppDatabase,
    userId: String
) {
    // Provide a sensible default user id — replace with real auth user id

    // Create repository and viewmodel factory (adapt if your JournalRepository constructor differs)
    val repository = remember {
        JournalRepository(
            journalDao = database.journalDao(),
            notesDao = database.notesDao(),
            messagesDao = database.messagesDao(),
            moodStatsDao = database.moodScanStatDao()
        )
    }
    val factory = remember { InsightsViewModelFactory(repository, userId) }

    val viewModel: InsightsViewModel = viewModel(factory = factory)

    // collect state from viewmodel
    val insightsState by viewModel.insights.collectAsState()
    val journalEntries by viewModel.journalEntries.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val moodStats by viewModel.moodStats.collectAsState()

    // use isLoading flag only for UI shimmer; here we consider empty = loading until viewmodel updates
    val isLoading = remember { derivedStateOf { insightsState == null } }

    // The original UI is preserved below; it now reads from insightsState (type InsightsData)
    val insightsData: InsightsData = insightsState ?: InsightsData()

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
