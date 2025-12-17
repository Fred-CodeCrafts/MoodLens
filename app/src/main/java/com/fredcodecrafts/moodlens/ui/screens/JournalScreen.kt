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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.FavoriteBorder
import com.fredcodecrafts.moodlens.ui.theme.GradientPrimary
import androidx.compose.ui.res.painterResource // ✅ Jangan lupa import ini
import com.fredcodecrafts.moodlens.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
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
import com.fredcodecrafts.moodlens.database.entities.JournalEntry
import com.fredcodecrafts.moodlens.database.entities.Note
import com.fredcodecrafts.moodlens.navigation.Screen
import com.fredcodecrafts.moodlens.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.fredcodecrafts.moodlens.database.repository.JournalRepository
import com.fredcodecrafts.moodlens.database.viewmodel.JournalViewModel
import androidx.compose.ui.focus.onFocusChanged
import com.fredcodecrafts.moodlens.components.VirtualKeyboard

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

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun JournalScreen(
    navController: NavController,
    db: AppDatabase, // Accept AppDatabase so Screen doesn't call DAOs directly
    userId: String
) {
    // Keep UI-level local state (selected entry, adding note UI state, input text)
    var entryToDelete by remember { mutableStateOf<JournalEntry?>(null) }
    var selectedEntryId by remember { mutableStateOf<String?>(null) }
    var isAddingNote by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }

    // Build repository and ViewModel via its Factory
    val journalRepo = remember { JournalRepository(db.journalDao(), db.notesDao(), db.messagesDao(), db.moodScanStatDao()) }
    val notesRepo = remember { com.fredcodecrafts.moodlens.database.repository.NotesRepository(db.notesDao()) }
    
    val viewModel: JournalViewModel = viewModel(
        factory = JournalViewModel.Factory(journalRepo, notesRepo, userId)
    )

    // Observe ViewModel state
    val entries by viewModel.entries.collectAsState()
    val notesMap by viewModel.notesMap.collectAsState()
    val loading by viewModel.loading.collectAsState()

    // Compute journal stats locally from entries & notesMap (UI-only derived)
    val stats by remember(entries, notesMap) {
        derivedStateOf {
            val total = entries.size
            val withNotesCount = notesMap.values.count { it.isNotEmpty() }
            val daysTracked = entries.map { formatTimestampToDate(it.timestamp) }.distinct().size
            JournalStats(totalEntries = total, withNotes = withNotesCount, daysTracked = daysTracked)
        }
    }

    val scope = rememberCoroutineScope()

    // Save note: call ViewModel, update UI state locally
    val onSaveNote: () -> Unit = {
        if (noteText.isNotBlank() && selectedEntryId != null) {
            // Ask ViewModel to persist
            viewModel.addNote(selectedEntryId!!, noteText)

            // Optimistic UI update (ViewModel will also update notesMap; this keeps UI snappy)
            // but we also clear the input and hide add note UI
            isAddingNote = false
            noteText = ""
            selectedEntryId = null
        }
    }

    // Delete entry: call ViewModel
    val onDeleteEntry: (JournalEntry) -> Unit = { entry ->
        // Optimistic UI update removed here because ViewModel exposes entries already;
        // but keep a local optimistic filter to keep UX snappy while ViewModel completes
        // (ViewModel will update entries flow)
        viewModel.deleteEntry(entry.entryId)
    }

    // Confirm deletion dialog
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

    // State for focus and IME to determine Virtual Keyboard visibility
    var isInputFocused by remember { mutableStateOf(false) }
    val imeVisible = WindowInsets.isImeVisible

    // Use a Column to stack content and potential keyboard
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding() // Handled here or in Scaffold if used
            .imePadding() // React to real keyboard
    ) {
        JournalHeader(
            navController = navController,
            onSyncClick = { viewModel.backupData() }
        )

        // Main Content Area (Weight 1f to fill space above keyboard)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MainPurple)
                }
                entries.isEmpty() -> EmptyJournalView {
                    scope.launch { viewModel.refreshAll() }
                }
                else -> {
                    val latestEntry = entries.firstOrNull()
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
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
                            androidx.compose.animation.AnimatedVisibility(visible = isAddingNote) {
                                AddNoteCard(
                                    noteText = noteText,
                                    onNoteChange = { noteText = it },
                                    onSaveClick = onSaveNote,
                                    onCancelClick = {
                                        isAddingNote = false
                                        noteText = ""
                                        selectedEntryId = null
                                        isInputFocused = false // Reset focus state
                                    },
                                    onFocusChanged = { isInputFocused = it }
                                )
                            }
                        }

                        items(items = entries, key = { it.entryId }) { entry ->
                            JournalEntryCard(
                                entry = entry,
                                notes = notesMap[entry.entryId] ?: emptyList(),
                                onClick = {
                                    navController.navigate(Screen.Reflection.createRoute(entry.entryId, entry.mood))
                                },
                                onLongClick = { entryToDelete = entry }
                            )
                        }
                    }
                }
            }
        }

        // Virtual Keyboard: Show if focused AND Real Keyboard (IME) is NOT visible
        if (isInputFocused && !imeVisible) {
            VirtualKeyboard()
        }
    }
}

// -- Supporting Composables (kept unchanged) --

@Composable
fun JournalHeader(navController: NavController, onSyncClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Back button navigates to Home
        IconButton(
            onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Journal.route) { inclusive = true }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF1A1A1A)
            )
        }

        // Centered title
        Box(
            modifier = Modifier.weight(1f),
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

        // Sync / Backup Button
        IconButton(onClick = onSyncClick) {
             Icon(
                imageVector = Icons.Default.Refresh, 
                contentDescription = "Backup Data",
                tint = MainPurple
            )
        }
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
                            text = entry.locationName ?: "No location",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
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
    onCancelClick: () -> Unit,
    onFocusChanged: (Boolean) -> Unit = {}
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
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { onFocusChanged(it.isFocused) },
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .background(
                            brush = GradientPrimary,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .then(
                            if (!noteText.isNotBlank()) Modifier.graphicsLayer(alpha = 0.5f) else Modifier
                        )

                ) {
                    Text("Save")
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
    // Inline dummy entry and note for preview (no DummyData and no DB access)
    val dummyEntry = JournalEntry(
        entryId = "preview-1",
        userId = "preview-user",
        mood = "happy",
        timestamp = System.currentTimeMillis(),
        locationName = "Jakarta",
        aiReflection = "You did great today!"
    )
    val dummyNotes = listOf(
        Note(noteId = "n1", entryId = dummyEntry.entryId, content = "Felt good after coffee"),
        Note(noteId = "n2", entryId = dummyEntry.entryId, content = "Walked 20 minutes")
    )
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
    name = "Journal Screen Full Preview (Inline Dummy Data)"
)
@Composable
fun JournalScreenFullPreview() {
    // Inline preview data (no DB access)
    val dummyEntries = listOf(
        JournalEntry(
            entryId = "e1",
            userId = "u1",
            mood = "happy",
            timestamp = System.currentTimeMillis(),
            locationName = "Jakarta",
            aiReflection = "Keep it up!"
        ),
        JournalEntry(
            entryId = "e2",
            userId = "u1",
            mood = "anxious",
            timestamp = System.currentTimeMillis() - 86_400_000L,
            locationName = null,
            aiReflection = null
        )
    )
    val dummyNotes = listOf(
        Note(noteId = "n1", entryId = "e1", content = "Had a good meeting"),
    )
    val dummyNotesMap = dummyNotes.groupBy { it.entryId }
    val dummyStats = JournalStats(
        totalEntries = dummyEntries.size,
        withNotes = dummyNotesMap.filterValues { it.isNotEmpty() }.size,
        daysTracked = dummyEntries.map { formatTimestampToDate(it.timestamp) }.distinct().size
    )
    val navController = rememberNavController() // NavController dummy for preview
    var isAddingNote by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    val latestEntry = dummyEntries.firstOrNull()

    MoodLensTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            JournalHeader(navController = navController)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            ) {
                item {
                    StatsCard(stats = dummyStats)
                }

                if (latestEntry != null && dummyNotesMap[latestEntry.entryId].isNullOrEmpty() && !isAddingNote) {
                    item {
                        AddNoteButton(onClick = { /* no-op in preview */ })
                    }
                }

                item {
                    AnimatedVisibility(visible = isAddingNote) {
                        AddNoteCard(
                            noteText = noteText,
                            onNoteChange = { noteText = it },
                            onSaveClick = { /* no-op */ },
                            onCancelClick = { /* no-op */ }
                        )
                    }
                }

                items(items = dummyEntries, key = { it.entryId }) { entry ->
                    JournalEntryCard(
                        entry = entry,
                        notes = dummyNotesMap[entry.entryId] ?: emptyList(),
                        onClick = { /* no-op */ },
                        onLongClick = { /* no-op */ }
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
