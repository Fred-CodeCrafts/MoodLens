package com.fredcodecrafts.moodlens.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fredcodecrafts.moodlens.database.AppDatabase
import com.fredcodecrafts.moodlens.ui.screens.*
import com.fredcodecrafts.moodlens.login.LoginScreen
import androidx.compose.ui.platform.LocalContext // <-- Pastikan import ini ada
import com.fredcodecrafts.moodlens.utils.NotificationState
import com.fredcodecrafts.moodlens.utils.NotificationData
import com.fredcodecrafts.moodlens.utils.NotificationType

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    database: AppDatabase,
    notificationState: NotificationState // <-- Sekarang tipe ini dikenali
) {
    NavHost(
        navController = navController,
        // Using Insights as the start destination as provided, but often Login or Home is used.
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // Navigate to CameraScan after successful login
                    navController.navigate(Screen.CameraScan.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onSkipDemo = {
                    // Navigate to CameraScan skipping login
                    navController.navigate(Screen.CameraScan.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            MainMenuScreen(navController = navController, database = database)
        }

        // 🚨 UPDATE: Pass the navController to CameraScanScreen
        composable(Screen.CameraScan.route) {
            CameraScanScreen(navController = navController, database = database)
        }

        composable(Screen.Journal.route) {
            val context = LocalContext.current

            JournalScreen(
                navController = navController,
                userId = "default_user",
                db = database
            )
        }

        composable(Screen.Insights.route) {
            InsightsScreen(navController = navController, database = database)
        }
        composable(
            route = Screen.Reflection.route,
            arguments = listOf(
                navArgument("entryId") { type = NavType.StringType },
                navArgument("mood") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId") ?: ""
            val currentMood = backStackEntry.arguments?.getString("mood") ?: "neutral"

            ReflectionScreen(
                entryId = entryId,
                currentMood = currentMood,
                database = database,   // ✅ ADD THIS
                onNavigateBack = {
                    navController.navigate(Screen.Journal.route) {
                        // This ensures you don't stack multiple journal screens
                        launchSingleTop = true

                        // This clears the reflection screen from the back stack
                        popUpTo(Screen.Reflection.route) {
                            inclusive = true
                        }
                    }
                },
                // 3. Modify the onReflection callback
                onReflectionComplete = { reflection ->
                    // Optional: Save reflection logic here or pass it up further
                    println("AI Reflection saved: $reflection")

                    // ✅ TRIGGER NOTIFICATION HERE
                    notificationState.showNotification(
                        NotificationData(
                            message = "Your reflection is successfully saved",
                            type = NotificationType.SUCCESS,
                            duration = 3000L // Show for 3 seconds
                        )
                    )

                    // Navigate back *after* triggering notification
//                    navController.popBackStack()
                },
                onFinishAndNavigate = {
                    navController.navigate(Screen.Journal.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
