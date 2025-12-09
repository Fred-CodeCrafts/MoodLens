package com.fredcodecrafts.moodlens.login


import android.app.Activity
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fredcodecrafts.moodlens.R
import com.fredcodecrafts.moodlens.components.AppButton
import com.fredcodecrafts.moodlens.components.AppCard
import com.fredcodecrafts.moodlens.database.AppDatabase
import com.fredcodecrafts.moodlens.database.entities.User
import com.fredcodecrafts.moodlens.ui.theme.gradientPrimary
import com.fredcodecrafts.moodlens.login.GoogleSignInHelper
import com.fredcodecrafts.moodlens.login.AuthManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

import android.util.Base64
import org.json.JSONObject

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSkipDemo: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Safely get Activity from context
    val activity = remember(context) {
        generateSequence(context) { (it as? android.content.ContextWrapper)?.baseContext }
            .filterIsInstance<Activity>()
            .firstOrNull()
            ?: throw IllegalStateException("Composable not hosted in an Activity")
    }

    val googleHelper = remember { GoogleSignInHelper(activity) }
    val supabaseAuth = remember { AuthManager() }
    val userDao = AppDatabase.getDatabase(activity).userDao()

    // Rotation animation for spinner
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientPrimary()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "MoodLens Logo",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "MoodLens",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(Modifier.height(24.dp))

            AppCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White.copy(alpha = 0.08f),
                borderColor = Color.White.copy(alpha = 0.15f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome back!",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Check in on your mood and reflect on your day. Sign in to continue.",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(24.dp))

                    AppButton(
                        text = if (isLoading) "Signing in..." else "Continue with Google",
                        onClick = {
                            if (isLoading) return@AppButton
                            isLoading = true

                            googleHelper.launch(
                                onSuccess = { idToken ->
                                    scope.launch {
                                        // FIX: Removed 'email' parameter. Only 'idToken' is needed now.
                                        val success = supabaseAuth.signInWithGoogle(idToken)

                                        if (success) {
                                            // Update/check local DB
                                            CoroutineScope(Dispatchers.IO).launch {
                                                // Extract the Google 'sub' (User ID) for local storage
                                                val googleSub = getGoogleUserId(idToken) ?: ""
                                                val sessionManager = com.fredcodecrafts.moodlens.utils.SessionManager(activity)
                                                
                                                // Persist Session!
                                                sessionManager.saveUserSession(
                                                    userId = com.fredcodecrafts.moodlens.utils.SessionManager.currentUserId ?: googleSub,
                                                    token = com.fredcodecrafts.moodlens.utils.SessionManager.accessToken ?: ""
                                                )

                                                var user = userDao.getUserByGoogleId(googleSub)
                                                if (user == null) {
                                                    user = User(
                                                        userId = UUID.randomUUID().toString(), // Local ID
                                                        googleId = googleSub
                                                    )
                                                    userDao.insert(user)
                                                }

                                                launch(Dispatchers.Main) {
                                                    isLoading = false
                                                    onLoginSuccess()
                                                }
                                            }
                                        } else {
                                            isLoading = false
                                        }
                                    }
                                },
                                onError = {
                                    isLoading = false
                                    Log.e("LoginScreen", "Google sign-in failed", it)
                                }
                            )

                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .rotate(rotation)
                                )
                                Spacer(Modifier.width(12.dp))
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_google),
                                    contentDescription = "Google Logo",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                            }
                        },
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Your mood data stays private and secure.",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            TextButton(onClick = onSkipDemo) {
                Text(
                    text = "Skip for demo",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


fun getGoogleUserId(idToken: String): String? {
    return try {
        val parts = idToken.split(".")
        if (parts.size < 2) return null
        val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
        val json = JSONObject(payload)
        json.getString("sub") // unique Google user ID
    } catch (e: Exception) {
        null
    }
}

fun getGoogleUserEmail(idToken: String): String? {
    return try {
        val parts = idToken.split(".")
        if (parts.size < 2) return null
        val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
        val json = org.json.JSONObject(payload)
        json.getString("email")
    } catch (e: Exception) {
        null
    }
}

