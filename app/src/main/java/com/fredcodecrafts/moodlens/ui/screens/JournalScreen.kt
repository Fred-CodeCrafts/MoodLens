package com.fredcodecrafts.moodlens.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.ui.res.painterResource // ✅ Jangan lupa import ini
import com.fredcodecrafts.moodlens.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fredcodecrafts.moodlens.components.*
import com.fredcodecrafts.moodlens.database.AppDatabase
import com.fredcodecrafts.moodlens.database.DummyData
import com.fredcodecrafts.moodlens.database.entities.JournalEntry
import com.fredcodecrafts.moodlens.database.entities.Note
import com.fredcodecrafts.moodlens.model.JournalStats
import com.fredcodecrafts.moodlens.navigation.Screen
import com.fredcodecrafts.moodlens.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

private fun formatTsToDate(ts: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = ts }
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
}
private fun formatTsToTime(ts: Long): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(ts))
}
private fun getEmojiForMood(mood: String): String = when (mood.lowercase()) {
    "happy" -> "😊"
    "sad" -> "😢"
    "anxious" -> "😟"
    "tired" -> "😴"
    "anger" -> "😠"
    "stress" -> "😫"
    else -> "😐"
}
private fun formatTimestampToDate(ts: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(java.util.Date(ts))
}
private fun formatTimestampToTime(ts: Long): String {
    val sdf = java.text.SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(java.util.Date(ts))
}

@Composable
fun JournalScreen(
    navController: NavController
) {
    val context = LocalContext.current
    // DAOs
    val db = remember { AppDatabase.getDatabase(context) }
    val journalDao = remember { db.journalDao() }
    val notesDao = remember { db.notesDao() }
    // UI state
    var entries by remember { mutableStateOf<List<JournalEntry>>(emptyList()) }
    var notesMap by remember { mutableStateOf<Map<String, List<Note>>>(emptyMap()) }
    var stats by remember { mutableStateOf(JournalStats(0, 0, 0)) }
    var isLoading by remember { mutableStateOf(true) }
    // Load from database once
    LaunchedEffect(Unit) {
        isLoading = true
        val loadedEntries = journalDao.getAllEntries()
        val loadedNotesMap = loadedEntries.associate { entry ->
            entry.entryId to notesDao.getNotesForEntry(entry.entryId)
        }
        val totalEntries = loadedEntries.size
        val totalNotes = loadedNotesMap.values.sumOf { it.size }
        val daysTracked = loadedEntries
            .map { formatTsToDate(it.timestamp) }
            .distinct()
            .size
        entries = loadedEntries
        notesMap = loadedNotesMap
        stats = JournalStats(totalEntries, totalNotes, daysTracked)
        isLoading = false
    }
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Stats section
            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                CardHeader {
                    androidx.compose.material3.Text("Your Journal Stats")
                }
                CardContent {
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatsColumn(value = stats.totalEntries.toString(), label = "Entries")
                        StatsColumn(value = stats.withNotes.toString(), label = "Notes")
                        StatsColumn(value = stats.daysTracked.toString(), label = "Days")
                    }
                }
            }
            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@Column
            }
            // Entries list
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(entries, key = { it.entryId }) { entry ->
                    AppCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Header: mood emoji and date/time
                        CardHeader {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                androidx.compose.material3.Text(
                                    text = getEmojiForMood(entry.mood),
                                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
                                )
                                androidx.compose.material3.Text(
                                    text = formatTsToTime(entry.timestamp),
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        // Content: location + notes
                        CardContent {
                            androidx.compose.material3.Text(
                                text = entry.location ?: "No location",
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            notesMap[entry.entryId]?.forEach { note ->
                                NoteCard(noteText = note.content)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// -- Supporting Composables --

@Composable
fun JournalHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterVertically).offset(x = (-16).dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.Black)
        }
        Text(
            text = "Mood Journal",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
fun StatsCard(stats: JournalStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = GradientPrimary)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(label = "Total Entries", value = stats.totalEntries.toString())
            StatItem(label = "With Notes", value = stats.withNotes.toString())
            StatItem(label = "Days Tracked", value = stats.daysTracked.toString())
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun EmptyJournalView(onTakeScanClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = LightPurple
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No journal entries yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start tracking your mood to see insights",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onTakeScanClicked,
            colors = ButtonDefaults.buttonColors(containerColor = MainPurple),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Take Your First Scan", color = Color.White)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JournalEntryCard(
    entry: JournalEntry,
    notes: List<Note>,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Mood icon bubble
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3E8FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getEmojiForMood(entry.mood),
                    fontSize = 24.sp
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                // Mood label & timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        shape = RoundedCornerShape(50.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = entry.mood.replaceFirstChar { it.uppercase() },
                            color = MainPurple,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Text(
                        text = formatTimestampToTime(entry.timestamp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                Spacer(Modifier.height(8.dp))
                // Date
                Text(
                    text = formatTimestampToDate(entry.timestamp),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                // Location row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.location),
                        contentDescription = "Location",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = entry.location ?: "No location",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                // Notes list
                if (notes.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    notes.forEach { note ->
                        NoteCard(noteText = note.content)
                        Spacer(Modifier.height(8.dp))
                    }
                }
                // AI-generated reflection
                entry.aiReflection?.let { reflectionText ->
                    Spacer(Modifier.height(8.dp))
                    ReflectionCard(reflectionText = reflectionText)
                }
            }
        }
    }
}


@Composable
fun NoteCard(noteText: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        // Gunakan shape dan warna yang lebih lembut
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightGray // Warna abu-abu keunguan yang sangat terang
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            // --- HEADER ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                // ✅ TAMBAHKAN ICON DI SINI
                Icon(
                    // Ganti R.drawable.ic_note_bubble dengan nama file XML Anda
                    painter = painterResource(id = R.drawable.bubblechat),
                    contentDescription = "Your Note",
                    tint = TextSecondary, // Warna abu-abu untuk header
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Your note",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- KONTEN CATATAN ---
            Text(
                text = noteText,
                style = MaterialTheme.typography.bodyMedium, // Style untuk konten utama
                color = TextPrimary
            )
        }
    }
}

@Composable
fun AddNoteButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = MainPurple),
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Note",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Note", color = Color.White)
        }
    }
}

@Composable
fun AddNoteCard(
    noteText: String,
    onNoteChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Add a Note",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = noteText,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter your note...") },
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancelClick) {
                    Text("Cancel", color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSaveClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MainPurple),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save", color = Color.White)
                }
            }
        }
    }
}
@Composable
fun ReflectionCard(
    reflectionText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = GradientCalm,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reflection",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = reflectionText,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFF0F2F5,
    name = "Journal Screen Full Page Preview" // Beri nama agar mudah dikenali
)
@Composable
fun JournalScreenFullPreview() {
    // 1. Siapkan semua data dummy yang dibutuhkan oleh layar
    val dummyEntries = DummyData.journalEntries
    val dummyNotesMap = DummyData.notes.groupBy { it.entryId }
    val dummyStats = JournalStats(
        totalEntries = dummyEntries.size,
        withNotes = dummyNotesMap.keys.size,
        daysTracked = dummyEntries.map { formatTimestampToDate(it.timestamp) }.distinct().size
    )

    // Preview tidak butuh NavController asli, cukup instance dummy
    val navController = rememberNavController()

    // 2. Bungkus dengan Tema aplikasi Anda
    MoodLensTheme {
        // 3. Buat ulang struktur UI JournalScreen di sini
        Scaffold(
            topBar = {
                // Gunakan TopAppBar atau JournalHeader yang sudah Anda buat
                JournalHeader(onBackClick = {})
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Item 1: Kartu Statistik
                item {
                    StatsCard(stats = dummyStats)
                }

                // Item 2: Daftar Entri Jurnal
                items(dummyEntries) { entry ->
                    JournalEntryCard(
                        entry = entry,
                        notes = dummyNotesMap[entry.entryId] ?: emptyList(),
                        onClick = {}, // Aksi di preview tidak perlu diisi
                        onLongClick = {}
                    )
                }
            }
        }
    }
}