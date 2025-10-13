package com.fredcodecrafts.moodlens.login

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fredcodecrafts.moodlens.components.AppButton
import com.fredcodecrafts.moodlens.components.AppCard
import com.fredcodecrafts.moodlens.components.ButtonVariant
import com.fredcodecrafts.moodlens.ui.theme.gradientPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import com.fredcodecrafts.moodlens.R


@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSkipDemo: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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

            // Main Card
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

                    // Dummy Auth Button
                    AppButton(
                        text = if (isLoading) "Signing in..." else "Continue with Google",
                        onClick = {
                            scope.launch {
                                isLoading = true
                                delay(1500)
                                isLoading = false
                                onLoginSuccess()
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google),
                                contentDescription = "Google Logo",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(12.dp)) // Increased spacing here
                        },
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        shape = RoundedCornerShape(4.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    // Later, you can easily replace the dummy auth with Google
                    // Example:
                    // AppButton(
                    //     text = "Sign in with Google",
                    //     leadingIcon = { Image(painterResource(R.drawable.ic_google), null, Modifier.size(20.dp)) },
                    //     onClick = { /* TODO: handle Google sign-in */ },
                    //     modifier = Modifier.fillMaxWidth(),
                    //     variant = ButtonVariant.Outline
                    // )

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
