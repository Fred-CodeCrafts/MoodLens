package com.fredcodecrafts.moodlens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fredcodecrafts.moodlens.ui.theme.MoodLensTheme
import com.fredcodecrafts.moodlens.components.InputField
import com.fredcodecrafts.moodlens.components.TextAreaField
import com.fredcodecrafts.moodlens.ui.theme.MainBackground
import androidx.compose.ui.graphics.Color
// IMPORT NOTIFICATION:
import com.fredcodecrafts.moodlens.utils.GlobalNotificationHandler
import com.fredcodecrafts.moodlens.utils.rememberNotificationState
import com.fredcodecrafts.moodlens.utils.NotificationState
// TAMBAHKAN INI UNTUK FUNCTION EXTENSION:
import com.fredcodecrafts.moodlens.utils.showReflectionSaved
import com.fredcodecrafts.moodlens.utils.showInfo
import com.fredcodecrafts.moodlens.utils.showError
import com.fredcodecrafts.moodlens.utils.showWarning
import com.fredcodecrafts.moodlens.utils.showJournalEntrySaved

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val notificationState = rememberNotificationState()

            MoodLensTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        ShowcaseScreen(
                            modifier = Modifier.padding(innerPadding),
                            notificationState = notificationState
                        )
                    }

                    GlobalNotificationHandler(state = notificationState)
                }
            }
        }
    }
}

@Composable
fun ShowcaseScreen(
    modifier: Modifier = Modifier,
    notificationState: NotificationState? = null
) {
    var name by remember { mutableStateOf("") }
    var journalEntry by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Reflection Companion",
            style = MaterialTheme.typography.headlineMedium
        )

        InputField(
            value = name,
            onValueChange = { name = it },
            label = "Name",
            placeholder = "Enter your name"
        )

        TextAreaField(
            text = "A short note.",
            placeholder = "No text yet"
        )

        TextAreaField(
            text = "This is a much longer reflection entry that will expand the height " +
                    "naturally as needed. The width will be constrained between 120dp and 400dp."
        )

        // Tombol test notification
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    notificationState?.showReflectionSaved()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Success Notification")
            }

            Button(
                onClick = {
                    notificationState?.showInfo("This is an info notification")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Info Notification")
            }

            Button(
                onClick = {
                    notificationState?.showError("This is an error notification")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Error Notification")
            }

            Button(
                onClick = {
                    notificationState?.showWarning("This is a warning notification")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Warning Notification")
            }
        }

        Button(
            onClick = {
                notificationState?.showJournalEntrySaved()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Reflection")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShowcasePreview() {
    MoodLensTheme {
        ShowcaseScreen()
    }
}