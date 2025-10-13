package com.fredcodecrafts.moodlens.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// --- Core Colors (converted from HSL in Tailwind theme) ---
val MainPurple = Color(0xFF7B3AED)    // Brand purple (hsl(258, 89%, 66%))
val MainBackground = Color(0xFFF9F8FF) // Corrected background (hsl(245, 100%, 99%))

// Extended Palette
val DarkPurple = Color(0xFF5A35A2)    // Supporting purple shade
val LightPurple = Color(0xFFD9C8FA)
val TextPrimary = Color(0xFF1C1C28)
val TextSecondary = Color(0xFF6F6F80)

// Complementary Colors (from your Tailwind HSLs)
val SecondaryBlue = Color(0xFFB6E0FE)     // hsl(218, 100%, 88%)
val AccentCoral = Color(0xFFFCA38A)       // hsl(14, 100%, 82%)
val SuccessGreen = Color(0xFF86EFAC)      // hsl(142, 76%, 73%)
val WarningOrange = Color(0xFFFCD34D)     // hsl(32, 100%, 78%)
val MutedNeutral = Color(0xFFF5F5FA)      // hsl(240, 20%, 96%)
val BorderNeutral = Color(0xFFE6E6F2)     // hsl(240, 20%, 92%)

// --- Notification Colors ---
val NotificationSuccess = SuccessGreen  // Green
val NotificationError = AccentCoral     // Red
val NotificationWarning = WarningOrange  // Amber
val NotificationInfo = SecondaryBlue     // Blue
val NotificationText = TextSecondary

// --- Gradient Brushes (converted from your CSS theme) ---
val GradientPrimary = Brush.linearGradient(
    colors = listOf(
        MainPurple,
        Color(0xFF9D66F5) // lighter variant of primary
    ),
    start = Offset(0f, 0f),
    end = Offset(1f, 1f)
)

val GradientCalm = Brush.linearGradient(
    colors = listOf(
        SecondaryBlue,
        Color(0xFFCFE8FF)
    ),
    start = Offset(0f, 0f),
    end = Offset(1f, 1f)
)

val GradientWarm = Brush.linearGradient(
    colors = listOf(
        AccentCoral,
        Color(0xFFFFC0A8)
    ),
    start = Offset(0f, 0f),
    end = Offset(1f, 1f)
)

val GradientBackground = Brush.linearGradient(
    colors = listOf(
        MainBackground,
        Color(0xFFFBFBFE)
    ),
    start = Offset(0f, 0f),
    end = Offset(0f, 1f)
)

val CardBackground = Color(0x99FFFFFF) // White @ 60% opacity
val CardShadow = Color(0x33000000)     // Soft shadow (20% black)