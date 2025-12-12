package com.fredcodecrafts.moodlens.login

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.fredcodecrafts.moodlens.utils.SessionManager

class AuthManager {

    private val supabaseUrl = "https://cglkbjwuvmakmamkcfww.supabase.co"
    // RENAMED to match the usage below
    private val supabaseAnonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNnbGtiand1dm1ha21hbWtjZnd3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjMyOTM1ODYsImV4cCI6MjA3ODg2OTU4Nn0.Yt2I8ELwfUT3sKD9PEMy5JgNGAbhnZ_gCXRN-m2a5Y8"

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true // <--- ADD THIS LINE! Crucial for Supabase.
            })
        }
    }

    suspend fun signInWithGoogle(idToken: String): Boolean {
        return try {
            // =================================================================
            // STEP 1: AUTHENTICATE (The "Gatekeeper")
            // Send the Google Token to Supabase.
            // If this succeeds, the user is GUARANTEED to be in the "Authentication" page.
            // =================================================================
            val authUrl = "$supabaseUrl/auth/v1/token?grant_type=id_token"

            val authResponse = client.post(authUrl) {
                header("apikey", supabaseAnonKey)
                contentType(ContentType.Application.Json)
                setBody(SupabaseAuthRequest(idToken = idToken))
            }

            // STOP if Auth failed
            if (authResponse.status.value !in 200..299) {
                val errorBody = authResponse.bodyAsText()
                Log.e("AuthManager", "STEP 1 FAILED: Could not create user in Auth. Response: $errorBody")
                return false
            }

            // Parse the session to get the real UUID and the Access Token
            val session = authResponse.body<SupabaseSession>()
            val realUuid = session.user.id
            val accessToken = session.accessToken

            Log.d("AuthManager", "STEP 1 SUCCESS: User is now in Supabase Auth. UUID: $realUuid")
            
            // CRITICAL SCRIPT ADAPTATION: Set Global Session Variables for Sync
            SessionManager.accessToken = accessToken
            SessionManager.currentUserId = realUuid

            true

        } catch (e: Exception) {
            Log.e("AuthManager", "CRITICAL ERROR: ${e.message}", e)
            false
        }
    }

    // --- REQUIRED DATA CLASSES ---

    @Serializable
    data class SupabaseAuthRequest(
        @SerialName("id_token") val idToken: String,
        val provider: String = "google"
    )

    @Serializable
    data class SupabaseSession(
        @SerialName("access_token") val accessToken: String,
        val user: SupabaseUser
    )

    @Serializable
    data class SupabaseUser(
        val id: String,
        val email: String? = null
    )

    @Serializable
    data class AppUser(
        @SerialName("user_id") val userId: String,
        @SerialName("google_id") val googleId: String
    )
}