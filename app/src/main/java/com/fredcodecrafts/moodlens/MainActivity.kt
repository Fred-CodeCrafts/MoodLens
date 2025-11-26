package com.fredcodecrafts.moodlens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(this)
        val session = SessionManager(this)

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
}
