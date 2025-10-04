package com.fredcodecrafts.moodlens.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object CameraScan : Screen("camera_scan")
    object Journal : Screen("journal")
    object Insights : Screen("insights")
    object Reflection : Screen("reflection")
    object UserDetail : Screen("user_detail")
}
