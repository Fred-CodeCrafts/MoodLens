package com.fredcodecrafts.moodlens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.fredcodecrafts.moodlens.database.AppDatabase
import com.fredcodecrafts.moodlens.database.viewmodel.MoodMapViewModel
import com.fredcodecrafts.moodlens.database.viewmodel.MoodMapViewModelFactory
import com.fredcodecrafts.moodlens.navigation.Screen
import com.fredcodecrafts.moodlens.ui.theme.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import android.os.Bundle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.window.Dialog
import com.fredcodecrafts.moodlens.database.repository.MoodMapRepository
import createMoodBubble
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.unit.sp
import android.Manifest // <--- Added
import android.content.pm.PackageManager // <--- Added
import androidx.activity.compose.rememberLauncherForActivityResult // <--- Added
import androidx.activity.result.contract.ActivityResultContracts // <--- Added
import androidx.core.content.ContextCompat // <--- Added
import com.google.android.gms.location.LocationServices // <--- Added

data class MoodLocation(
    val id: Long,
    val mood: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val note: String? = null
)

data class MoodCluster(
    val latitude: Double,
    val longitude: Double,
    val moods: List<MoodLocation>,
    val dominantMood: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodMapScreen(
    navController: NavHostController,
    database: AppDatabase,
    userId: String
) {
    val context = LocalContext.current

    val repository = remember {
        MoodMapRepository(
            journalDao = database.journalDao()
        )
    }
    val factory = remember(userId) { MoodMapViewModelFactory(repository, userId) }
    val viewModel: MoodMapViewModel = viewModel(factory = factory)

    val moodLocations by viewModel.moodLocations.collectAsState()
    val clusters by viewModel.clusters.collectAsState()

    var selectedCluster by remember { mutableStateOf<MoodCluster?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedMoodFilter by remember { mutableStateOf<String?>(null) }

    val mapView = remember { MapView(context) }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }

    // --- Location Permission & Client ---
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    DisposableEffect(Unit) {
        mapView.onCreate(Bundle())
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDestroy()
        }
    }

    // Move camera to user location once permission granted
    LaunchedEffect(googleMap, hasLocationPermission) {
        googleMap?.let { map ->
            if (hasLocationPermission) {
                try {
                    map.isMyLocationEnabled = true
                    map.uiSettings.isMyLocationButtonEnabled = true

                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            val userLat = location.latitude
                            val userLng = location.longitude
                            val userPos = LatLng(userLat, userLng)
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(userPos, 15f))
                        }
                    }
                } catch (e: SecurityException) {
                    // Handle exception
                }
            }
        }
    }

    // React to filter changes by asking ViewModel to re-cluster
    LaunchedEffect(selectedMoodFilter) {
        viewModel.filterByMood(selectedMoodFilter)
    }

    LaunchedEffect(clusters, googleMap) {
        googleMap?.let { map ->
            map.clear()
            
            // Use clusters directly from ViewModel (already filtered)
            clusters.forEach { cluster ->
                val hueColor = getMoodMarkerColor(cluster.dominantMood)
                val customIcon = createMoodBubble(
                    context = context,
                    mood = cluster.dominantMood,
                    backgroundColor = hueColor
                )

                val markerOptions = MarkerOptions()
                    .position(LatLng(cluster.latitude, cluster.longitude))
                    .title(cluster.dominantMood)
                    .snippet("${cluster.moods.size} entries")
                    .icon(customIcon)
                    .anchor(0.5f, 0.5f)

                map.addMarker(markerOptions)?.tag = cluster
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackground)
        ) {
            // Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.MoodMap.route) { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }

                    Text(
                        text = "Mood Map",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )

                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filter",
                            tint = MainPurple
                        )
                    }
                }
            }

            // Map View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize()
                ) { view ->
                    view.getMapAsync { map ->
                        googleMap = map
                        map.uiSettings.isZoomControlsEnabled = true
                        map.uiSettings.isCompassEnabled = true
                        
                        // RAISE ZOOM CONTROLS
                        // left, top, right, bottom padding
                        // Raising bottom approx 100dp to clear the stats card
                         val density = context.resources.displayMetrics.density
                         val bottomPadding = (130 * density).toInt() 
                         map.setPadding(0, 0, 0, bottomPadding)

                        map.setOnMarkerClickListener { marker ->
                            selectedCluster = marker.tag as? MoodCluster
                            true
                        }
                    }
                }

                // Legend
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Legend",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                        MapLegendItem("Happy", "😊", getMoodColor("happy"))
                        MapLegendItem("Calm", "😌", getMoodColor("calm"))
                        MapLegendItem("Neutral", "😐", getMoodColor("neutral"))
                        MapLegendItem("Stressed", "😖", getMoodColor("stressed"))
                        MapLegendItem("Sad", "😢", getMoodColor("sad"))
                    }
                }

                // Stats Card
                if (moodLocations.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 44.dp, start = 52.dp, end = 52.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            // REMOVED horizontal padding here so the weights use the full width
                            modifier = Modifier.padding(vertical = 10.dp),
                            // REMOVED Arrangement.SpaceBetween
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            // --- ITEM 1 ---
                            Box(
                                modifier = Modifier.weight(1f), // Force 33% width
                                contentAlignment = Alignment.Center
                            ) {
                                MapStatItem("Total", moodLocations.size.toString())
                            }

                            VerticalDivider(
                                modifier = Modifier.height(24.dp),
                                color = Color.LightGray
                            )

                            // --- ITEM 2 ---
                            Box(
                                modifier = Modifier.weight(1f), // Force 33% width
                                contentAlignment = Alignment.Center
                            ) {
                                MapStatItem("Locations", clusters.size.toString())
                            }

                            VerticalDivider(
                                modifier = Modifier.height(24.dp),
                                color = Color.LightGray
                            )

                            // --- ITEM 3 ---
                            Box(
                                modifier = Modifier.weight(1f), // Force 33% width
                                contentAlignment = Alignment.Center
                            ) {
                                MapStatItem(
                                    "Common",
                                    clusters.groupBy { it.dominantMood }
                                        .maxByOrNull { it.value.size }?.key ?: "-"
                                )
                            }
                        }
                    }
                }
            }
        }

        // Cluster Detail Dialog
        selectedCluster?.let { cluster ->
            Dialog(onDismissRequest = { selectedCluster = null }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = getMoodProfile(cluster.dominantMood).emoji,
                                style = MaterialTheme.typography.displayMedium
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = cluster.dominantMood,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${cluster.moods.size} entries here",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = LightGray)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "All Moods at This Location",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            modifier = Modifier.heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(cluster.moods.sortedByDescending { it.timestamp }) { mood ->
                                MoodEntryItem(mood)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { selectedCluster = null },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MainPurple
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Close", color = Color.White)
                        }
                    }
                }
            }
        }

        // Filter Dialog
        if (showFilterDialog) {
            Dialog(onDismissRequest = { showFilterDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Filter by Mood",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val moods = listOf("Happy", "Calm", "Excited", "Neutral", "Tired", "Stressed", "Anxious", "Sad", "Angry")

                        FilterChip(
                            selected = selectedMoodFilter == null,
                            onClick = {
                                selectedMoodFilter = null
                                showFilterDialog = false
                            },
                            label = { Text("All Moods") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        moods.forEach { mood ->
                            FilterChip(
                                selected = selectedMoodFilter == mood,
                                onClick = {
                                    selectedMoodFilter = mood
                                    showFilterDialog = false
                                },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(getMoodProfile(mood).emoji)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(mood)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

// Renamed to avoid conflict with InsightsScreen
@Composable
fun MapLegendItem(label: String, emoji: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = emoji, style = MaterialTheme.typography.bodySmall)
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

// Renamed to avoid conflict with InsightsScreen
@Composable
fun MapStatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        // valid way to tighten items if you previously had `spacedBy`
        verticalArrangement = Arrangement.spacedBy((-6).dp) // Optional: Negative spacing to pull them very close
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                // 1. Remove default font padding
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                // 2. Tighter line height
                lineHeight = 16.sp
            ),
            color = MainPurple
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                // 1. Remove default font padding
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                // 2. Tighter line height
                lineHeight = 12.sp
            ),
            color = TextSecondary
        )
    }
}

@Composable
fun MoodEntryItem(mood: MoodLocation) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MutedNeutral,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getMoodColor(mood.mood)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getMoodProfile(mood.mood).emoji,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mood.mood,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
                Text(
                    text = formatTimestamp(mood.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                mood.note?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

// Map-specific helper functions
fun getMoodMarkerColor(mood: String): Float = when (mood.lowercase()) {
    "happy", "excited" -> BitmapDescriptorFactory.HUE_GREEN
    "calm" -> BitmapDescriptorFactory.HUE_CYAN
    "neutral" -> BitmapDescriptorFactory.HUE_BLUE
    "tired" -> BitmapDescriptorFactory.HUE_AZURE
    "stressed", "anxious" -> BitmapDescriptorFactory.HUE_ORANGE
    "sad" -> BitmapDescriptorFactory.HUE_VIOLET
    "angry" -> BitmapDescriptorFactory.HUE_RED
    else -> BitmapDescriptorFactory.HUE_BLUE
}

fun getMoodColor(mood: String): Color = when (mood.lowercase()) {
    "happy", "excited" -> Color(0xFF4CAF50)
    "calm" -> Color(0xFF2196F3)
    "neutral" -> Color(0xFF9E9E9E)
    "tired" -> Color(0xFF607D8B)
    "stressed", "anxious" -> Color(0xFFFF9800)
    "sad" -> Color(0xFF9C27B0)
    "angry" -> Color(0xFFF44336)
    else -> Color(0xFF9E9E9E)
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
        hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        minutes > 0 -> "$minutes min${if (minutes > 1) "s" else ""} ago"
        else -> "Just now"
    }
}
