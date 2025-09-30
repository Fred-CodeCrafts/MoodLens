package com.fredcodecrafts.moodlens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fredcodecrafts.moodlens.ui.theme.MoodLensTheme
import com.fredcodecrafts.moodlens.components.InputField
import com.fredcodecrafts.moodlens.components.TextAreaField
import com.fredcodecrafts.moodlens.ui.theme.MainBackground
import androidx.compose.ui.graphics.Color
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoodLensTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ShowcaseScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ShowcaseScreen(modifier: Modifier = Modifier) {
    var name by remember { mutableStateOf("") }
    var journalEntry by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Reflection Companion",
            style = MaterialTheme.typography.headlineMedium
        )

        // Single-line input (for name or short answers)
        InputField(
            value = name,
            onValueChange = { name = it },
            label = "Name",
            placeholder = "Enter your name"
        )

        // Default, auto-fitting text
        TextAreaField(
            text = "A short note.",
            placeholder = "No text yet"
        )

// Will stretch but stay within min/max width
        TextAreaField(
            text = "This is a much longer reflection entry that will expand the height " +
                    "naturally as needed. The width will be constrained between 120dp and 400dp."
        )



        // Example Button using entered text
        Button(
            onClick = {
                // TODO: Save or submit entry
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Reflection")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ShowcasePreview() {
    MoodLensTheme {
        ShowcaseScreen()
    }
}
