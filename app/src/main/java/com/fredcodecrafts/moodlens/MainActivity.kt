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
import com.fredcodecrafts.moodlens.ui.theme.MoodLensTheme // ✅ Import Tema kamu
import com.fredcodecrafts.moodlens.utils.InAppNotification // ✅ Import komponen Notifikasi
import com.fredcodecrafts.moodlens.utils.rememberNotificationState
import androidx.compose.foundation.layout.Box // ✅ Import Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment // ✅ Import Alignment
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database
        val db = AppDatabase.getDatabase(this)

        // Preload dummy data safely using lifecycleScope
        lifecycleScope.launch(Dispatchers.IO) {
            if (db.userDao().getAllUsers().isEmpty()) db.userDao().insertAll(DummyData.users)
            if (db.journalDao().getAllEntries().isEmpty()) db.journalDao().insertAll(DummyData.journalEntries)
            if (db.notesDao().getAllNotes().isEmpty()) db.notesDao().insertAll(DummyData.notes)
            if (db.messagesDao().getAllMessages().isEmpty()) db.messagesDao().insertAll(DummyData.messages)
            if (db.moodScanStatDao().getAllStats().isEmpty()) db.moodScanStatDao().insertAll(DummyData.moodScanStats)
            if (db.questionsDao().getAllQuestions().isEmpty()) db.questionsDao().insertAll(PreloadedQuestions.questions)
        }

        // Set up Compose UI with NavController and AppNavHost
        setContent {
            MoodLensTheme { // ✅ Bungkus semuanya dengan Tema kamu
                val navController = rememberNavController()

                // 1. Buat notification state di sini
                val notificationState = rememberNotificationState()

                Surface(color = MaterialTheme.colorScheme.background) {
                    // 2. Bungkus AppNavHost dengan Box
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppNavHost(
                            navController = navController,
                            database = db,
                            notificationState = notificationState // <-- Kirim state ke NavHost
                        )

                        // 3. Tampilkan UI Notifikasi di atas segalanya
                        InAppNotification(
                            state = notificationState,
                            modifier = Modifier.align(Alignment.TopCenter) // Atur posisi
                        )
                    }
                }
            }
        }
    }
}
