package com.fredcodecrafts.moodlens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import com.fredcodecrafts.moodlens.ui.theme.AppTypography

// --- Base Card ---
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = Color.Transparent,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .background(backgroundColor, shape = RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, shape = RoundedCornerShape(12.dp))
            .padding(0.dp) // padding handled by children
    ) {
        content()
    }
}

// --- Card Header ---
@Composable
fun CardHeader(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
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
        style = AppTypography.headlineMedium,
        modifier = modifier
    )
}

// --- Card Description ---
@Composable
fun CardDescription(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
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
fun CardContent(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        content = content
    )
}

// --- Card Footer ---
@Composable
fun CardFooter(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
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
            style = AppTypography.displayLarge
        )
        Text(
            text = label,
            color = textColor.copy(alpha = 0.8f),
            style = AppTypography.bodyMedium
        )
    }
}
