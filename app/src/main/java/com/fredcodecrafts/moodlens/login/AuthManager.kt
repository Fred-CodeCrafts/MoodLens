package com.fredcodecrafts.moodlens.login

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AuthManager {

    private val supabaseUrl = "https://cglkbjwuvmakmamkcfww.supabase.co"
    // WARNING: Never keep keys in code for production. Use BuildConfig.
    private val supabaseAnonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNnbGtiand1dm1ha21hbWtjZnd3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjMyOTM1ODYsImV4cCI6MjA3ODg2OTU4Nn0.Yt2I8ELwfUT3sKD9PEMy5JgNGAbhnZ_gCXRN-m2a5Y8"

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun signInWithGoogle(idToken: String): Boolean {
        return try {
            // STEP 1: Exchange Google Token for Supabase Session
            // This creates the user in auth.users automatically!
            val authUrl = "$supabaseUrl/auth/v1/token?grant_type=id_token"

            val authResponse = client.post(authUrl) {
                header("apikey", supabaseAnonKey)
                contentType(ContentType.Application.Json)
                setBody(SupabaseAuthRequest(idToken = idToken))
            }

            if (authResponse.status.value !in 200..299) {
                Log.e("AuthManager", "Supabase Auth Failed: ${authResponse.status}")
                return false
            }

            val session = authResponse.body<SupabaseSession>()
            val userId = session.user.id
            val accessToken = session.accessToken

            // STEP 2: Update/Insert into app_data.app_users
            // We must use the accessToken (not anon key) so RLS works
            val tableUrl = "$supabaseUrl/rest/v1/app_users"

            // Upsert (Insert or Update) based on user_id
            // We use the UUID from Supabase, NOT the email
            val dbResponse = client.post(tableUrl) {
                url {
                    parameters.append("on_conflict", "user_id")
                    parameters.append("resolution", "merge-duplicates")
                }
                header("apikey", supabaseAnonKey)
                header("Authorization", "Bearer $accessToken") // Authenticated Request
                header("Prefer", "resolution=merge-duplicates") // Standard Upsert header
                contentType(ContentType.Application.Json)
                setBody(AppUser(userId = userId, googleId = idToken))
            }

            if (dbResponse.status.value in 200..299) {
                Log.d("AuthManager", "User synced successfully")
                true
            } else {
                Log.e("AuthManager", "DB Sync Failed: ${dbResponse.status}")
                false
            }

        } catch (e: Exception) {
            Log.e("AuthManager", "Error signing in", e)
            false
        }
    }

    // --- Data Classes ---

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
        @SerialName("user_id") val userId: String, // This must be UUID
        @SerialName("google_id") val googleId: String
    )
}