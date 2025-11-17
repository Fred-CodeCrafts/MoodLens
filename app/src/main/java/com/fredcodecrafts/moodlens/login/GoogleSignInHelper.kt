package com.fredcodecrafts.moodlens.login

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import java.security.MessageDigest
import java.util.*

class GoogleSignInHelper(private val activity: Activity) {

    private val oneTapClient = Identity.getSignInClient(activity)

    // Generate a SHA-256 nonce
    private fun createNonce(): String {
        val raw = UUID.randomUUID().toString()
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(raw.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    private val nonce = createNonce() // Generate a new nonce each time this class is instantiated

    private val signInRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId("983494992902-oi2f8lafrmt1htdb5gapc3aguclpsq91.apps.googleusercontent.com")
                .setFilterByAuthorizedAccounts(false)
                .setNonce(nonce) // Pass the nonce here
                .build()
        )
        .setAutoSelectEnabled(false)
        .build()

    fun launch(onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                try {
                    activity.startIntentSenderForResult(
                        result.pendingIntent.intentSender,
                        1001,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                } catch (e: Exception) {
                    onError(e)
                }
            }
            .addOnFailureListener { e -> onError(e) }
    }

    fun handleResult(data: Intent?): String? {
        return try {
            val credential = oneTapClient.getSignInCredentialFromIntent(data)
            credential.googleIdToken
        } catch (e: ApiException) {
            Log.e("GoogleSignInHelper", "Failed to retrieve ID Token", e)
            null
        }
    }
}
