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

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    database: AppDatabase
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // Navigate to home after login
                    navController.navigate(Screen.CameraScan.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onSkipDemo = {
                    // Navigate to home skipping login
                    navController.navigate(Screen.CameraScan.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            MainMenuScreen(navController = navController, database = database)
        }
        composable(Screen.CameraScan.route) { CameraScanScreen() }
        composable(Screen.Journal.route) {
            // Provide all required parameters
            JournalScreen(
                navController = navController,
//                context = LocalContext.current, // Get context here
//                userId = "default_user" // Provide the userId
            )
        }
        composable(Screen.Insights.route) {
            InsightsScreen(navController = navController, database = database)
        }
        composable(
            route = Screen.Reflection.route,
            arguments = listOf(
                navArgument("entryId") {
                    type = NavType.StringType
                },
                navArgument("mood") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId") ?: ""
            val currentMood = backStackEntry.arguments?.getString("mood") ?: "neutral"

            ReflectionScreen(
                entryId = entryId,
                currentMood = currentMood,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onReflection = { reflection ->
                    // Save reflection to database or handle as needed
                    // You can pass this back to JournalScreen if needed
                    println("AI Reflection saved: $reflection")

                    // Navigate back to Journal after saving
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.UserDetail.route) {
            UserDetailScreen(navController = navController, database = database)
        }
    }
}
