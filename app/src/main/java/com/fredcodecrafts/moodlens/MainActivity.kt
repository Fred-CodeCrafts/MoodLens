package com.fredcodecrafts.moodlens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.fredcodecrafts.moodlens.database.AppDatabase
import com.fredcodecrafts.moodlens.database.DummyData
import com.fredcodecrafts.moodlens.database.PreloadedQuestions
import com.fredcodecrafts.moodlens.navigation.AppNavHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.fredcodecrafts.moodlens.ui.theme.MoodLensTheme
import com.fredcodecrafts.moodlens.utils.InAppNotification
import com.fredcodecrafts.moodlens.utils.rememberNotificationState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.fredcodecrafts.moodlens.utils.SessionManager
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    // Location permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                // Fine location permission granted - best accuracy
                // You can notify user or update UI here if needed
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                // Coarse location permission granted - approximate location
                // Still good enough for mood map functionality
            }
            else -> {
                // No location access granted
                // Mood map will still work but won't capture new locations
                // Existing location data will still be displayed
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(this)
        val session = SessionManager(this)

        // Request location permissions if not already granted
        requestLocationPermissionsIfNeeded()

        // Determine start destination here
        val startDestination = if (session.isLoggedIn()) {
            "home_screen"   // ⬅️ change to your actual home route
        } else {
            "login_screen"  // ⬅️ change to your actual login route
        }

        // Preload dummy data (currently disabled)
        /*
        lifecycleScope.launch(Dispatchers.IO) {
            if (db.userDao().getAllUsers().isEmpty()) db.userDao().insertAll(DummyData.users)
            if (db.journalDao().getAllEntries().isEmpty()) db.journalDao().insertAll(DummyData.journalEntries)
            if (db.notesDao().getAllNotes().isEmpty()) db.notesDao().insertAll(DummyData.notes)
            if (db.messagesDao().getAllMessages().isEmpty()) db.messagesDao().insertAll(DummyData.messages)
            if (db.moodScanStatDao().getAllStats().isEmpty()) db.moodScanStatDao().insertAll(DummyData.moodScanStats)
            if (db.questionsDao().getAllQuestions().isEmpty()) db.questionsDao().insertAll(PreloadedQuestions.questions)
        }
        */

        setContent {
            MoodLensTheme {
                val navController = rememberNavController()
                val notificationState = rememberNotificationState()

                Surface(color = MaterialTheme.colorScheme.background) {
                    Box(modifier = Modifier.fillMaxSize()) {

                        AppNavHost(
                            navController = navController,
                            database = db,
                            notificationState = notificationState,
                        )

                        InAppNotification(
                            state = notificationState,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }
            }
        }
    }

    /**
     * Checks if location permissions are granted and requests them if needed.
     * This is necessary for the Mood Map feature to capture location data.
     */
    private fun requestLocationPermissionsIfNeeded() {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // Only request if neither permission is granted
        if (!fineLocationGranted && !coarseLocationGranted) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    /**
     * Helper function to check if location permissions are granted.
     * You can use this in other parts of your app to check permission status.
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
}