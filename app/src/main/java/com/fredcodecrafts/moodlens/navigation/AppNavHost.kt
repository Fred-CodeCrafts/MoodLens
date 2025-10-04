package com.fredcodecrafts.moodlens.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen { navController.navigate(Screen.Home.route) }
        }
        composable(Screen.Home.route) {
            MainMenu(navController = navController, database = database)
        }
        composable(Screen.CameraScan.route) { CameraScanScreen() }
        composable(Screen.Journal.route) { JournalScreen() }
        composable(Screen.Insights.route) { InsightsScreen() }
        composable(Screen.Reflection.route) { ReflectionScreen() }
        composable(Screen.UserDetail.route) {
            UserDetailScreen(navController = navController, database = database)
        }
    }
}
