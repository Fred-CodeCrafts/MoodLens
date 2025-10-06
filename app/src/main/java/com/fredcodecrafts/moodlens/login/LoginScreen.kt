package com.fredcodecrafts.moodlens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fredcodecrafts.moodlens.components.*
import com.fredcodecrafts.moodlens.utils.*

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit = {}) {
    val notificationState = NotificationState()

    // For text inputs
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // For progress sample
    var progress by remember { mutableStateOf(0.4f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // --- 1️⃣ Progress Card Example ---
        WellnessProgressCard(
            progress = progress,
            title = "Login Progress",
            description = "Simulated account setup progress"
        )

        // --- 2️⃣ Input Fields ---
        InputField(
            value = username,
            onValueChange = { username = it },
            label = "Username",
            placeholder = "Enter your username"
        )

        InputField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            placeholder = "Enter your password"
        )

        // --- 3️⃣ Text Area for Notes or Comments ---
        TextAreaField(
            text = notes,
            placeholder = "Any notes or remarks...",
            modifier = Modifier.fillMaxWidth()
        )

        // --- 4️⃣ Buttons ---
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    // Simulate login success
                    onLoginSuccess()
                    progress = 1f // show full progress
                    notificationState.showNotification(
                        NotificationData(
                            id = System.currentTimeMillis().toString(),
                            title = "Login Successful!",
                            message = "Welcome, $username 👋",
                            type = NotificationType.SUCCESS,
                            duration = 3000
                        )
                    )
                }
            ) {
                Text("Login")
            }


        }
    }

    // --- 5️⃣ Notification Overlay ---
    InAppNotification(state = notificationState)
}
