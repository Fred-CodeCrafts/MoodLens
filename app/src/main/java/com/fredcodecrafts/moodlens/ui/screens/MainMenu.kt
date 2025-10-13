package com.fredcodecrafts.moodlens.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.fredcodecrafts.moodlens.R
import com.fredcodecrafts.moodlens.components.*
import com.fredcodecrafts.moodlens.database.AppDatabase
import com.fredcodecrafts.moodlens.database.entities.MoodScanStat
import kotlinx.coroutines.launch

@Composable
fun MainMenuScreen(
    navController: NavHostController,
    database: AppDatabase
) {
    val scope = rememberCoroutineScope()
    var moodStat by remember { mutableStateOf<MoodScanStat?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch user stats
    LaunchedEffect(Unit) {
        val stats = database.moodScanStatDao().getAllStats()
        moodStat = stats.firstOrNull()
        isLoading = false
    }

    val dailyScans = moodStat?.dailyScans ?: 0
    val weekStreak = moodStat?.weekStreak ?: 0
    val canAccessInsights = moodStat?.canAccessInsights ?: false

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "MoodLens",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Your emotional wellness companion",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AppCard(
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Flame icon from vector drawable
                    Image(
                        painter = painterResource(R.drawable.ic_flame),
                        contentDescription = "Daily Scans",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$dailyScans", color = MaterialTheme.colorScheme.onSecondary, fontSize = 18.sp)
                    Text(
                        "Daily Scans",
                        color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            AppCard(
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Week Streak",
                        tint = MaterialTheme.colorScheme.onError
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$weekStreak", color = MaterialTheme.colorScheme.onError, fontSize = 18.sp)
                    Text(
                        "Week Streak",
                        color = MaterialTheme.colorScheme.onError.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Menu Items
        val menuItems = listOf(
            MenuItem(
                title = "Camera Scan",
                description = "Check your mood with AI",
                onClick = { navController.navigate("camera") },
                available = true
            ),
            MenuItem(
                title = "Journal",
                description = "View your mood history",
                onClick = { navController.navigate("journal") },
                available = true
            ),
            MenuItem(
                title = "Insights",
                description = "Mood patterns & analytics",
                onClick = { navController.navigate("insights") },
                available = canAccessInsights
            )
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            menuItems.forEach { item ->
                AppCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = item.available) { item.onClick() }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(item.title.first().toString(), color = MaterialTheme.colorScheme.primary)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(item.title, fontWeight = FontWeight.Bold)
                                if (!item.available) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                item.description,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            if (item.title == "Insights" && !canAccessInsights) {
                                Spacer(modifier = Modifier.height(4.dp))
                                AppBadge(text = "Unlock after 5 scans", type = BadgeType.Secondary)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Action
        AppButton(
            text = "Quick Mood Scan",
            onClick = { navController.navigate("camera") },
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Motivation
        Text(
            text = "🌟 Every day is a new opportunity to understand yourself better",
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

data class MenuItem(
    val title: String,
    val description: String,
    val onClick: () -> Unit,
    val available: Boolean = true
)
