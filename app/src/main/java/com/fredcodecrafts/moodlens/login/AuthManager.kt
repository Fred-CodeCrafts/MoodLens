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

import com.fredcodecrafts.moodlens.utils.SupabaseConfig

class AuthManager {

    // Using shared config
    private val supabaseUrl = SupabaseConfig.SUPABASE_URL
    private val supabaseAnonKey = SupabaseConfig.ANON_KEY

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


            // =================================================================
            // STEP 2: UPSERT USER TO 'app_users' TABLE
            // Ensuring the user exists in our public table for RLS policies
            // =================================================================
            try {
                // Use the user's email if available, or just ID
                val email = session.user.email
                
                // We send a POST to the table endpoint with Prefer: resolution=merge-duplicates
                // This acts as an UPSERT (Insert or Update if PK matches)
                client.post("$supabaseUrl/rest/v1/${SupabaseConfig.SCHEMA}.app_users") {
                    header("Authorization", "Bearer $accessToken")
                    header("apikey", supabaseAnonKey)
                    header("Prefer", "resolution=merge-duplicates")
                    // Target the 'app_data' schema
                    header("Accept-Profile", SupabaseConfig.SCHEMA)
                    header("Content-Profile", SupabaseConfig.SCHEMA)
                    contentType(ContentType.Application.Json)
                    setBody(
                        AppUser(
                            userId = realUuid,
                            googleId = session.user.id // Using Supabase ID as google_id mapping for now, or actual google ID if passed
                        )
                    )
                }
                Log.d("AuthManager", "STEP 2 SUCCESS: User upserted to app_users.")

            } catch (e: Exception) {
                // Non-fatal, login can still proceed, but log it
                Log.e("AuthManager", "STEP 2 WARNING: Failed to upsert to app_users: ${e.message}")
            }

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