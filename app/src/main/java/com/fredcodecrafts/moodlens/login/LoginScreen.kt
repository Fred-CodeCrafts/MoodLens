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

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit = {}) {
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
            }
        }
    }
}
