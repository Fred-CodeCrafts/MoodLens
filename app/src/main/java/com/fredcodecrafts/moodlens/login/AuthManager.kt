package com.fredcodecrafts.moodlens.login

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AuthManager {

    private val supabaseUrl = "https://cglkbjwuvmakmamkcfww.supabase.co"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNnbGtiand1dm1ha21hbWtjZnd3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjMyOTM1ODYsImV4cCI6MjA3ODg2OTU4Nn0.Yt2I8ELwfUT3sKD9PEMy5JgNGAbhnZ_gCXRN-m2a5Y8" // Move to secure storage for production

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    suspend fun signInWithGoogle(idToken: String, email: String): Boolean {
        return try {
            val tableUrl = "$supabaseUrl/rest/v1/app_users"

            // Check if user exists
            val existing = client.get(tableUrl) {
                url { parameters.append("user_id", "eq.$email") } // user_id stores email
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
            }

            if (existing.bodyAsText() == "[]") {
                val response = client.post(tableUrl) {
                    header("apikey", supabaseKey)
                    header("Authorization", "Bearer $supabaseKey")
                    contentType(ContentType.Application.Json)
                    setBody(AppUser(userId = email, googleId = idToken))
                }
                Log.d("AuthManager", "Inserted new user: ${response.status}")
            } else {
                Log.d("AuthManager", "User already exists")
            }
            true
        } catch (e: Exception) {
            Log.e("AuthManager", "Error signing in with Google", e)
            false
        }
    }

    @Serializable
    data class AppUser(
        @SerialName("user_id") val userId: String,
        @SerialName("google_id") val googleId: String
    )
}
