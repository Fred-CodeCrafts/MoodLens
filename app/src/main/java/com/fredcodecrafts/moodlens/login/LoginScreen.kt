package com.fredcodecrafts.moodlens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fredcodecrafts.moodlens.components.*
import com.fredcodecrafts.moodlens.utils.NotificationState
import com.fredcodecrafts.moodlens.utils.NotificationType
import com.fredcodecrafts.moodlens.utils.NotificationData
import com.fredcodecrafts.moodlens.utils.InAppNotification

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit = {}) {
    // For testing notifications
    val notificationState = NotificationState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        AppCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF673AB7),
        ) {
            CardHeader {
                CardTitle(
                    text = "Stats Overview",
                    textColor = Color.White
                )
                CardDescription(
                    text = "Week Summary",
                    textColor = Color.White.copy(alpha = 0.8f)
                )
            }

            CardContent {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatsColumn(
                        value = "13",
                        label = "Total Entries",
                        textColor = Color.White
                    )
                    StatsColumn(
                        value = "0",
                        label = "With Notes",
                        textColor = Color.White
                    )
                    StatsColumn(
                        value = "4",
                        label = "Days Tracked",
                        textColor = Color.White
                    )
                }
            }

            CardFooter {
                Button(onClick = onLoginSuccess) {
                    Text(text = "Login (dummy)")
                }

                Spacer(modifier = Modifier.width(16.dp))

                // New button to trigger a test notification
                Button(onClick = {
                    notificationState.showNotification(
                        NotificationData(
                            id = System.currentTimeMillis().toString(),
                            title = "Test Notification",
                            message = "This is a sample success message",
                            type = NotificationType.SUCCESS,
                            duration = 3000
                        )
                    )
                }) {
                    Text(text = "Test Notification")
                }
            }
        }
    }

    // Display in-app notification
    InAppNotification(state = notificationState)
}
