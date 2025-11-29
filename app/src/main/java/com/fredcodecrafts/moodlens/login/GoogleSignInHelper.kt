package com.fredcodecrafts.moodlens.login

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.MessageDigest
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GoogleSignInHelper(
    private val activity: Activity
) {

    private val credentialManager = CredentialManager.create(activity)

    private fun createNonce(): String {
        val raw = UUID.randomUUID().toString()
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(raw.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    fun launch(
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {

        val nonce = createNonce()

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId("500733986799-14ab1pogfgkp20d9vr95n43i5qmb3vkh.apps.googleusercontent.com")
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .setNonce(nonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // FIX: run suspend function in coroutine
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result: GetCredentialResponse =
                    credentialManager.getCredential(
                        context = activity,
                        request = request
                    )

                val credential = result.credential
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleCredential.idToken

                onSuccess(idToken)

            } catch (e: Exception) {
                Log.e("GoogleSignInHelper", "Google Sign-In error", e)
                onError(e)
            }
        }
    }
}
