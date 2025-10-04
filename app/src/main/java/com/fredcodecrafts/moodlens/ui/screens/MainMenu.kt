package com.fredcodecrafts.moodlens.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fredcodecrafts.moodlens.database.AppDatabase
import com.fredcodecrafts.moodlens.navigation.Screen

@Composable
fun MainMenu(
    navController: NavHostController,
    database: AppDatabase
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Welcome to the Main Menu")

        // Navigate to Camera Scan screen
        Button(onClick = { navController.navigate(Screen.CameraScan.route) }) {
            Text("Go to Camera Scan")
        }

        // Navigate to User Detail screen
        Button(onClick = { navController.navigate(Screen.UserDetail.route) }) {
            Text("Go to User Detail")
        }

        // Example: you can add more buttons here for Journal, Insights, Reflection, etc.
    }
}
