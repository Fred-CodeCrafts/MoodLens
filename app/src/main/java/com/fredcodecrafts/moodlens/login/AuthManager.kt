package com.fredcodecrafts.moodlens.login

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AuthManager {

    private val postgrestUrl = "https://cglkbjwuvmakmamkcfww.supabase.co"
    private val postgrestKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNnbGtiand1dm1ha21hbWtjZnd3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjMyOTM1ODYsImV4cCI6MjA3ODg2OTU4Nn0.Yt2I8ELwfUT3sKD9PEMy5JgNGAbhnZ_gCXRN-m2a5Y8"

    private val client = HttpClient(Android) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    /**
     * Sign in using Google ID token + email
     */
    suspend fun signInWithGoogle(idToken: String, email: String): Boolean {
        return try {
            // Check if user exists
            val existing: HttpResponse = client.get("$postgrestUrl?email=eq.$email") {
                header("apikey", postgrestKey)
                header("Authorization", "Bearer $postgrestKey")
            }

            if (existing.bodyAsText() == "[]") {
                // Insert new user
                val response = client.post(postgrestUrl) {
                    header("apikey", postgrestKey)
                    header("Authorization", "Bearer $postgrestKey")
                    contentType(ContentType.Application.Json)
                    setBody(User(email = email, googleIdToken = idToken))
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
    data class User(
        val email: String,
        val googleIdToken: String
    )
}
