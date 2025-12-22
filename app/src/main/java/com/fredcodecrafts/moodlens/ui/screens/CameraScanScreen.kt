package com.fredcodecrafts.moodlens.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.fredcodecrafts.moodlens.R
import com.fredcodecrafts.moodlens.database.AppDatabase
import com.fredcodecrafts.moodlens.database.repository.JournalRepository
import com.fredcodecrafts.moodlens.database.repository.MoodScanStatRepository
import com.fredcodecrafts.moodlens.database.viewmodel.CameraScanViewModel
import com.fredcodecrafts.moodlens.navigation.Screen
import com.fredcodecrafts.moodlens.utils.GlobalNotificationHandler
import com.fredcodecrafts.moodlens.utils.rememberNotificationState
import kotlinx.coroutines.delay

import java.util.UUID

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
                tint = Color(0xFF7B3FE4)
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
    hasCameraPermission: Boolean,
    imageCapture: androidx.camera.core.ImageCapture,
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
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(primaryGradient, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(primaryGradient, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Layer 1: Camera Preview or Placeholder
            if (hasCameraPermission) {
                // Add padding to show the purple border
                Box(modifier = Modifier.padding(4.dp).clip(RoundedCornerShape(20.dp))) {
                    CameraPreviewView(
                        imageCapture = imageCapture,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                CameraPreviewPlaceholder()
            }

            // Layer 2: Scanning Overlay
            if (isScanning) {
                // Scanning animation
                val infiniteTransition = rememberInfiniteTransition()
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )

                // Semi-transparent overlay to dim the camera a bit
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
}


@Composable
fun MoodResultSection(
    mood: String,
    onContinue: () -> Unit,
    onReflect: () -> Unit
) {
    val moodEmojis = mapOf(
        "angry" to "😠",
        "disgust" to "🤢",
        "fear" to "😨",
        "happy" to "😊",
        "neutral" to "😐",
        "sad" to "😢",
        "surprise" to "😲"
    )
    val moodMessages = mapOf(
        "angry" to "You seem angry. Take a deep breath 😤",
        "disgust" to "Something seems off. It's okay to feel this way 🤢",
        "fear" to "You look afraid. You are safe here 🛡️",
        "happy" to "You look happy today! ✨",
        "neutral" to "You seem neutral. A balanced state of mind ⚖️",
        "sad" to "You look sad today... I'm here for you 💙",
        "surprise" to "Wow! You look surprised! 😲"
    )
    val moodGradient = when (mood) {
        "happy", "surprise" -> Brush.linearGradient(listOf(Color(0xFFFFB300), Color(0xFFFF8A00)))
        "sad", "fear" -> Brush.linearGradient(listOf(Color(0xFF4E54C8), Color(0xFF8F94FB)))
        "angry", "disgust" -> Brush.linearGradient(listOf(Color(0xFFD32F2F), Color(0xFFE57373)))
        "neutral" -> Brush.linearGradient(listOf(Color(0xFF607D8B), Color(0xFF90A4AE)))
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
                    text = "Take Photo",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

/**
 * Camera preview composable using CameraX PreviewView.
 * Keeps camera binding simple — ready for adding ImageAnalysis later for ML.
 */
@Composable
fun CameraPreviewView(
    imageCapture: androidx.camera.core.ImageCapture,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also { p ->
                        p.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreviewView", "Failed to bind camera use cases", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}

/**
 * Request camera and location permissions.
 */
@Composable
fun RequestPermissions(onGranted: () -> Unit) {
    val context = LocalContext.current
    var permissionRequested by remember { mutableStateOf(false) }

    val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val allGranted = result.values.all { it }
        if (allGranted) onGranted()
    }

    LaunchedEffect(Unit) {
        if (!permissionRequested) {
            permissionRequested = true
            launcher.launch(permissions)
        }
    }
}

/**
 * Full CameraScanScreen integrated with CameraScanViewModel, CameraX preview, and DB repositories.
 */
@Composable
fun CameraScanScreen(
    navController: NavHostController,
    database: AppDatabase,
    userId: String
) {
    val notificationState = rememberNotificationState()
    val context = LocalContext.current
    
    // Create repositories from your existing DB
    val journalRepo = remember {
        JournalRepository(
            database.journalDao(),
            database.notesDao(),
            database.messagesDao(),
            database.moodScanStatDao()
        )
    }

    val statsRepo = remember {
        MoodScanStatRepository(database.moodScanStatDao())
    }

    // Create ViewModel via Factory
    val viewModel: CameraScanViewModel = viewModel(
        factory = CameraScanViewModel.Factory(
            journalRepo,
            statsRepo,
            userId,
            context.applicationContext
        )
    )

    // Collect UI state from ViewModel
    val detectedEmotion by viewModel.detectedEmotion.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()

    // Permission flow
    fun checkPermissions(): Boolean {
        val camera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val location = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return camera && location
    }

    var permissionGranted by remember { mutableStateOf(checkPermissions()) }

    if (!permissionGranted) {
        RequestPermissions(onGranted = { permissionGranted = true })
        GlobalNotificationHandler(state = notificationState)
        return
    }

    // Notification when scan is done
    LaunchedEffect(detectedEmotion) {
        detectedEmotion?.let { em ->
            notificationState.showNotification(
                com.fredcodecrafts.moodlens.utils.NotificationData(
                    title = "Analysis Complete",
                    message = "Detected: ${em.replaceFirstChar { it.uppercase() }}",
                    type = com.fredcodecrafts.moodlens.utils.NotificationType.SUCCESS,
                    duration = 3000L
                )
            )
        }
    }

    // ImageCapture use case
    val imageCapture = remember {
        androidx.camera.core.ImageCapture.Builder()
            .setCaptureMode(androidx.camera.core.ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    
    // Location Client
    val fusedLocationClient = remember {
        com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
    }

    val geocoder = remember { android.location.Geocoder(context, java.util.Locale.getDefault()) }

    // Function to get simple address (City, Country)
    fun getCityName(lat: Double, lng: Double): String {
        return try {
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                // Combine locality (City) and country
                val city = address.locality ?: address.subAdminArea ?: "Unknown City"
                val country = address.countryName ?: ""
                if (country.isNotBlank()) "$city, $country" else city
            } else {
                "Unknown Location"
            }
        } catch (e: Exception) {
            Log.e("CameraScanScreen", "Geocoder failed", e)
            "Unknown Location"
        }
    }

    // Function to take photo and get location
    fun takePhotoAndAnalyze() {
        val mainExecutor = ContextCompat.getMainExecutor(context)
        
        imageCapture.takePicture(mainExecutor, object : androidx.camera.core.ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                val bitmap = image.toBitmap()
                
                try {
                    // Fetch location (permission already granted)
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            Log.d("CameraScanScreen", "Location found: ${location.latitude}, ${location.longitude}")
                            // Use Geocoder here
                            val cityLocation = getCityName(location.latitude, location.longitude)
                            viewModel.analyzeImage(bitmap, cityLocation, location.latitude, location.longitude)
                        } else {
                            Log.d("CameraScanScreen", "Location null, proceeding without it")
                            viewModel.analyzeImage(bitmap)
                        }
                    }.addOnFailureListener {
                        Log.e("CameraScanScreen", "Failed to get location", it)
                        viewModel.analyzeImage(bitmap)
                    }
                } catch (e: SecurityException) {
                    Log.e("CameraScanScreen", "Permission error getting location", e)
                    viewModel.analyzeImage(bitmap)
                } finally {
                    image.close()
                }
            }

            override fun onError(exception: androidx.camera.core.ImageCaptureException) {
                Log.e("CameraScanScreen", "Photo capture failed: ${exception.message}", exception)
            }
        })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        if (detectedEmotion == null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MoodScanHeader(navController = navController)

                MoodCameraCard(
                    isScanning = isScanning,
                    scanProgress = scanProgress,
                    hasCameraPermission = permissionGranted,
                    imageCapture = imageCapture
                )

                ScanActionButton(
                    showCameraPreview = !isScanning,
                    onStartScan = { takePhotoAndAnalyze() }, // <-- Call takePhotoAndAnalyze
                    onNewScan = { viewModel.resetScan() }
                )
            }
        } else {
            MoodResultSection(
                mood = detectedEmotion!!,
                onContinue = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.CameraScan.route) { inclusive = true }
                    }
                },
                onReflect = {
                    // Use the ID from the ViewModel if available, or fallback to new_scan (should not happen if flow is correct)
                    val entryId = viewModel.lastEntryId.value ?: UUID.randomUUID().toString()
                    navController.navigate(
                        Screen.Reflection.createRoute(
                            entryId = entryId,
                            mood = detectedEmotion!!
                        )
                    ) {
                        popUpTo(Screen.CameraScan.route) { inclusive = true }
                    }
                }
            )
        }
    }
    GlobalNotificationHandler(state = notificationState)
}
