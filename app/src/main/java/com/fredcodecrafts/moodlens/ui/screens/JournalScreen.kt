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

private fun formatTimestampToDate(timestamp: Long): String {
    val entryCalendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    val todayCalendar = Calendar.getInstance()
    return when {
        entryCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                entryCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR) -> "Today"
        else -> {
            todayCalendar.add(Calendar.DAY_OF_YEAR, -1)
            if (entryCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                entryCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)
            ) "Yesterday"
            else SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(timestamp))
        }
    }
}

private fun formatTimestampToTime(timestamp: Long): String {
    val format = SimpleDateFormat("h:mm a", Locale.getDefault())
    return format.format(Date(timestamp))
}

private fun getEmojiForMood(mood: String): String = when (mood.lowercase()) {
    "anxious" -> "😟"
    "happy" -> "😊"
    "sad" -> "😢"
    "anger" -> "😠"
    "joy" -> "😄"
    "stress" -> "😫"
    "tired" -> "😴"
    else -> "😐"
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JournalScreen(
    navController: NavController,
    // Context dan userId tidak lagi diperlukan di sini
) {
    var entryToDelete by remember { mutableStateOf<JournalEntry?>(null) }

    // ✅ FIX: Inisialisasi semua state langsung dari DummyData.
    var entries by remember { mutableStateOf(DummyData.journalEntries) }
    var notesMap by remember { mutableStateOf(DummyData.notes.groupBy { it.entryId }) }
    var stats by remember {
        mutableStateOf(
            JournalStats(
                totalEntries = DummyData.journalEntries.size,
                withNotes = DummyData.notes.groupBy { it.entryId }.keys.size,
                daysTracked = DummyData.journalEntries.map { formatTimestampToDate(it.timestamp) }.distinct().size
            )
        )
    }
    var selectedEntryId by remember { mutableStateOf<String?>(null) }
    var isAddingNote by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    // ✅ FIX: isLoading tidak lagi diperlukan, selalu false.
    val isLoading by remember { mutableStateOf(false) }
    val latestEntry = entries.firstOrNull()

    // ✅ FIX: LaunchedEffect yang mengakses database DIHAPUS.

    // ✅ FIX: Fungsi onSaveNote sekarang HANYA mengubah state lokal.
    val onSaveNote: () -> Unit = {
        if (noteText.isNotBlank() && selectedEntryId != null) {
            val newNote = Note("note_${System.currentTimeMillis()}", selectedEntryId!!, noteText)
            val updatedNotes = notesMap[selectedEntryId]?.toMutableList() ?: mutableListOf()
            updatedNotes.add(newNote)
            notesMap = notesMap.toMutableMap().apply { put(selectedEntryId!!, updatedNotes) }
            stats = stats.copy(withNotes = notesMap.values.count { it.isNotEmpty() })
            isAddingNote = false
            noteText = ""
        }
    }

    // ✅ FIX: Fungsi onDeleteEntry sekarang HANYA mengubah state lokal.
    val onDeleteEntry: (JournalEntry) -> Unit = { entry ->
        entries = entries.filter { it.entryId != entry.entryId }
        notesMap = notesMap.toMutableMap().apply { remove(entry.entryId) }
        stats = stats.copy(totalEntries = entries.size)
    }

    // Dialog konfirmasi hapus
    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete Entry") },
            text = { Text("Are you sure you want to delete this journal entry?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteEntry(entryToDelete!!)
                    entryToDelete = null
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        JournalHeader(onBackClick = { navController.navigateUp() })

        when {
            isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MainPurple)
            }
            entries.isEmpty() -> EmptyJournalView {
                // Untuk testing, kita bisa buat tombol ini memuat ulang data dummy
                entries = DummyData.journalEntries
                notesMap = DummyData.notes.groupBy { it.entryId }
            }
            else -> {
                // ✅ FIX: Semua konten sekarang berada di dalam LazyColumn
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        StatsCard(stats = stats)
                    }

                    if (latestEntry != null && notesMap[latestEntry.entryId].isNullOrEmpty() && !isAddingNote) {
                        item {
                            AddNoteButton(onClick = {
                                selectedEntryId = latestEntry.entryId
                                isAddingNote = true
                            })
                        }
                    }

                    item {
                        AnimatedVisibility(visible = isAddingNote) {
                            AddNoteCard(
                                noteText = noteText,
                                onNoteChange = { noteText = it },
                                onSaveClick = onSaveNote,
                                onCancelClick = {
                                    isAddingNote = false
                                    noteText = ""
                                }
                            )
                        }
                    }

                    items(items = entries, key = { it.entryId }) { entry ->
                        JournalEntryCard(
                            entry = entry,
                            // ✅ 3. KIRIM DATA NOTES DARI SINI
                            notes = notesMap[entry.entryId] ?: emptyList(),
                            onClick = {
                                navController.navigate(Screen.Reflection.createRoute(entry.entryId, entry.mood))
                            },
                            onLongClick = {
                                entryToDelete = entry
                            }
                        )
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
                .background(brush = JournalGradient)
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
            //.padding(horizontal = 16.dp, vertical = 8.dp) // Padding dikontrol oleh LazyColumn
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.White), // ✅ FIX: Beri warna solid
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFF3E8FF)), // Warna lebih soft
                contentAlignment = Alignment.Center
            ) {
                Text(text = getEmojiForMood(entry.mood), fontSize = 24.sp)
            }
            // ✅ FIX: Weight diubah menjadi 1f agar mengisi sisa ruang
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        shape = RoundedCornerShape(50.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)), // Warna disamakan
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = entry.mood.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelLarge, // Style lebih pas
                            color = MainPurple,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Text(
                        text = formatTimestampToTime(entry.timestamp),
                        style = MaterialTheme.typography.bodyMedium, color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatTimestampToDate(entry.timestamp),
                    style = MaterialTheme.typography.titleMedium, // Ukuran lebih besar agar jelas
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                // ✅ FIX: Ganti emoji dengan Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.location), // Call the XML file
                        contentDescription = "Location",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = entry.location ?: "No location", // Handle jika lokasi null
                        style = MaterialTheme.typography.bodyMedium, color = TextSecondary
                    )
                }

                if (notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    notes.forEach { note ->
                        NoteCard(noteText = note.content)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                if (entry.aiReflection != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ReflectionCard(reflectionText = entry.aiReflection)
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