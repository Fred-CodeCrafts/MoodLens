package com.fredcodecrafts.moodlens.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Geocoder
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
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
import com.google.android.gms.location.LocationServices
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
import kotlinx.coroutines.delay
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min

// ML imports (your existing classes)
import com.fredcodecrafts.moodlens.ml.emotionPrediction.EmotionClassifier
import com.fredcodecrafts.moodlens.ml.emotionPrediction.PredictionResult

val purpleColor = Color(0xFF7B3FE4) // your purple

// -------------------------
// Your existing UI pieces (kept intact)
// -------------------------
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
    hasCameraPermission: Boolean,
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
            when {
                !hasCameraPermission -> {
                    CameraPreviewPlaceholder() // Show placeholder if no permission
                }
                !isScanning -> {
                    CameraPreviewView(modifier = Modifier.fillMaxSize())
                }
                else -> {
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

// -------------------------
// Camera preview (original simple view kept for back-compat)
// -------------------------
@Composable
fun CameraPreviewView(modifier: Modifier = Modifier) {
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
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                } catch (e: Exception) {
                    Log.e("CameraPreviewView", "Failed to bind camera use cases", e)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = modifier
    )
}

// -------------------------
// Permission helpers (unchanged)
// -------------------------
@Composable
fun RequestCameraPermission(onGranted: () -> Unit) {
    val context = LocalContext.current
    var permissionRequested by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) onGranted() }

    LaunchedEffect(Unit) {
        if (!permissionRequested) {
            permissionRequested = true
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
}

// -------------------------
// Location helpers (kept, note startScan signature uses currentLocation)
// -------------------------
fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

@SuppressLint("MissingPermission")
fun fetchLocationAndStartScan(context: Context, viewModel: CameraScanViewModel) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            val lat = location.latitude
            val lng = location.longitude

            val address = try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    addresses[0].locality ?: addresses[0].subAdminArea ?: "Unknown Location"
                } else {
                    "Unknown Location"
                }
            } catch (e: Exception) {
                "Unknown Location"
            }

            // NOTE: uses your signature currentLocation
            viewModel.startScan(currentLocation = address, latitude = lat, longitude = lng)
        } else {
            viewModel.startScan(null, null, null)
        }
    }.addOnFailureListener {
        viewModel.startScan(null, null, null)
    }
}

// -------------------------
// ML: FaceDetectorHelper (ML Kit wrapper)
// -------------------------

@OptIn(androidx.camera.core.ExperimentalGetImage::class)
class FaceDetectorHelper {
    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .enableTracking()
        .build()

    private val detector = FaceDetection.getClient(options)

    /**
     * Processes an ImageProxy and returns faces + InputImage.
     * This function closes imageProxy in its completion block to avoid leaks.
     */
    fun process(imageProxy: ImageProxy, onResult: (faces: List<Face>, inputImage: InputImage) -> Unit) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val rotation = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotation)
        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                onResult(faces, inputImage)
            }
            .addOnFailureListener { t ->
                Log.w("FaceDetectorHelper", "face detection fail", t)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}

// -------------------------
// ML: Bitmap ↔ ByteBuffer converter for quantized grayscale model
// -------------------------
fun bitmapToGrayscaleByteBuffer(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): ByteBuffer {
    val resized = if (bitmap.width != targetWidth || bitmap.height != targetHeight) {
        Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    } else {
        bitmap
    }

    val byteBuffer = ByteBuffer.allocateDirect(targetWidth * targetHeight)
    byteBuffer.order(ByteOrder.nativeOrder())

    val pixels = IntArray(targetWidth * targetHeight)
    resized.getPixels(pixels, 0, targetWidth, 0, 0, targetWidth, targetHeight)

    for (px in pixels) {
        val r = (px shr 16) and 0xFF
        val g = (px shr 8) and 0xFF
        val b = px and 0xFF
        val y = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
        byteBuffer.put((y and 0xFF).toByte())
    }
    byteBuffer.rewind()
    return byteBuffer
}

// -------------------------
// ML: FaceEmotionAnalyzer (detect face -> crop -> classify)
// -------------------------
class FaceEmotionAnalyzer(
    private val context: Context,
    private val classifier: EmotionClassifier,
    private val inputWidth: Int = 48,
    private val inputHeight: Int = 48,
    private val onResult: (predictions: List<PredictionResult>?, imageSize: Size?, faceBoxInImage: Rect?) -> Unit
) : ImageAnalysis.Analyzer {

    private val faceHelper = FaceDetectorHelper()

    override fun analyze(image: ImageProxy) {
        // Use FaceDetectorHelper; it closes the imageProxy for us
        faceHelper.process(image) { faces, inputImage ->
            if (faces.isEmpty()) {
                onResult(null, null, null)
                return@process
            }

            // Use first face (tracked/closest)
            val face = faces.first()
            val box = face.boundingBox // in image pixel coordinates (InputImage's coordinate system)
            // Try to extract bitmap from InputImage via reflection (pragmatic)
            val bitmap = inputImageToBitmap(inputImage)
            if (bitmap == null) {
                // fallback: use mediaImage -> YUV -> Bitmap conversion could be implemented, but skip here
                Log.w("FaceEmotionAnalyzer", "InputImage bitmap extraction failed")
                onResult(null, Size(inputImage.width.toFloat(), inputImage.height.toFloat()), box)
                return@process
            }

            // Crop face safely
            val cropped = cropBoxFromBitmap(bitmap, box) ?: run {
                onResult(null, Size(inputImage.width.toFloat(), inputImage.height.toFloat()), box)
                return@process
            }

            // Convert to grayscale ByteBuffer
            val bb = bitmapToGrayscaleByteBuffer(cropped, inputWidth, inputHeight)

            // Classify (catch exceptions)
            try {
                val preds = classifier.classify(bb)
                onResult(preds, Size(inputImage.width.toFloat(), inputImage.height.toFloat()), box)
            } catch (t: Throwable) {
                Log.w("FaceEmotionAnalyzer", "classification failed", t)
                onResult(null, Size(inputImage.width.toFloat(), inputImage.height.toFloat()), box)
            }
        }
    }

    /** Try to read private bitmap from InputImage (works in many ML Kit versions). */
    private fun inputImageToBitmap(inputImage: InputImage): Bitmap? {
        return try {
            val f = InputImage::class.java.getDeclaredField("bitmap")
            f.isAccessible = true
            (f.get(inputImage) as? Bitmap)
        } catch (e: Exception) {
            Log.w("FaceEmotionAnalyzer", "reflection extraction failed", e)
            null
        }
    }

    private fun cropBoxFromBitmap(src: Bitmap, box: Rect): Bitmap? {
        val left = max(0, box.left)
        val top = max(0, box.top)
        val right = min(src.width, box.right)
        val bottom = min(src.height, box.bottom)
        val w = right - left
        val h = bottom - top
        if (w <= 0 || h <= 0) return null
        return Bitmap.createBitmap(src, left, top, w, h)
    }
}

fun mapImageRectToViewRect(
    faceRect: Rect,
    viewWidth: Int,
    viewHeight: Int,
    imageWidth: Int,
    imageHeight: Int,
): Rect {
    val scaleX = viewWidth.toFloat() / imageWidth
    val scaleY = viewHeight.toFloat() / imageHeight

    return Rect(
        (faceRect.left * scaleX).toInt(),
        (faceRect.top * scaleY).toInt(),
        (faceRect.right * scaleX).toInt(),
        (faceRect.bottom * scaleY).toInt()
    )
}


// -------------------------
// CameraPreviewViewWithML: binds Preview + ImageAnalysis + FaceEmotionAnalyzer
// - Exposes previewView (so overlay mapping can happen)
// - onPredUpdate: live label String
// - onPredFull: List<PredictionResult> for more info
// - onFaceBox: face rectangle in image coordinates + imageSize so we can map to PreviewView
// -------------------------
@Composable
fun CameraPreviewViewWithML(
    modifier: Modifier = Modifier,
    classifier: EmotionClassifier,
    onPredUpdate: (String) -> Unit,
    onPredFull: (List<PredictionResult>?) -> Unit,
    onFaceBox: (faceRectInPreview: RectF?) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }

    // previewViewRef to measure size later (for mapping)
    var previewViewRef: PreviewView? = null
    var previewViewSize by remember { mutableStateOf(IntSize(0, 0)) }

    AndroidView(factory = { ctx ->
        val previewView = PreviewView(ctx).apply {
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
        previewViewRef = previewView

        // Camera provider
        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }

                val analyzer = FaceEmotionAnalyzer(ctx, classifier, 48, 48) { preds, imageSize, faceBox ->
                    // Called on ML Kit completion thread. Map faceBox from image coords -> preview coords and update callbacks.
                    // post to main thread using previewView.post
                    previewView.post {
                        if (faceBox == null || imageSize == null) {
                            onPredUpdate("No face")
                            onPredFull(null)
                            onFaceBox(null)
                        } else {
                            // Map image coords -> previewView coords
                            val mapped = mapImageRectToViewRect(faceBox, imageSize, previewView)
                            // choose top label if preds present
                            val top = preds?.maxByOrNull { it.confidence }
                            val label = if (top != null) "${top.label} (${(top.confidence * 100).toInt()}%)" else "No prediction"
                            onPredUpdate(label)
                            onPredFull(preds)
                            onFaceBox(mapped)
                        }
                    }
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .build()
                    .also { it.setAnalyzer(executor, analyzer) }

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
            } catch (e: Exception) {
                Log.e("CameraPreviewViewWithML", "bind camera fail", e)
            }
        }, ContextCompat.getMainExecutor(ctx))

        previewView
    }, modifier = modifier
        .onGloballyPositioned { coords ->
            previewViewSize = coords.size
        }
    )

    // Local helper: map image rect -> preview view rect using PreviewView transformation
    fun mapImageRectToViewRect(imageRect: Rect, imageSize: Size, previewView: PreviewView): RectF? {
        try {
            val viewW = previewView.width.toFloat()
            val viewH = previewView.height.toFloat()
            if (viewW == 0f || viewH == 0f) return null

            // ML Kit InputImage for mediaImage from CameraX has width = imageSize.width and height = imageSize.height
            // PreviewView.ScaleType.FILL_CENTER shows the rotated + scaled surface inside the view. We'll compute a simple letterbox-scale mapping.
            val imageW = imageSize.width
            val imageH = imageSize.height

            // Compute scale to fit image into view while preserving aspect ratio (FILL_CENTER crops to fill view).
            val scale = max(viewW / imageW, viewH / imageH) // fill center uses max; but surface may be rotated for front camera - handle rotation separately if needed

            // Center offset
            val scaledImageW = imageW * scale
            val scaledImageH = imageH * scale
            val dx = (viewW - scaledImageW) / 2f
            val dy = (viewH - scaledImageH) / 2f

            // Map image rect to view:
            val left = imageRect.left * scale + dx
            val top = imageRect.top * scale + dy
            val right = imageRect.right * scale + dx
            val bottom = imageRect.bottom * scale + dy

            return RectF(left, top, right, bottom)
        } catch (e: Exception) {
            Log.w("mapImageRectToViewRect", "mapping failed", e)
            return null
        }
    }
}

// -------------------------
// Full CameraScanScreen (integrated ML + original features)
// -------------------------
@Composable
fun CameraScanScreen(
    navController: NavHostController,
    database: AppDatabase // <-- DB passed from outside
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

    // Provide your userId
    val userId = remember { "default_user" }  // TODO replace with real user

    // Create ViewModel via Factory
    val viewModel: CameraScanViewModel = viewModel(
        factory = CameraScanViewModel.Factory(
            journalRepo,
            statsRepo,
            userId
        )
    )

    // Collect UI state from ViewModel
    val detectedEmotion by viewModel.detectedEmotion.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()

    // Permission flow for CAMERA
    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    // LOCATION permission launcher (for runtime pop-up)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (isGranted) {
            // Permission just granted, now fetch location and start scan
            fetchLocationAndStartScan(context, viewModel)
        } else {
            // Permission denied, start scan without location
            Toast.makeText(context, "Location denied. Saving without location.", Toast.LENGTH_SHORT).show()
            viewModel.startScan(null, null, null)
        }
    }

    if (!permissionGranted) {
        RequestCameraPermission(onGranted = { permissionGranted = true })
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

    // Load classifier once
    val classifier = remember { EmotionClassifier(context) }

    // Live ML UI state
    var livePrediction by remember { mutableStateOf("Detecting...") }
    var liveFaceBox by remember { mutableStateOf<RectF?>(null) }
    var lastPredictions by remember { mutableStateOf<List<PredictionResult>?>(null) }

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

                // Camera + overlay box — keep aspect ratio and styling consistent
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    // Preview with ML analyzer
                    CameraPreviewViewWithML(
                        modifier = Modifier.fillMaxSize(),
                        classifier = classifier,
                        onPredUpdate = { label -> livePrediction = label },
                        onPredFull = { preds -> lastPredictions = preds },
                        onFaceBox = { rect -> liveFaceBox = rect }
                    )

                    // Overlay: bounding box & label (mapped to PreviewView coords by CameraPreviewViewWithML)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val box = liveFaceBox
                        if (box != null) {
                            // Draw bounding box
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.0f),
                                topLeft = Offset(box.left, box.top),
                                size = Size(box.width(), box.height()),
                                cornerRadius = CornerRadius(8f, 8f),
                                style = Stroke(width = 4f, miter = 10f)
                            )
                            // draw label background
                            val label = livePrediction
                            val padding = 8f
                            val textWidth = 220f.coerceAtMost(size.width - 20f)
                            drawRoundRect(
                                color = Color.Black.copy(alpha = 0.5f),
                                topLeft = Offset(box.left, max(0f, box.top - 36f)),
                                size = Size(textWidth, 36f),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                            // draw text using Android canvas is not available here; instead use Compose overlay below
                        }
                    }

                    // Label via Compose (bottom-center)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Text(
                            text = livePrediction,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.45f), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Keep your original card and button
                MoodCameraCard(
                    isScanning = isScanning,
                    scanProgress = scanProgress,
                    hasCameraPermission = permissionGranted
                )

                ScanActionButton(
                    showCameraPreview = !isScanning,
                    onStartScan = {
                        // If we already have location permission -> fetch & start scan with location
                        if (hasLocationPermission(context)) {
                            // When startScan, attach lastPredictions top label if present
                            val top = lastPredictions?.maxByOrNull { it.confidence }?.label
                            // You may want to pass the predicted emotion into the viewModel; depends on signature
                            // For now we keep behaviour: fetch location then call viewModel.startScan(currentLocation = ..)
                            fetchLocationAndStartScan(context, viewModel)
                            // Optionally, save predicted emotion into viewModel via a setter (if implemented)
                            if (top != null) {
                                viewModel.setDetectedEmotion(top) // if your ViewModel exposes this; else ignore
                            }
                        } else {
                            // Trigger runtime permission popup
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
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
                    navController.navigate(
                        Screen.Reflection.createRoute(
                            entryId = "new_scan",
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
