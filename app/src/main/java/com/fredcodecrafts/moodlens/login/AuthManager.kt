package com.fredcodecrafts.moodlens.login

import android.util.Log
import com.fredcodecrafts.moodlens.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthManager {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun signInWithGoogle(idToken: String): Boolean {
        return try {
            // =================================================================
            // STEP 1: AUTHENTICATE with Firebase
            // =================================================================
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            
            val user = authResult.user
            if (user == null) {
                Log.e("AuthManager", "STEP 1 FAILED: Firebase sign-in returned null user.")
                return false
            }

            // CRITICAL SCRIPT ADAPTATION: Set Global Session Variables
            SessionManager.currentUserId = user.uid
            // Firebase ID tokens are dynamic, but we can store the UID.
            // For Sync with a backend, we'd usually get an ID token here:
            // val token = user.getIdToken(true).await().token
            // SessionManager.accessToken = token 
            // For now, since we are disabling remote sync, we just need the User ID.
            SessionManager.accessToken = "firebase_token_placeholder"

            Log.d("AuthManager", "STEP 1 SUCCESS: User signed in to Firebase. UID: ${user.uid}")
            
            // =================================================================
            // STEP 2: USER DB RECORD
            // Firebase handles user creation automatically. 
            // If we needed a separate 'users' table in a custom DB, we would do it here.
            // For now, we rely on Firebase Auth's user record.
            // =================================================================

            true

        } catch (e: Exception) {
            Log.e("AuthManager", "CRITICAL ERROR: ${e.message}", e)
            false
        }
    }
}