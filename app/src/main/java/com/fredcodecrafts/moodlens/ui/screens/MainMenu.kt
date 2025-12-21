package com.fredcodecrafts.moodlens.ui.screens

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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fredcodecrafts.moodlens.components.*
import com.fredcodecrafts.moodlens.database.AppDatabase
import com.fredcodecrafts.moodlens.database.entities.MoodScanStat
import com.fredcodecrafts.moodlens.ui.theme.*
import com.fredcodecrafts.moodlens.utils.fadeInAnimation
import com.fredcodecrafts.moodlens.utils.slideUpAnimation
import kotlinx.coroutines.launch
import com.fredcodecrafts.moodlens.R
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.draw.shadow
import com.fredcodecrafts.moodlens.navigation.Screen
import com.fredcodecrafts.moodlens.ui.theme.AppTypography

// --- Added imports for ViewModel integration (only DB-related changes) ---
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fredcodecrafts.moodlens.database.repository.MoodScanStatRepository
import com.fredcodecrafts.moodlens.database.viewmodel.MainMenuViewModel
import com.fredcodecrafts.moodlens.database.viewmodel.MainMenuViewModelFactory
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.lazy.LazyColumn
@Composable
fun MainMenuScreen(
    navController: NavHostController,
    database: AppDatabase,
    userId: String
) {
    val scope = rememberCoroutineScope()

    // --- ViewModel-based data (replaces direct DAO call) ---
    val viewModel: MainMenuViewModel = viewModel(
        factory = MainMenuViewModelFactory(
            MoodScanStatRepository(database.moodScanStatDao()),
            userId
        )
    )

    // Observe the StateFlow exposed by the ViewModel
    val moodStat by viewModel.moodStat.collectAsState()

    // If you want to keep an isLoading flag similar to the previous implementation,
    // the ViewModel handles loading; here we keep a simple local flag set to false.
    var isLoading by remember { mutableStateOf(false) }

    val dailyScans = moodStat?.dailyScans ?: 0
    val weekStreak = moodStat?.weekStreak ?: 0
    val canAccessInsights = moodStat?.canAccessInsights ?: true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GradientBackground)
            .padding(20.dp)
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .fadeInAnimation()
        ) {
            Text(
                text = "MoodLens",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.graphicsLayer(alpha = 0.99f) // Needed for blend mode to work
                    .drawWithCache {
                        val brush = GradientPrimary
                        onDrawWithContent {
                            // Draw text with gradient fill instead of plain color
                            drawContent()
                            drawRect(
                                brush = brush,
                                blendMode = BlendMode.SrcAtop // <— applies gradient *into* text shape
                            )
                        }
                    },
                color = Color.White // base color (will blend into gradient)
            )

            Text(
                text = "Your emotional wellness companion",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stats Section
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .slideUpAnimation()
        ) {
            GradientStatCard(
                title = "Daily Scans",
                value = "$dailyScans",
                iconPainter = painterResource(R.drawable.ic_flame), // 🔥 custom flame icon
                gradient = GradientCalm,
                modifier = Modifier.weight(1f)
            )

            GradientStatCard(
                title = "Week Streak",
                value = "$weekStreak",
                iconVector = Icons.Default.DateRange, // standard icon
                gradient = GradientWarm,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Menu Items
        val menuItems = listOf(
            MenuItem(
                title = "Camera Scan",
                description = "Check your mood with AI",
                iconPainter = painterResource(R.drawable.ic_camera), // 🔥 custom flame icon
                onClick = { navController.navigate(Screen.CameraScan.route) },
                gradient = GradientPrimary,
                available = true
            ),
            MenuItem(
                title = "Journal",
                description = "View your mood history",
                iconPainter = painterResource(R.drawable.ic_book), // 🔥 custom flame icon
                onClick = { navController.navigate("Journal") },
                gradient = GradientCalm,
                available = true
            ),
            MenuItem(
                title = "Mood Map",
                description = "See where you feel your moods",
                iconPainter = painterResource(R.drawable.ic_map),
                onClick = { navController.navigate(Screen.MoodMap.route) },
                gradient = GradientAccent,
                available = true),
            MenuItem(
                title = "Insights",
                description = "Mood patterns & analytics",
                iconPainter = painterResource(R.drawable.ic_chart), // 🔥 custom flame icon
                onClick = { navController.navigate(Screen.Insights.route) },
                gradient = GradientWarm,
                available = canAccessInsights
            )
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            menuItems.forEachIndexed { index, item ->
                GradientMenuCard(
                    item = item,
                    index = index,
                    canAccessInsights = canAccessInsights
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Quick Action Button
        AppButton(
            text = "Quick Mood Scan",
            onClick = { navController.navigate(Screen.CameraScan.route) },
            modifier = Modifier
                .fillMaxWidth()
                .slideUpAnimation(),
            containerColor = MainPurple,
            contentColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Motivation
        Text(
            text = "🌟 Every day is a new opportunity to understand yourself better",
            modifier = Modifier.fillMaxWidth(),
            color = TextSecondary,
            textAlign = TextAlign.Center,
            fontSize = MaterialTheme.typography.bodySmall.fontSize
        )
        // TEMPORARY TESTING BUTTON - REMOVE LATER
        // Spacer(modifier = Modifier.height(16.dp))
        // AppButton(
        //     text = "TEST - Go to Insights",
        //   onClick = { navController.navigate(Screen.Insights.route) },
        //    modifier = Modifier
        //        .fillMaxWidth(),
        //    containerColor = Color.Red, // Make it stand out
        //    contentColor = Color.White
        //    )
    }
}



/**
 * GradientStatCard built on top of AppCard.
 * Layout:
 * [Icon] [Value]
 *   Title
 */
@Composable
fun GradientStatCard(
    title: String,
    value: String,
    gradient: Brush,
    modifier: Modifier = Modifier,
    iconVector: ImageVector? = null,
    iconPainter: Painter? = null
) {
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp),
        backgroundColor = Color.Transparent, // so gradient shows
        borderColor = Color.Transparent,
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(gradient, shape = RoundedCornerShape(12.dp))
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // --- Top Row: Icon + Value ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    when {
                        iconPainter != null -> {
                            Image(
                                painter = iconPainter,
                                contentDescription = title,
                                modifier = Modifier
                                    .size(28.dp)
                                    .padding(end = 8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        iconVector != null -> {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = title,
                                tint = Color.White,
                                modifier = Modifier
                                    .size(28.dp)
                                    .padding(end = 8.dp)
                            )
                        }
                    }

                    Text(
                        text = value,
                        color = Color.White,
                        style = AppTypography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // --- Bottom Text (title/label) ---
                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.85f),
                    style = AppTypography.bodySmall
                )
            }
        }
    }
}


@Composable
fun GradientMenuCard(
    item: MenuItem,
    index: Int,
    canAccessInsights: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = item.available) { item.onClick() }
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = MaterialTheme.colorScheme.CardShadowColor,
                spotColor = MaterialTheme.colorScheme.CardShadowColor
            )
            .background(Color.White, RoundedCornerShape(12.dp)) // Fixed Color.White
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(item.gradient),
                contentAlignment = Alignment.Center
            ) {
                when {
                    item.iconVector != null -> Icon(
                        imageVector = item.iconVector,
                        contentDescription = item.title,
                        tint = Color.White
                    )
                    item.iconPainter != null -> Image(
                        painter = item.iconPainter,
                        contentDescription = item.title
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (!item.available) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = item.description,
                    color = TextSecondary,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize
                )
                if (item.title == "Insights" && !canAccessInsights) {
                    Spacer(modifier = Modifier.height(4.dp))
                    AppBadge(text = "Unlock after 5 scans", type = BadgeType.Secondary)
                }
            }
        }
    }
}



data class MenuItem(
    val title: String,
    val description: String,
    val iconVector: ImageVector? = null,
    val iconPainter: Painter? = null,
    val onClick: () -> Unit,
    val gradient: Brush,
    val available: Boolean = true
)
