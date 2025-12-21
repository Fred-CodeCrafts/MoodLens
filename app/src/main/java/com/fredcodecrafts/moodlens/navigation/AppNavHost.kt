package com.fredcodecrafts.moodlens.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fredcodecrafts.moodlens.database.AppDatabase
import com.fredcodecrafts.moodlens.login.LoginScreen
import com.fredcodecrafts.moodlens.ui.screens.*
import com.fredcodecrafts.moodlens.utils.NotificationState
import com.fredcodecrafts.moodlens.utils.NotificationData
import com.fredcodecrafts.moodlens.utils.NotificationType
import com.fredcodecrafts.moodlens.utils.SessionManager

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    database: AppDatabase,
    notificationState: NotificationState,
    mainViewModel: com.fredcodecrafts.moodlens.database.viewmodel.MainViewModel
) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)

    // ⚡ Auto redirect if logged in → Home
    val startDestination =
        if (sessionManager.isLoggedIn()) Screen.Home.route
        else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // =============== LOGIN SCREEN ===============
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    sessionManager.setLoggedIn(true)
                    // Trigger Sync on Login!
                    mainViewModel.syncNow()
                    
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onSkipDemo = {
                    sessionManager.setLoggedIn(false)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // =============== HOME ===============
        composable(Screen.Home.route) {
            MainMenuScreen(
                navController = navController,
                database = database,
                userId = sessionManager.getUserId() ?: "default_user"
            )
        }

        // =============== CAMERA SCAN ===============
        composable(Screen.CameraScan.route) {
            CameraScanScreen(
                navController = navController,
                database = database,
                userId = sessionManager.getUserId() ?: "default_user"
            )
        }

        // =============== JOURNAL ===============
        composable(Screen.Journal.route) {
            JournalScreen(
                navController = navController,
                userId = sessionManager.getUserId() ?: "default_user",
                db = database
            )
        }

        // ADD THIS NEW ROUTE FOR MOOD MAP
        composable(route = Screen.MoodMap.route) {
            MoodMapScreen(navController = navController, database = database)
        }

        // =============== INSIGHTS ===============
        composable(Screen.Insights.route) {
            InsightsScreen(
                navController = navController,
                database = database,
                userId = sessionManager.getUserId() ?: "default_user"
            )
        }

        // =============== REFLECTION SCREEN ===============
        composable(
            route = Screen.Reflection.route,
            arguments = listOf(
                navArgument("entryId") { type = NavType.StringType },
                navArgument("mood") { type = NavType.StringType }
            )
        ) { entry ->
            val entryId = entry.arguments?.getString("entryId") ?: ""
            val mood = entry.arguments?.getString("mood") ?: "neutral"

            ReflectionScreen(
                entryId = entryId,
                currentMood = mood,
                database = database,
                onNavigateBack = {
                    navController.navigate(Screen.Journal.route) {
                        launchSingleTop = true
                        popUpTo(Screen.Reflection.route) { inclusive = true }
                    }
                },
                onReflectionComplete = { reflection ->
                    notificationState.showNotification(
                        NotificationData(
                            message = "Your reflection is successfully saved",
                            type = NotificationType.SUCCESS,
                            duration = 3000L
                        )
                    )
                },
                onFinishAndNavigate = {
                    navController.navigate(Screen.Journal.route) {
                        launchSingleTop = true
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    }
                }
            )
        }
    }
}
