package com.fredcodecrafts.moodlens.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object CameraScan : Screen("camera_scan")
    object Journal : Screen("journal")
    object MoodMap : Screen("mood_map")
    object Insights : Screen("insights")
    object Reflection : Screen("reflection/{entryId}/{mood}") {
        fun createRoute(entryId: String, mood: String): String {
            return "reflection/$entryId/$mood"
        }
    }    object UserDetail : Screen("user_detail")
}
