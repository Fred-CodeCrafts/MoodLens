package com.fredcodecrafts.moodlens.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// *** MoodCard yang sudah ada (tidak perlu diubah) ***
@Composable
fun MoodCard(
    title: String,
    desc: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = CardDefaults.shape
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = desc, fontSize = 14.sp)
        }
    }
}

// *** FUNGSI BARU UNTUK TAMPILAN SHAFT STATISTIK ***
@Composable
fun MoodStatsShaft(
    modifier: Modifier = Modifier,
    // Kita gunakan warna ungu yang mirip dengan gambar
    backgroundColor: Color = Color(0xFF673AB7)
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        // Atur warna latar belakang dan elevasi
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        // Row adalah "shaft" yang menampung 3 kolom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // Atur padding di dalam shaft agar tidak terlalu mepet
                .padding(vertical = 24.dp, horizontal = 16.dp),
            // Atur agar item-item terdistribusi merata
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kita buat 3 Column untuk setiap statistik

            // Kolom 1: Total Entries
            StatsColumn(value = "13", label = "Total Entries")

            // Kolom 2: With Notes
            StatsColumn(value = "0", label = "With Notes")

            // Kolom 3: Days Tracked
            StatsColumn(value = "4", label = "Days Tracked")
        }
    }
}

// Komponen bantu untuk mempermudah penulisan 3 kolom yang identik
@Composable
private fun StatsColumn(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Nilai Angka (Lebih besar dan tebal)
        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        // Label Deskripsi (Lebih kecil dan agak transparan)
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}