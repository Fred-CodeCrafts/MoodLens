package com.fredcodecrafts.moodlens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart // Contoh ikon grafik naik
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Komponen Card yang menampilkan judul, ikon, persentase, Progress Bar linear,
 * dan deskripsi di bagian bawah. Mirip dengan desain Wellness Score.
 * @param progress Nilai kemajuan antara 0.0f hingga 1.0f (misalnya, 0.75f = 75%).
 * @param title Judul kartu, default "Wellness Score".
 * @param description Teks deskripsi di bawah progress bar.
 * @param progressColor Warna utama progress bar.
 * @param trackColor Warna track progress bar.
 * @param percentLabelColor Warna label persentase (kotak hijau).
 */
@Composable
fun WellnessProgressCard(
    modifier: Modifier = Modifier,
    progress: Float,
    title: String = "Wellness Score",
    description: String = "Based on positive mood entries this period",
    // Warna kustom agar lebih mirip gambar
    progressColor: Color = Color(0xFF673AB7), // Ungu
    trackColor: Color = Color(0xFFE0E0E0), // Abu-abu terang
    percentLabelColor: Color = Color(0xFF4CAF50) // Hijau
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = CardDefaults.shape // Bentuk default Card
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Bagian Atas: Ikon, Judul, dan Persentase
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween // Agar persentase di kanan
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Ikon
                    Icon(
                        imageVector = Icons.Default.ShowChart, // Contoh ikon grafik naik
                        contentDescription = null, // Deskripsi aksesibilitas
                        tint = MaterialTheme.colorScheme.primary, // Warna ikon bisa disesuaikan
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Judul
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Kotak Persentase (misalnya 40%)
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier
                        .background(percentLabelColor, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp)) // Jarak antara judul dan progress bar

            // Linear Progress Bar
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp), // Ketebalan progress bar
                color = progressColor,
                trackColor = trackColor
            )

            Spacer(modifier = Modifier.height(8.dp)) // Jarak antara progress bar dan deskripsi

            // Teks Deskripsi
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}