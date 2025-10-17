package com.fredcodecrafts.moodlens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fredcodecrafts.moodlens.ui.theme.AppTypography
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import com.fredcodecrafts.moodlens.database.entities.Message
import com.fredcodecrafts.moodlens.ui.theme.GradientPrimary
import com.fredcodecrafts.moodlens.ui.theme.SecondaryBlue


/**
 * A modular and reusable card component with optional header, content, and footer sections.
 * This serves as the foundation for UI blocks across the app.
 */

private val CardShape = RoundedCornerShape(12.dp)

// --- Base Card ---
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = Color.Transparent,
    backgroundBrush: Brush? = null, // ✅ Parameter baru untuk gradient
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .then(
                // Terapkan brush jika ada, jika tidak pakai backgroundColor
                if (backgroundBrush != null) {
                    Modifier.background(backgroundBrush, shape = CardShape)
                } else {
                    Modifier.background(backgroundColor, shape = CardShape)
                }
            )
            .border(1.dp, borderColor, shape = CardShape)
            .padding(contentPadding)
    ) {
        content()
    }
}

// --- Card Header ---
@Composable
fun CardHeader(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        content = content
    )
}

// --- Card Title ---
@Composable
fun CardTitle(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        text = text,
        color = textColor,
        style = AppTypography.titleMedium,
        modifier = modifier
    )
}

// --- Card Description ---
@Composable
fun CardDescription(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Text(
        text = text,
        color = textColor,
        style = AppTypography.bodyMedium,
        modifier = modifier
    )
}

// --- Card Content ---
@Composable
fun CardContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        content = content
    )
}

// --- Card Footer ---
@Composable
fun CardFooter(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        content = content
    )
}

@Composable
fun StatsColumn(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    textColor: Color = Color.White
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = textColor,
            style = MaterialTheme.typography.headlineMedium, // ✅ Gunakan gaya yang ada
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp

        )
        Text(
            text = label,
            color = textColor.copy(alpha = 0.8f), // Efek transparan ini bagus!
            style = MaterialTheme.typography.bodyMedium // ✅ Gunakan gaya yang ada
        )
    }
}

@Composable
fun ChatMessageBubble(message: Message) {
    // Define the gradient for the AI bubble
    val aiGradient = Brush.verticalGradient(
        colors = listOf(SecondaryBlue, Color(0xFFD6F3FF))
    )

    if (message.isUser) {
        // Call the reusable bubble for the user
        MessageBubble(
            message = message,
            brush = GradientPrimary, // Your purple gradient
            shape = UserBubbleShape(),
            arrangement = Arrangement.End,
            contentColor = Color.White // White text on a dark background
        )
    } else {
        // Call the reusable bubble for the AI
        MessageBubble(
            message = message,
            brush = aiGradient,
            shape = AiBubbleShape(),
            arrangement = Arrangement.Start,
            contentColor = MaterialTheme.colorScheme.onSurface // Dark text on a light background
        )
    }
}

// Di file: com/fredcodecrafts/moodlens/components/Card.kt

@Composable
private fun MessageBubble(
    message: Message,
    brush: Brush,
    shape: Shape,
    arrangement: Arrangement.Horizontal,
    contentColor: Color
) {
    Row(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = arrangement
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(brush, shape)
                .clip(shape),
            color = Color.Transparent
        ) {
            MessageContent(message, contentColor = contentColor)
        }
    }
}

@Composable
private fun MessageContent(message: Message, contentColor: Color) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = message.text,
            color = contentColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatTimestamp(message.timestamp),
            color = contentColor.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
private fun AiBubbleShape(): Shape = RoundedCornerShape(
    topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp
)

@Composable
private fun UserBubbleShape(): Shape = RoundedCornerShape(
    topStart = 20.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp
)

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
