package com.fredcodecrafts.moodlens.login

import android.util.Log
import android.util.Base64
import org.json.JSONObject
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
import com.fredcodecrafts.moodlens.utils.SupabaseClient


class AuthManager {
    
    // Note: URL and Key are now centralized in SupabaseClient, but we keep them here for now if needed locally or refactor fully.
    private val supabaseUrl = "https://cglkbjwuvmakmamkcfww.supabase.co"
    private val supabaseAnonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNnbGtiand1dm1ha21hbWtjZnd3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjMyOTM1ODYsImV4cCI6MjA3ODg2OTU4Nn0.Yt2I8ELwfUT3sKD9PEMy5JgNGAbhnZ_gCXRN-m2a5Y8"

    private val client = SupabaseClient.client

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

            // =================================================================
            // STEP 2: SYNC USER TO DATABASE
            // Upsert the user into the 'public.users' table to ensure they exist in our schema.
            // =================================================================
            
            // Extract Google ID (sub) from the ID Token
            val googleId = try {
                val parts = idToken.split(".")
                if (parts.size >= 2) {
                    val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
                    val json = JSONObject(payload)
                    json.getString("sub")
                } else {
                    ""
                }
            } catch (e: Exception) {
                Log.e("AuthManager", "Failed to parse Google ID from token", e)
                ""
            }

            if (googleId.isNotEmpty()) {
                val usersUrl = "$supabaseUrl/rest/v1/users"
                val appUser = AppUser(userId = realUuid, googleId = googleId)

                val userResponse = client.post(usersUrl) {
                    header("Authorization", "Bearer $accessToken")
                    header("apikey", supabaseAnonKey)
                    header("Prefer", "resolution=merge-duplicates")
                    contentType(ContentType.Application.Json)
                    setBody(appUser)
                }

                if (userResponse.status.value in 200..299) {
                    Log.d("AuthManager", "STEP 2 SUCCESS: User synced to public.users table.")
                } else {
                    Log.e("AuthManager", "STEP 2 FAILED: Could not sync user. Response: ${userResponse.bodyAsText()}")
                    // parsing failed but auth succeeded, checking if we should return false?
                    // Usually we want to ensure data consistency, but if auth succeeded, user might be able to retry.
                    // For now, let's return true but log error, or maybe fail?
                    // The prompt asked "placed to the online database". If that fails, maybe login should be considered "incomplete".
                    // But blocking login might be too harsh if it's just a sync issue.
                    // However, if the app relies on the user being in the DB, it will crash later.
                    // Let's assume critical failure for now to be safe.
                    return false
                }
            } else {
                Log.e("AuthManager", "STEP 2 FAILED: Could not extract Google ID.")
                return false
            }

            // SET SESSION TOKEN GLOBALLY
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