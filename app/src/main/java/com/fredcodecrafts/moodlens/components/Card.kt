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
import androidx.compose.ui.unit.dp
import com.fredcodecrafts.moodlens.ui.theme.AppTypography

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
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .background(backgroundColor, shape = CardShape)
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

// --- Stats Column ---
@Composable
fun StatsColumn(
    value: String,
    label: String,
    textColor: Color = Color.White
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = textColor,
            style = AppTypography.displaySmall
        )
        Text(
            text = label,
            color = textColor.copy(alpha = 0.8f),
            style = AppTypography.bodySmall
        )
    }
}
