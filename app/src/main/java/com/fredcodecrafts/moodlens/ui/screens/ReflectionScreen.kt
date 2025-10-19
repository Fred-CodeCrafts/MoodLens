package com.fredcodecrafts.moodlens.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fredcodecrafts.moodlens.components.ChatMessageBubble
import com.fredcodecrafts.moodlens.components.InputField
import com.fredcodecrafts.moodlens.database.PreloadedQuestions
import com.fredcodecrafts.moodlens.database.entities.Message
import com.fredcodecrafts.moodlens.database.entities.Question
import com.fredcodecrafts.moodlens.ui.theme.GradientPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import com.fredcodecrafts.moodlens.ui.theme.gradientPrimary
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Use AutoMirrored for back arrow
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp


// Data class for the reflection session
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

// Helper to map mood strings to emotion labels in PreloadedQuestions
object MoodMapper {
    fun mapMoodToEmotion(mood: String): String {
        return when (mood.lowercase()) {
            "happy", "joy", "happiness" -> "Happiness"
            "sad", "sadness" -> "Sadness"
            "anger", "angry" -> "Anger"
            "anxious", "anxiety", "fear" -> "Anxiety"
            "stress", "stressed", "overwhelm", "tired" -> "Stress"
            else -> "Happiness" // Default fallback
        }
    }

    fun getQuestionsForMood(mood: String): List<Question> {
        val emotion = mapMoodToEmotion(mood)
        return PreloadedQuestions.questions.filter {
            it.emotionLabel == emotion
        }.sortedBy {
            // Ensure opening question comes first
            when (it.type) {
                "opening" -> 0
                "prompt" -> 1
                else -> 2
            }
        }
    }
}

// Color definitions (add these to your theme file later)
val MainPurple = Color(0xFF6B46C1)
val AccentPink = Color(0xFFEC4899)
val TextPrimary = Color(0xFF1F2937)
val TextSecondary = Color(0xFF6B7280)
val BackgroundColor = Color(0xFFF9FAFB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReflectionScreen(
    entryId: String,
    currentMood: String,
    onNavigateBack: () -> Unit,
    onReflectionComplete: (String) -> Unit,
    onFinishAndNavigate: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // State management
    var session by remember {
        mutableStateOf(
            ReflectionSession(
                entryId = entryId,
                mood = currentMood,
                questions = MoodMapper.getQuestionsForMood(currentMood)
            )
        )
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
    val promptQuestions = session.questions.filter { it.type == "prompt" }
    val progress = if (promptQuestions.isNotEmpty()) {
        (session.responses.size.toFloat()) / promptQuestions.size.toFloat()
    } else 0f

    // Initialize with opening message
    LaunchedEffect(Unit) {
        delay(500)
        val openingQuestion = session.questions.firstOrNull { it.type == "opening" }
        if (openingQuestion != null) {
            messages = messages + Message(
                messageId = UUID.randomUUID().toString(),
                entryId = entryId,
                text = openingQuestion.text,
                isUser = false,
                timestamp = System.currentTimeMillis()
            )
            hasShownOpening = true
        }

        // Show first prompt question after opening
        delay(1500)
        val firstPrompt = promptQuestions.firstOrNull()
        if (firstPrompt != null) {
            messages = messages + Message(
                messageId = UUID.randomUUID().toString(),
                entryId = entryId,
                text = firstPrompt.text,
                isUser = false,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    fun scrollToBottom() {
        scope.launch {
            delay(100)
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    fun handleSendResponse() {
        if (currentResponse.isBlank()) return

        scope.launch {
            // Add user message
            messages = messages + Message(
                messageId = UUID.randomUUID().toString(),
                entryId = entryId,
                text = currentResponse,
                isUser = true,
                timestamp = System.currentTimeMillis()
            )

            // Store response for current prompt question
            if (currentQuestionIndex < promptQuestions.size) {
                session.responses[promptQuestions[currentQuestionIndex].questionId] = currentResponse
            }
            currentResponse = ""

            scrollToBottom()

            // Show typing indicator
            isTyping = true
            delay(1500)
            isTyping = false

            // Move to next question or complete
            if (currentQuestionIndex < promptQuestions.size - 1) {
                currentQuestionIndex++

                // Add transition message
                val transitionMessage = getTransitionMessage(currentQuestionIndex, promptQuestions.size)
                if (transitionMessage.isNotEmpty()) {
                    messages = messages + Message(
                        messageId = UUID.randomUUID().toString(),
                        entryId = entryId,
                        text = transitionMessage,
                        isUser = false,
                        timestamp = System.currentTimeMillis()
                    )

                    delay(800)
                }

                // Add next question
                messages = messages + Message(
                    messageId = UUID.randomUUID().toString(),
                    entryId = entryId,
                    text = promptQuestions[currentQuestionIndex].text,
                    isUser = false,
                    timestamp = System.currentTimeMillis()
                )

                scrollToBottom()

            } else {
                // All questions answered
                showAdditionalNotes = true

                messages = messages + Message(
                    messageId = UUID.randomUUID().toString(),
                    entryId = entryId,
                    text = "Thank you for sharing your thoughts. Would you like to add any additional notes before we complete your reflection?",
                    isUser = false,
                    timestamp = System.currentTimeMillis()
                )

                scrollToBottom()
            }
        }
    }

    fun completeReflection(includeNotes: Boolean = false) {
        scope.launch {
            if (includeNotes && additionalNotes.isNotBlank()) {
                session.additionalNotes = additionalNotes

                messages = messages + Message(
                    messageId = UUID.randomUUID().toString(),
                    entryId = entryId,
                    text = "Additional notes: $additionalNotes",
                    isUser = true,
                    timestamp = System.currentTimeMillis()
                )
            }

            isTyping = true
            delay(2000)

            // Generate AI reflection
            val aiReflection = generateAIReflection(session)
            session.aiReflection = aiReflection
            session.endTime = System.currentTimeMillis()

            messages = messages + Message(
                messageId = UUID.randomUUID().toString(),
                entryId = entryId,
                text = "✨ $aiReflection",
                isUser = false,
                timestamp = System.currentTimeMillis()
            )

            isTyping = false
            showSummary = true
            showAdditionalNotes = false

            scrollToBottom()

            // Callback with reflection
            onReflectionComplete(aiReflection)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientPrimary()),
    ) {
        Scaffold(
            topBar = {
                ReflectionTopBar(
                    progress = progress,
                    onBackClick = onNavigateBack,
                    currentQuestionNumber = currentQuestionIndex + 1,
                    totalQuestions = promptQuestions.size
                )
            }, containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Transparent) // <-- make it transparent
// your linear gradient
            ) {
                // Messages list
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
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

                // Input area
                when {
                    showSummary -> {
                        ReflectionSummary(
                            session = session,
                            onDone = onFinishAndNavigate
                        )
                    }

                    showAdditionalNotes -> {
                        AdditionalNotesInput(
                            notes = additionalNotes,
                            onNotesChange = { additionalNotes = it },
                            onSkip = { completeReflection(false) },
                            onSave = { completeReflection(true) }
                        )
                    }

                    else -> {
                        ReflectionInputArea(
                            currentResponse = currentResponse,
                            onResponseChange = { currentResponse = it },
                            onSend = ::handleSendResponse,
                            placeholder = getPlaceholderForQuestion(
                                promptQuestions.getOrNull(currentQuestionIndex)
                            )
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun ReflectionTopBar(
    progress: Float,
    onBackClick: () -> Unit,
    currentQuestionNumber: Int,
    totalQuestions: Int
) {
    // 1. Use Column as the main container for the header + progress bar
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Add padding for status bar if needed, and bottom padding
            .padding(top = 8.dp, bottom = 0.dp) // No bottom padding here, progress bar handles it
    ) {
        // 2. Use Box to align back button and centered content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min) // Adjust height based on content
                .padding(horizontal = 4.dp, vertical = 4.dp) // Padding around the content
        ) {
            // Back Button
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

            // Centered Content (Heart, Title, Subtitle)
            Column(
                modifier = Modifier.align(Alignment.Center), // Center this column within the Box
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
        } // End of Box

        // 3. Place Progress Bar *below* the Box
//        LinearProgressIndicator(
//            progress = { progress }, // Use lambda syntax for progress
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(6.dp),
//            color = AccentPink,
//            trackColor = MainPurple.copy(alpha = 0.3f) // Ensure track color contrasts slightly
//        )
    } // End of Column
}


@Composable
private fun ReflectionInputArea(
    currentResponse: String,
    onResponseChange: (String) -> Unit,
    onSend: () -> Unit,
    placeholder: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        // ✅ Atur alignment Column jika perlu (misal: CenterHorizontally)
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.Bottom, // Jaga agar TextField dan Button rata bawah
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = currentResponse,
                onValueChange = onResponseChange,
                placeholder = {
                    Text(placeholder, color = Color.White)
                },
                modifier = Modifier
                    .weight(1f) // Biarkan TextField mengisi sisa ruang
                    .padding(end = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    // ✅ Tambahkan textColor di sini
                    focusedTextColor = Color.White, // Warna teks saat fokus
                    unfocusedTextColor = Color.White // Warna teks saat tidak fokus
                    // Mungkin perlu atur warna text color lain (cursor, dll)
                    // cursorColor = Color.White,
                    // focusedLabelColor = Color.White,
                    // unfocusedLabelColor = Color.White.copy(alpha=0.7f)

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
                modifier = Modifier.size(48.dp) // Beri ukuran tetap agar bentuknya konsisten
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }

        Text(
            text = "💜 Your thoughts are safe and valued",
            fontSize = 12.sp,
            color = Color.White,
            modifier = Modifier
                // .align(Alignment.CenterHorizontally) // Tidak perlu karena Column sudah diatur
                .padding(top = 8.dp)
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
                placeholder = {
                    Text("Optional: Add any other thoughts...", color = Color.Gray)
                },
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
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Skip")
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MainPurple
                    )
                ) {
                    Text("Add & Complete")
                }
            }
        }
    }
}

@Composable
private fun ReflectionSummary(
    session: ReflectionSession,
    onDone: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Success icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(MainPurple, AccentPink)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Complete",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Reflection Complete! 🌟",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Thank you for taking time to reflect on your feelings",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            if (!session.aiReflection.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF0E6FF)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "✨ Your Personalized Insight",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MainPurple
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = session.aiReflection!!,
                            fontSize = 14.sp,
                            color = TextPrimary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MainPurple
                )
            ) {
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
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF0F0F0)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) { index ->
                    AnimatedDot(delayMillis = index * 100L)
                }
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
            .background(
                if (visible) Color.Gray else Color.Gray.copy(alpha = 0.3f)
            )
    )
}

// Helper functions
private fun getTransitionMessage(currentIndex: Int, totalQuestions: Int): String {
    val progress = (currentIndex + 1).toFloat() / totalQuestions.toFloat()
    return when {
        progress < 0.3f -> listOf(
            "Let's explore this further...",
            "Thank you for sharing that...",
            "I hear you..."
        ).random()
        progress < 0.6f -> listOf(
            "You're doing great...",
            "Let's dig a little deeper...",
            "That's insightful..."
        ).random()
        progress < 0.9f -> listOf(
            "We're almost there...",
            "One more thing to explore...",
            "Thank you for being so open..."
        ).random()
        else -> ""
    }
}

private fun getPlaceholderForQuestion(question: Question?): String {
    return when {
        question == null -> "Share your thoughts..."
        question.text.contains("trigger", ignoreCase = true) -> "What happened?"
        question.text.contains("feeling", ignoreCase = true) -> "Describe the feeling..."
        question.text.contains("think", ignoreCase = true) ||
                question.text.contains("thought", ignoreCase = true) -> "Share your thoughts..."
        question.text.contains("comfort", ignoreCase = true) -> "What helps you feel better?"
        question.text.contains("control", ignoreCase = true) -> "What can you manage?"
        else -> "Type your response..."
    }
}

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

//@Preview(showBackground = true)
//@Composable
//fun ReflectionScreenPreview() {
//    ReflectionScreen(
//        entryId = "preview_entry",
//        currentMood = "sad",
//        onNavigateBack = {},
//        onReflection = {}
//
//    )
//}
