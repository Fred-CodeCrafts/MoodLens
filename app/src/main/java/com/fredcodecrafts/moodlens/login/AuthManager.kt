package com.fredcodecrafts.moodlens.login

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AuthManager {

    private val supabaseUrl = "https://cglkbjwuvmakmamkcfww.supabase.co"
    private val supabaseKey = "YOUR_ANON_KEY_HERE"

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    suspend fun signInWithGoogle(idToken: String, email: String): Boolean {
        return try {
            val tableUrl = "$supabaseUrl/rest/v1/users"

            // Check user
            val existing = client.get(tableUrl) {
                url { parameters.append("email", "eq.$email") }
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
            }

            if (existing.bodyAsText() == "[]") {
                val response = client.post(tableUrl) {
                    header("apikey", supabaseKey)
                    header("Authorization", "Bearer $supabaseKey")
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
