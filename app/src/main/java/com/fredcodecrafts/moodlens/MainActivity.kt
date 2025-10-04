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
            val navController = rememberNavController()
            Surface(color = MaterialTheme.colorScheme.background) {
                AppNavHost(
                    navController = navController,
                    database = db
                )
            }
        }
    }
}
