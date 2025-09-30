package com.fredcodecrafts.moodlens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fredcodecrafts.moodlens.ui.theme.MainBackground
import com.fredcodecrafts.moodlens.ui.theme.TextPrimary
import com.fredcodecrafts.moodlens.ui.theme.TextSecondary

@Composable
fun TextAreaField(
    text: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    backgroundColor: Color = MainBackground,
    borderColor: Color = TextSecondary,
    textColor: Color = TextPrimary,
    minWidth: Int = 120,   // ✅ minimum width in dp
    maxWidth: Int = 400,   // ✅ maximum width in dp
    minHeight: Int = 80    // ✅ minimum height in dp, but no maxHeight
) {
    Box(
        modifier = modifier
            .widthIn(min = minWidth.dp, max = maxWidth.dp)
            .heightIn(min = minHeight.dp) // no max height → grows with text
            .background(backgroundColor, shape = MaterialTheme.shapes.medium)
            .border(1.dp, borderColor, shape = MaterialTheme.shapes.medium)
            .padding(12.dp)
    ) {
        if (text.isEmpty()) {
            Text(
                text = placeholder,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
