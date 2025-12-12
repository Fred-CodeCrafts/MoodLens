package com.fredcodecrafts.moodlens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VirtualKeyboard(
    modifier: Modifier = Modifier,
    onKeyPress: (String) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFD1D5DB)) // Keyboard gray background
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1
        KeyboardRow(keys = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"))
        // Row 2
        KeyboardRow(keys = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"), padding = 16.dp)
        // Row 3
        KeyboardRow(keys = listOf("Z", "X", "C", "V", "B", "N", "M"), padding = 32.dp)
        // Space Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White)
                    .shadow(1.dp, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun KeyboardRow(keys: List<String>, padding: androidx.compose.ui.unit.Dp = 0.dp) {
    Row(
        modifier = Modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        keys.forEach { key ->
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(42.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White)
                    .shadow(1.dp, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = key,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
        }
    }
}
