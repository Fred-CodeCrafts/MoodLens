package com.fredcodecrafts.moodlens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fredcodecrafts.moodlens.ui.theme.MoodLensTheme

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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Hello Android!",
            style = MaterialTheme.typography.headlineMedium
        )

        // Elevated Material 3 Button
        Button(
            onClick = { /*TODO: Action*/ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Primary Button")
        }

        // Outlined Button
        OutlinedButton(
            onClick = { /*TODO: Action*/ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Outlined Button")
        }

        // Text Button
        TextButton(
            onClick = { /*TODO: Action*/ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Text Button")
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
