package com.fredcodecrafts.moodlens.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fredcodecrafts.moodlens.components.ChatMessageBubble
import com.fredcodecrafts.moodlens.database.AppDatabase
import com.fredcodecrafts.moodlens.database.entities.JournalEntry
import com.fredcodecrafts.moodlens.database.entities.Message
import com.fredcodecrafts.moodlens.database.entities.Note
import com.fredcodecrafts.moodlens.database.entities.Question
import com.fredcodecrafts.moodlens.database.viewmodel.ReflectionViewModel
import com.fredcodecrafts.moodlens.ui.theme.gradientPrimary 
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import com.fredcodecrafts.moodlens.components.VirtualKeyboard

// Repositories
import com.fredcodecrafts.moodlens.database.repository.JournalRepository
import com.fredcodecrafts.moodlens.database.repository.MessagesRepository
import com.fredcodecrafts.moodlens.database.repository.NotesRepository

// --- DATA CLASSES & HELPERS ---

data class ReflectionSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val entryId: String,
    val mood: String,
    val questions: List<Question>,
    val responses: MutableMap<String, String> = mutableMapOf(),
    val startTime: Long = System.currentTimeMillis(),
    var endTime: Long? = null,
    var aiReflection: String? = null,
    var additionalNotes: String? = null
)

object MoodMapper {
    fun mapMoodToEmotion(mood: String): String {
        return when (mood.lowercase()) {
            "happy", "joy", "happiness" -> "Happiness"
            "sad", "sadness" -> "Sadness"
            "anger", "angry" -> "Anger"
            "anxious", "anxiety", "fear" -> "Anxiety"
            "stress", "stressed", "overwhelm", "tired" -> "Stress"
            else -> "Happiness"
        }
    }
}

val MainPurple = Color(0xFF6B46C1)
val AccentPink = Color(0xFFEC4899)
val TextPrimary = Color(0xFF1F2937)
val TextSecondary = Color(0xFF6B7280)


class ReflectionViewModelFactory(
    private val context: Context,
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReflectionViewModel::class.java)) {
            val journalRepo = JournalRepository(
                database.journalDao(),
                database.notesDao(),
                database.messagesDao(),
                database.moodScanStatDao()
            )
            val notesRepo = NotesRepository(database.notesDao())
            val messagesRepo = MessagesRepository(database.messagesDao())
            
            return ReflectionViewModel(journalRepo, notesRepo, messagesRepo, database.questionsDao()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// --- 1. STATEFUL COMPOSABLE (THE LOGIC / BRAIN) ---
@Composable
fun ReflectionScreen(
    database: AppDatabase,
    entryId: String,
    currentMood: String,
    onNavigateBack: () -> Unit,
    onReflectionComplete: (String) -> Unit,
    onFinishAndNavigate: () -> Unit
) {
    val context = LocalContext.current
    val reflectionViewModel: ReflectionViewModel = viewModel(
        factory = ReflectionViewModelFactory(context, database)
    )

    val scope = rememberCoroutineScope()
    // DAOs removed, using ViewModel logic

    val openingQuestion by reflectionViewModel.openingQuestion.collectAsState()
    val promptQuestionsFromVm by reflectionViewModel.promptQuestions.collectAsState()

    // State definitions
    var session by remember {
        mutableStateOf(ReflectionSession(entryId = entryId, mood = currentMood, questions = emptyList()))
    }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var currentResponse by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    var showSummary by remember { mutableStateOf(false) }
    var showAdditionalNotes by remember { mutableStateOf(false) }
    var additionalNotes by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var hasShownOpening by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    
    // Note: session.questions might be empty initially. Ensure UI handles empty state gracefully.
    val promptQuestions = session.questions.filter { it.type == "prompt" }

    val progress = if (promptQuestions.isNotEmpty()) {
        (session.responses.size.toFloat()) / promptQuestions.size.toFloat()
    } else 0f

    // Helper functions inside Logic Scope
    fun scrollToBottom() {
        scope.launch {
            delay(100)
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    fun completeReflection(includeNotes: Boolean) {
        scope.launch {
            if (includeNotes && additionalNotes.isNotBlank()) {
                session.additionalNotes = additionalNotes
                messages = messages + Message(
                    messageId = UUID.randomUUID().toString(), entryId = entryId,
                    text = "Additional notes: $additionalNotes", isUser = true, timestamp = System.currentTimeMillis()
                )
            }

            isTyping = true
            delay(2000)

            val aiReflection = generateAIReflection(session)
            session.aiReflection = aiReflection
            session.endTime = System.currentTimeMillis()

            messages = messages + Message(
                messageId = UUID.randomUUID().toString(), entryId = entryId,
                text = "✨ $aiReflection", isUser = false, timestamp = System.currentTimeMillis()
            )

            // Database Operations delegated to ViewModel (Handles Sync and existing Entry Update)
            reflectionViewModel.saveReflection(
                session = session,
                aiReflection = aiReflection,
                additionalNotes = if (includeNotes) additionalNotes else null,
                messages = messages
            ) {
                 isTyping = false
                 showSummary = true
                 showAdditionalNotes = false
                 scrollToBottom()
                 onReflectionComplete(aiReflection)
            }
        }
    }

    fun handleSendResponse() {
        if (currentResponse.isBlank()) return

        scope.launch {
            messages = messages + Message(
                messageId = UUID.randomUUID().toString(), entryId = entryId,
                text = currentResponse, isUser = true, timestamp = System.currentTimeMillis()
            )

            val promptQs = session.questions.filter { it.type == "prompt" }
            if (currentQuestionIndex < promptQs.size) {
                session.responses[promptQs[currentQuestionIndex].questionId] = currentResponse
            }
            currentResponse = ""
            scrollToBottom()

            isTyping = true
            delay(1500)
            isTyping = false

            if (currentQuestionIndex < promptQs.size - 1) {
                currentQuestionIndex++
                val transitionMessage = getTransitionMessage(currentQuestionIndex, promptQs.size)
                if (transitionMessage.isNotEmpty()) {
                    messages = messages + Message(
                        messageId = UUID.randomUUID().toString(), entryId = entryId,
                        text = transitionMessage, isUser = false, timestamp = System.currentTimeMillis()
                    )
                    delay(800)
                }
                messages = messages + Message(
                    messageId = UUID.randomUUID().toString(), entryId = entryId,
                    text = promptQs[currentQuestionIndex].text, isUser = false, timestamp = System.currentTimeMillis()
                )
                scrollToBottom()
            } else {
                showAdditionalNotes = true
                messages = messages + Message(
                    messageId = UUID.randomUUID().toString(), entryId = entryId,
                    text = "Thank you for sharing. Would you like to add any additional notes?",
                    isUser = false, timestamp = System.currentTimeMillis()
                )
                scrollToBottom()
            }
        }
    }

    // Logic Initialization
    LaunchedEffect(key1 = Unit) {
        val emotion = MoodMapper.mapMoodToEmotion(currentMood)
        reflectionViewModel.loadQuestionsForEmotion(emotion)
    }

    // React to questions loading
    LaunchedEffect(openingQuestion, promptQuestionsFromVm) {
        if (openingQuestion != null || promptQuestionsFromVm.isNotEmpty()) {
            val loadedQuestions = listOfNotNull(openingQuestion) + promptQuestionsFromVm
            session = session.copy(questions = loadedQuestions)
        }
    }
    
    // Message Initialization (dependent on session.questions having data)
    LaunchedEffect(session.questions) {
        if (session.questions.isNotEmpty() && messages.isEmpty()) {         
             if (!hasShownOpening) {
                 // Check existing messages logic omitted for brevity in this specific fix, assuming new session
                 
                 delay(500)
                 val openingQ = session.questions.firstOrNull { it.type == "opening" }
                 if (openingQ != null) {
                     messages = messages + Message(UUID.randomUUID().toString(), entryId, openingQ.text, false, System.currentTimeMillis())
                     hasShownOpening = true
                 }
                 delay(1500)
                 val firstPrompt = session.questions.filter { it.type == "prompt" }.firstOrNull()
                 if (firstPrompt != null) {
                      messages = messages + Message(UUID.randomUUID().toString(), entryId, firstPrompt.text, false, System.currentTimeMillis())
                 }
             }
        }
    }

    // Call Stateless UI
    ReflectionScreenContent(
        messages = messages,
        isTyping = isTyping,
        showSummary = showSummary,
        showAdditionalNotes = showAdditionalNotes,
        additionalNotes = additionalNotes,
        currentResponse = currentResponse,
        progress = progress,
        currentQuestionNumber = currentQuestionIndex + 1,
        totalQuestions = promptQuestions.size,
        session = session,
        placeholder = getPlaceholderForQuestion(promptQuestions.getOrNull(currentQuestionIndex)),
        listState = listState,
        onBackClick = onNavigateBack,
        onResponseChange = { currentResponse = it },
        onSend = { handleSendResponse() },
        onNotesChange = { additionalNotes = it },
        onSkipNotes = { completeReflection(false) },
        onSaveNotes = { completeReflection(true) },
        onFinish = onFinishAndNavigate
    )
}
@Composable
fun ReflectionScreenContent(
    messages: List<Message>,
    isTyping: Boolean,
    showSummary: Boolean,
    showAdditionalNotes: Boolean,
    additionalNotes: String,
    currentResponse: String,
    progress: Float,
    currentQuestionNumber: Int,
    totalQuestions: Int,
    session: ReflectionSession,
    placeholder: String,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onBackClick: () -> Unit,
    onResponseChange: (String) -> Unit,
    onSend: () -> Unit,
    onNotesChange: (String) -> Unit,
    onSkipNotes: () -> Unit,
    onSaveNotes: () -> Unit,
    onFinish: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent, 
        topBar = {
            ReflectionHeaderSection(
                progress = progress,
                onBackClick = onBackClick,
                currentQuestionNumber = currentQuestionNumber,
                totalQuestions = totalQuestions
            )
        },
        bottomBar = {
            ReflectionInputSection(
                showSummary = showSummary,
                showAdditionalNotes = showAdditionalNotes,
                session = session,
                isTyping = isTyping,
                currentResponse = currentResponse,
                additionalNotes = additionalNotes,
                placeholder = placeholder,
                onResponseChange = onResponseChange,
                onSend = onSend,
                onNotesChange = onNotesChange,
                onSkipNotes = onSkipNotes,
                onSaveNotes = onSaveNotes,
                onFinish = onFinish
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientPrimary()) 
                .padding(paddingValues)
        ) {
            ReflectionBodySection(
                messages = messages,
                isTyping = isTyping,
                listState = listState
            )
        }
    }
}

// --- DEFINE SECTION 1: HEADER ---
@Composable
fun ReflectionHeaderSection(
    progress: Float,
    onBackClick: () -> Unit,
    currentQuestionNumber: Int,
    totalQuestions: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .statusBarsPadding()
            .padding(top = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Heart",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Reflection Session",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                if (totalQuestions > 0) {
                    Text(
                        text = "Question $currentQuestionNumber of $totalQuestions",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// --- DEFINE SECTION 2: BODY ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReflectionBodySection(
    messages: List<Message>,
    isTyping: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    // Auto-scroll to bottom when keyboard (IME) opens
    val imeVisible = WindowInsets.isImeVisible
    LaunchedEffect(imeVisible, messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(messages) { message ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInHorizontally()
            ) {
                ChatMessageBubble(message = message)
            }
        }

        if (isTyping) {
            item {
                TypingIndicator()
            }
        }
    }
}

// --- DEFINE SECTION 3: INPUT ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReflectionInputSection(
    showSummary: Boolean,
    showAdditionalNotes: Boolean,
    session: ReflectionSession,
    isTyping: Boolean,
    currentResponse: String,
    additionalNotes: String,
    placeholder: String,
    onResponseChange: (String) -> Unit,
    onSend: () -> Unit,
    onNotesChange: (String) -> Unit,
    onSkipNotes: () -> Unit,
    onSaveNotes: () -> Unit,
    onFinish: () -> Unit
) {
    // Detect IME visibility to toggle Virtual Keyboard
    val imeVisible = WindowInsets.isImeVisible
    var isInputFocused by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .navigationBarsPadding() // Keep this to respect gesture bar
            .imePadding() // Pushes up when real keyboard opens
    ) {
        when {
            showSummary -> {
                ReflectionSummary(
                    session = session,
                    onDone = onFinish
                )
            }
            showAdditionalNotes -> {
                AdditionalNotesInput(
                    notes = additionalNotes,
                    onNotesChange = onNotesChange,
                    onSkip = onSkipNotes,
                    onSave = onSaveNotes
                )
            }
            else -> {
                if (!isTyping) {
                    ReflectionInputArea(
                        currentResponse = currentResponse,
                        onResponseChange = onResponseChange,
                        onSend = onSend,
                        placeholder = placeholder,
                        onFocusChanged = { isInputFocused = it }
                    )
                    
                    // Show Virtual Keyboard ONLY if input is focused AND real keyboard is NOT visible
                    // This satisfies "simulate" requesting for users/demos who want to see a keyboard 
                    // without the real one, or if hardware keyboard is attached.
                    if (isInputFocused && !imeVisible) {
                         com.fredcodecrafts.moodlens.components.VirtualKeyboard()
                    }
                }
            }
        }
    }
}

// --- 3. SUB-COMPONENTS (UI PARTS) ---

@Composable
private fun ReflectionInputArea(
    currentResponse: String,
    onResponseChange: (String) -> Unit,
    onSend: () -> Unit,
    placeholder: String,
    onFocusChanged: (Boolean) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = currentResponse,
                onValueChange = onResponseChange,
                placeholder = { Text(placeholder, color = Color.White) },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .onFocusChanged { onFocusChanged(it.isFocused) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                maxLines = 4
            )

            IconButton(
                onClick = onSend,
                enabled = currentResponse.isNotBlank(),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (currentResponse.isNotBlank()) MainPurple else Color.Gray,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                    disabledContentColor = Color.White
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
        Text(
            text = "💜 Your thoughts are safe and valued",
            fontSize = 12.sp,
            color = Color.White,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun AdditionalNotesInput(
    notes: String,
    onNotesChange: (String) -> Unit,
    onSkip: () -> Unit,
    onSave: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Any additional thoughts?",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                placeholder = { Text("Optional: Add any other thoughts...", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MainPurple,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onSkip, modifier = Modifier.weight(1f)) {
                    Text("Skip")
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MainPurple)
                ) {
                    Text("Add & Complete")
                }
            }
        }
    }
}

@Composable
private fun ReflectionSummary(session: ReflectionSession, onDone: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(colors = listOf(MainPurple, AccentPink))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = "Complete", tint = Color.White, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Reflection Complete! 🌟", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Thank you for taking time to reflect on your feelings", fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center)

            if (!session.aiReflection.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0E6FF))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("✨ Your Personalized Insight", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MainPurple)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(session.aiReflection!!, fontSize = 14.sp, color = TextPrimary, lineHeight = 20.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MainPurple)) {
                Text("Complete Reflection")
            }
        }
    }
}


@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) { index -> AnimatedDot(delayMillis = index * 100L) }
            }
        }
    }
}

@Composable
private fun AnimatedDot(delayMillis: Long) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMillis)
        while (true) {
            visible = !visible
            delay(600)
        }
    }
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(if (visible) Color.Gray else Color.Gray.copy(alpha = 0.3f))
    )
}

// Logic Helpers (Private) - kept for local AI generation if offline or for immediate feedback
private fun generateAIReflection(session: ReflectionSession): String {
    val emotion = MoodMapper.mapMoodToEmotion(session.mood)
    val hasResponses = session.responses.isNotEmpty()

    val baseReflection = when (emotion) {
        "Happiness" -> {
            if (hasResponses) {
                "Your joy radiates through your words today. It's wonderful to see you embracing these positive moments. ${
                    if (session.responses.values.any { it.contains("friend", ignoreCase = true) || it.contains("family", ignoreCase = true) })
                        "The connections you've mentioned seem to bring you real happiness. "
                    else ""
                }Keep nurturing what brings you this lightness and remember these feelings during challenging times."
            } else {
                "It's beautiful that you're taking time to acknowledge your happiness. These moments of joy are precious and worth celebrating."
            }
        }
        "Sadness" -> {
            if (hasResponses) {
                "Thank you for trusting me with these difficult feelings. Your sadness is valid and it's brave of you to acknowledge it. ${
                    if (session.responses.values.any { it.length > 50 })
                        "I can sense the depth of what you're experiencing. "
                    else ""
                }Remember that this feeling will pass, and it's okay to take all the time you need to process it. You're not alone in this."
            } else {
                "I recognize that you're going through a difficult time. Even though it's hard to put into words right now, acknowledging your sadness is an important step."
            }
        }
        "Anger" -> {
            if (hasResponses) {
                "Your feelings are completely valid. ${
                    if (session.responses.values.any { it.contains("unfair", ignoreCase = true) || it.contains("wrong", ignoreCase = true) })
                        "It sounds like an important boundary may have been crossed. "
                    else ""
                }Anger often signals that something we value has been threatened or disrespected. Take the time you need to process this, and remember that you have the power to channel this energy constructively."
            } else {
                "It takes strength to acknowledge anger. This emotion often carries important messages about our boundaries and values."
            }
        }
        "Anxiety" -> {
            if (hasResponses) {
                "I can sense the weight of worry you're carrying. ${
                    if (session.responses.values.any { it.contains("future", ignoreCase = true) || it.contains("what if", ignoreCase = true) })
                        "It's natural to feel uncertain about what's ahead. "
                    else ""
                }Your awareness of these anxious feelings is actually a strength. Remember to take things one step at a time, and focus on what you can control in this moment."
            } else {
                "Anxiety can feel overwhelming, and I want you to know it's okay to feel this way. Sometimes our minds try to protect us by preparing for every possibility."
            }
        }
        "Stress" -> {
            if (hasResponses) {
                "It sounds like you're juggling a lot right now. ${
                    if (session.responses.values.any { it.contains("work", ignoreCase = true) || it.contains("deadline", ignoreCase = true) })
                        "The pressure you're facing is real and demanding. "
                    else ""
                }Remember that it's okay to not have everything figured out at once. Breaking things down into smaller, manageable steps can help."
            } else {
                "Feeling stressed is your body's way of telling you that you need to pause and recharge. It's not a sign of weakness, but a call to take care of yourself."
            }
        }
        else -> {
            "Thank you for taking this time to reflect on your feelings. Self-awareness is a powerful tool for growth and healing."
        }
    }

    val ending = if (!session.additionalNotes.isNullOrBlank()) {
        " Your additional thoughts show deep self-reflection. Keep honoring your feelings this way."
    } else ""

    return baseReflection + ending
}


private fun getTransitionMessage(currentIndex: Int, totalQuestions: Int): String {
    val progress = (currentIndex + 1).toFloat() / totalQuestions.toFloat()
    return when {
        progress < 0.3f -> listOf("Let's explore this further...", "Thank you for sharing that...", "I hear you...").random()
        progress < 0.6f -> listOf("You're doing great...", "Let's dig a little deeper...", "That's insightful...").random()
        progress < 0.9f -> listOf("We're almost there...", "One more thing to explore...", "Thank you for being so open...").random()
        else -> ""
    }
}

private fun getPlaceholderForQuestion(question: Question?): String {
    return when {
        question == null -> "Share your thoughts..."
        question.text.contains("trigger", ignoreCase = true) -> "What happened?"
        question.text.contains("feeling", ignoreCase = true) -> "Describe the feeling..."
        question.text.contains("think", ignoreCase = true) || question.text.contains("thought", ignoreCase = true) -> "Share your thoughts..."
        else -> "Type your response..."
    }
}