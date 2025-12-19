package com.fredcodecrafts.moodlens.utils

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("moodlens_prefs", Context.MODE_PRIVATE)

    init {
        // Hydrate static variables from SharedPreferences on initialization
        if (accessToken == null) {
            accessToken = prefs.getString("accessToken", null)
        }
        if (currentUserId == null) {
            currentUserId = prefs.getString("currentUserId", null)
        }
    }

    fun setLoggedIn(status: Boolean) {
        prefs.edit().putBoolean("isLoggedIn", status).apply()
    }

    fun saveUserSession(userId: String, token: String) {
        prefs.edit()
            .putString("currentUserId", userId)
            .putString("accessToken", token)
            .putBoolean("isLoggedIn", true)
            .apply()
        // Update companion/static for legacy access if needed, but prefer instance methods
        currentUserId = userId
        accessToken = token
    }

    fun getUserId(): String? {
        val storedId = prefs.getString("currentUserId", null)
        // Sync static cache if null but found in prefs
        if (currentUserId == null && storedId != null) {
            currentUserId = storedId
        }
        return storedId ?: currentUserId
    }

    fun getAccessToken(): String? {
         val storedToken = prefs.getString("accessToken", null)
         if (accessToken == null && storedToken != null) {
            accessToken = storedToken
         }
         return storedToken ?: accessToken
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("isLoggedIn", false)
    }

    fun logout() {
        prefs.edit().clear().apply()
        accessToken = null
        currentUserId = null
    }

    companion object {
        var accessToken: String? = null
        var currentUserId: String? = null
    }
}
