package com.fredcodecrafts.moodlens.utils

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("moodlens_prefs", Context.MODE_PRIVATE)

    fun setLoggedIn(status: Boolean) {
        prefs.edit().putBoolean("isLoggedIn", status).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("isLoggedIn", false)
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
