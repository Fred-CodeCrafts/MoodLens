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
import com.fredcodecrafts.moodlens.ui.theme.GradientPrimary
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
import com.fredcodecrafts.moodlens.navigation.Screen
import com.fredcodecrafts.moodlens.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.graphics.graphicsLayer


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
    context: Context,
    userId: String = "default_user"
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
    val db = AppDatabase.getDatabase(context)
    val journalDao = db.journalDao()
    val notesDao = db.notesDao()
    var selectedEntryId by remember { mutableStateOf<String?>(null) }
    var isAddingNote by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    // ✅ FIX: isLoading tidak lagi diperlukan, selalu false.
    val isLoading by remember { mutableStateOf(false) }
    val latestEntry = entries.firstOrNull()
    val scope = rememberCoroutineScope()


    // ✅ FIX: LaunchedEffect yang mengakses database DIHAPUS.

    // ✅ FIX: Fungsi onSaveNote sekarang HANYA mengubah state lokal.
    val onSaveNote: () -> Unit = {
        if (noteText.isNotBlank() && selectedEntryId != null) {
            val newNote = Note(
                noteId = "note_${System.currentTimeMillis()}",
                entryId = selectedEntryId!!,
                content = noteText
            )

            // 1. Update UI state immediately for responsiveness
            val updatedNotes = notesMap[selectedEntryId]?.toMutableList() ?: mutableListOf()
            updatedNotes.add(newNote)
            notesMap = notesMap.toMutableMap().apply { put(selectedEntryId!!, updatedNotes) }
            stats = stats.copy(withNotes = notesMap.values.count { it.isNotEmpty() })

            // 2. ✅ Save the new note to the database in the background
            scope.launch(Dispatchers.IO) {
                notesDao.insert(newNote)
                // Optional: You could reload all data here, but updating
                // the local state might be enough if LaunchedEffect reloads
                // correctly when navigating back.
                // withContext(Dispatchers.Main) { loadData() } // Example if you have loadData()
            }

            // 3. Reset input fields
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
        JournalHeader( navController = navController )

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
                    contentPadding = PaddingValues(start = 16.dp,end = 16.dp, bottom = 16.dp),
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
fun JournalHeader(
    navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Back button navigates to Home
        IconButton(onClick = {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Journal.route) { inclusive = true }
            }
        }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF1A1A1A)
            )
        }

        // Centered title
        Box(
            modifier = Modifier
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Mood Journal",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF1A1A1A)
            )
        }
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
fun StatsCard(stats: JournalStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
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
            .padding(vertical = 8.dp)
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
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(LightPurple, MainPurple),
                            start = Offset(0f, 0f),
                            end = Offset(100f, 100f)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = getEmojiForMood(entry.mood), fontSize = 24.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
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
                            style = MaterialTheme.typography.labelLarge,
                            color = MainPurple,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Text(
                        text = formatTimestampToTime(entry.timestamp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTimestampToDate(entry.timestamp),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.location),
                            contentDescription = "Location",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = entry.location ?: "No location",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
//                            fontSize = 18.sp
                        )
                    }
                }

                if (notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    notes.forEach { note ->
                        NoteCard(noteText = note.content)
                    }
                }

                if (entry.aiReflection != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ReflectionCard(reflectionText = entry.aiReflection)
                    }
                }
            }
        }
    }
}

@Composable
fun NoteCard(noteText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x449E86A1) // 258,15%,55% in HSL-ish approximation
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.bubblechat),
                    contentDescription = "Your Note",
                    tint = TextSecondary,
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
            Text(
                text = noteText,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        }
    }
}

@Composable
fun AddNoteButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White), // ✅ White background
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center // ikon + teks di tengah
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Note to Your Lastest Mood",
                tint = Color.Black // ✅ Black icon
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add Note to Your Lastest Mood",
                color = Color.Black // ✅ Black text
            )
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                OutlinedButton(
                    onClick = onCancelClick,
                    border = BorderStroke(1.dp, Color.Gray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSaveClick,
                    enabled = noteText.isNotBlank(),
                    // 1. Buat warna asli Button menjadi transparan
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        // Atur warna teks agar kontras dengan gradient (misal: putih)
                        contentColor = Color.White,
                        // Atur warna saat disabled jika perlu (opsional)
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = Color.White.copy(alpha = 0.5f) // Teks jadi redup
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        // 2. Gambar gradient di background modifier
                        .background(
                            brush = GradientPrimary,
                            shape = RoundedCornerShape(8.dp) // Pastikan shape sama
                        )
                        // 3. (Opsional) Tambahkan efek visual saat disabled
                        .then(
                            if (!noteText.isNotBlank()) Modifier.graphicsLayer(alpha = 0.5f) else Modifier
                        )

                ) {
                    Text(
                        "Save",
                        // Warna teks sekarang diatur di ButtonDefaults.buttonColors
                        // color = if (noteText.isNotBlank()) Color.White else Color.White.copy(alpha = 0.5f)
                    )
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
@Preview(showBackground = true)
@Composable
fun JournalEntryCardPreview() {
    val dummyEntry = DummyData.journalEntries.first()
    val dummyNotes = DummyData.notes.filter { it.entryId == dummyEntry.entryId }
    MoodLensTheme {
        JournalEntryCard(
            entry = dummyEntry,
            notes = dummyNotes,
            onClick = {},
            onLongClick = {}
        )
    }
}
@Preview(
    showBackground = true,
    backgroundColor = 0xFFF8F9FA, // Warna background sesuai Column utama
    name = "Journal Screen Full Preview (Dummy Data)"
)
@Composable
fun JournalScreenFullPreview() {
    // 1. Siapkan semua state menggunakan DummyData
    val dummyEntries = DummyData.journalEntries
    val dummyNotesMap = DummyData.notes.groupBy { it.entryId }
    val dummyStats = JournalStats(
        totalEntries = dummyEntries.size,
        withNotes = dummyNotesMap.filterValues { it.isNotEmpty() }.size,
        daysTracked = dummyEntries.map { formatTimestampToDate(it.timestamp) }.distinct().size
    )
    val navController = rememberNavController() // NavController dummy untuk preview

    // State tambahan (opsional untuk preview, bisa diatur sesuai kebutuhan)
    var selectedEntryId by remember { mutableStateOf<String?>(null) }
    var isAddingNote by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    val latestEntry = dummyEntries.firstOrNull()

    // 2. Bungkus dengan Tema Aplikasi Anda
    MoodLensTheme {
        // 3. Bangun ulang struktur UI JournalScreen
        Column(modifier = Modifier.fillMaxSize()) {
            JournalHeader( navController = navController )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp,end = 16.dp, bottom = 16.dp),
                //verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Item 1: Kartu Statistik
                item {
                    StatsCard(stats = dummyStats)
                }

                // Item 2: Tombol "Add Note" (jika kondisi terpenuhi)
                if (latestEntry != null && dummyNotesMap[latestEntry.entryId].isNullOrEmpty() && !isAddingNote) {
                    item {
                        AddNoteButton(onClick = { /* Aksi kosong di preview */ })
                    }
                }

                // Item 3: Kartu Input "Add Note" (jika isAddingNote true)
                item {
                    AnimatedVisibility(visible = isAddingNote) { // Atau set isAddingNote=true untuk melihatnya
                        AddNoteCard(
                            noteText = noteText,
                            onNoteChange = { noteText = it },
                            onSaveClick = { /* Aksi kosong */ },
                            onCancelClick = { /* Aksi kosong */ }
                        )
                    }
                }

                // Item 4: Daftar Entri Jurnal
                items(items = dummyEntries, key = { it.entryId }) { entry ->
                    JournalEntryCard(
                        entry = entry,
                        notes = dummyNotesMap[entry.entryId] ?: emptyList(),
                        onClick = { /* Aksi kosong */ },
                        onLongClick = { /* Aksi kosong */ }
                    )
                }
            }
        }
    }
}

data class JournalStats(
    val totalEntries: Int = 0,
    val withNotes: Int = 0,
    val daysTracked: Int = 0
)
