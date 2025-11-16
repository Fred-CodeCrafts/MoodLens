package com.fredcodecrafts.moodlens.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.fredcodecrafts.moodlens.R
import com.fredcodecrafts.moodlens.utils.rememberNotificationState
import com.fredcodecrafts.moodlens.utils.GlobalNotificationHandler
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import com.fredcodecrafts.moodlens.navigation.Screen


val purpleColor = Color(0xFF7B3FE4) // your purple


@Composable
fun CameraPreviewPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFB3E5FC), Color(0xFF81D4FA))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_camera_svg),
                contentDescription = "Camera Icon",
                modifier = Modifier.size(64.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Position your face in the camera frame",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
@Composable
fun MoodScanHeader(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Home button top-left
        IconButton(
            onClick = {
                navController.navigate(Screen.Home.route) {
                    // Clear back stack up to the start destination to prevent going back to CameraScan
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.lc_home_svg),
                contentDescription = "home",
                tint = purpleColor
            )

        }

        // Header content centered
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sparkles icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF7B3FE4), Color(0xFFBB6BD9)) // Gradient purple
                        ),
                        shape = CircleShape
                    )
                    .shadow(8.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.heart),
                    contentDescription = "Heart",
                    tint = Color.White, // Icon color is white
                    modifier = Modifier.size(32.dp)
                )
            }


            Spacer(modifier = Modifier.height(16.dp))

            // MoodLens title with gradient text
            Text(
                text = "MoodLens",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF7B3FE4), Color(0xFFBB6BD9))
                    )
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Let's check in with how you're feeling today",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}
@Composable
fun MoodCameraCard(
    isScanning: Boolean,
    scanProgress: Float,
    modifier: Modifier = Modifier
) {
    val primaryGradient = Brush.linearGradient(listOf(Color(0xFF7B3FE4), Color(0xFFBB6BD9)))
    val warmGradient = Brush.linearGradient(listOf(Color(0xFFFFB300), Color(0xFFFF8A00)))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent) // Made transparent
    ) {
        // Gradient background applied to the Box inside the Card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(primaryGradient, RoundedCornerShape(24.dp)), // Apply gradient here
            contentAlignment = Alignment.Center
        ) {
            if (!isScanning) {
                CameraPreviewPlaceholder()
            } else {
                val infiniteTransition = rememberInfiniteTransition(label = "Rotation") // Added label
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ), label = "Rotation Animation" // Added label
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.White.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(primaryGradient, CircleShape)
                                .graphicsLayer { rotationZ = rotation },
                            contentAlignment = Alignment.TopStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color.White, CircleShape)
                                    .align(Alignment.TopStart)
                                    .offset(x = 4.dp, y = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Analyzing your mood...",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(scanProgress)
                                .background(warmGradient, RoundedCornerShape(16.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(scanProgress * 100).toInt()}%",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}


@Composable
fun MoodResultSection(
    mood: String,
    onContinue: () -> Unit,
    onReflect: () -> Unit
) {
    val moodEmojis = mapOf(
        "happy" to "😊",
        "sad" to "😢",
        "anxious" to "😰",
        "calm" to "😌",
        "excited" to "🤩",
        "tired" to "😴"
    )
    val moodMessages = mapOf(
        "happy" to "You look happy today! ✨",
        "sad" to "You look sad today... I'm here for you 💙",
        "anxious" to "You seem anxious today. Let's breathe together 🌸",
        "calm" to "You look calm and peaceful today 🕊️",
        "excited" to "You look excited today! Amazing energy! ⚡",
        "tired" to "You look tired today. Rest is important 🌙"
    )
    val moodGradient = when (mood) {
        "happy", "excited" -> Brush.linearGradient(listOf(Color(0xFFFFB300), Color(0xFFFF8A00)))
        "sad", "tired" -> Brush.linearGradient(listOf(Color(0xFF4E54C8), Color(0xFF8F94FB)))
        "anxious" -> Brush.linearGradient(listOf(Color(0xFF7B3FE4), Color(0xFFBB6BD9)))
        "calm" -> Brush.linearGradient(listOf(Color(0xFF00C6FF), Color(0xFF0072FF)))
        else -> Brush.linearGradient(listOf(Color(0xFF7B3FE4), Color(0xFFBB6BD9)))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mood Card
        // Mood Card centered
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f) // 90% of screen width
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .background(moodGradient, RoundedCornerShape(24.dp))
                        .padding(32.dp)
                        .fillMaxWidth(), // <-- make inner Box take full width
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(), // <-- full width
                        horizontalAlignment = Alignment.CenterHorizontally // <-- center content
                    ) {
                        Text(
                            text = moodEmojis[mood] ?: "😐",
                            fontSize = 48.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            text = "Mood Detected",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = moodMessages[mood] ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }



        // Mood Added Confirmation
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF4CAF50), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Heart",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your mood has been added to your journal",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        // Action Buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Button(
                onClick = onReflect,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.9f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.message), // your custom message icon
                    contentDescription = "Talk to AI",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Let's Reflect Together", color = Color.Black)
            }

            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B3FE4)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Continue", color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Continue",
                    tint = Color.White
                )
            }
        }

        // Encouragement Text
        Text(
            text = "Remember, all feelings are valid. You're doing great by checking in with yourself! 💜",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 300.dp)
        )
    }
}

@Composable
fun ScanActionButton(
    showCameraPreview: Boolean,
    onStartScan: () -> Unit,
    onNewScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showCameraPreview) {
            Button(
                onClick = onStartScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B3FE4)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_camera_svg), // <-- custom SVG
                    contentDescription = "Start Scan",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Mood Scan",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else {
            Button(
                onClick = onNewScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF7B3FE4)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, Color(0xFF7B3FE4))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "New Scan",
                    tint = Color(0xFF7B3FE4),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "New Scan",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun CameraScanScreen(navController: NavHostController) {
    val notificationState = rememberNotificationState()
    var detectedEmotion by remember { mutableStateOf<String?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableStateOf(0f) }

    // Simulate scanning
    LaunchedEffect(isAnalyzing) {
        if (isAnalyzing) {
            scanProgress = 0f
            repeat(20) {
                delay(75)
                scanProgress += 0.05f
            }

            val emotions = when (Random.nextInt(100)) {
                in 0..30 -> "happy"
                in 31..50 -> "neutral"
                in 51..70 -> "calm"
                in 71..85 -> "sad"
                in 86..95 -> "excited"
                else -> "tired"
            }
            detectedEmotion = emotions
            isAnalyzing = false

            notificationState.showNotification(
                com.fredcodecrafts.moodlens.utils.NotificationData(
                    title = "Analysis Complete",
                    message = "Detected: ${emotions.replaceFirstChar { it.uppercase() }}",
                    type = com.fredcodecrafts.moodlens.utils.NotificationType.SUCCESS,
                    duration = 3000L
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        if (detectedEmotion == null) {
            // Show camera + scan button while scanning/not scanned
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MoodScanHeader(navController = navController)
                MoodCameraCard(
                    isScanning = isAnalyzing,
                    scanProgress = scanProgress
                )
                ScanActionButton(
                    showCameraPreview = !isAnalyzing,
                    onStartScan = { isAnalyzing = true },
                    onNewScan = {
                        detectedEmotion = null
                        scanProgress = 0f
                        isAnalyzing = false
                    }
                )
            }
        } else {
            // Overwrite everything with the result
            MoodResultSection(
                mood = detectedEmotion!!,
                onContinue = {
                    // Navigate to Home
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.CameraScan.route) { inclusive = true } // remove CameraScan from backstack
                    }
                },
                onReflect = {    navController.navigate(Screen.Reflection.createRoute(entryId = "new_scan", mood = detectedEmotion!!)) {
                    popUpTo(Screen.CameraScan.route) { inclusive = true } // remove CameraScan from backstack
                }

                }
            )
        }
    }

    GlobalNotificationHandler(state = notificationState)
}

