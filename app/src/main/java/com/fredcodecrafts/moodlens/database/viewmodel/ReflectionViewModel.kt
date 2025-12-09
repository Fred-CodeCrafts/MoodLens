package com.fredcodecrafts.moodlens.database.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fredcodecrafts.moodlens.database.AppDatabase
import com.fredcodecrafts.moodlens.database.entities.Question
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.fredcodecrafts.moodlens.database.dao.QuestionsDao
import com.fredcodecrafts.moodlens.database.entities.JournalEntry
import com.fredcodecrafts.moodlens.database.entities.Message
import com.fredcodecrafts.moodlens.database.entities.Note
import com.fredcodecrafts.moodlens.database.repository.JournalRepository
import com.fredcodecrafts.moodlens.database.repository.MessagesRepository
import com.fredcodecrafts.moodlens.database.repository.NotesRepository
import com.fredcodecrafts.moodlens.ui.screens.ReflectionSession // Ensure this imports correctly or move data class
import kotlinx.coroutines.Dispatchers

class ReflectionViewModel(
    private val journalRepo: JournalRepository,
    private val notesRepo: NotesRepository,
    private val messagesRepo: MessagesRepository,
    private val questionsDao: QuestionsDao
) : ViewModel() {

    private val _openingQuestion = MutableStateFlow<Question?>(null)
    val openingQuestion = _openingQuestion.asStateFlow()

    private val _promptQuestions = MutableStateFlow<List<Question>>(emptyList())
    val promptQuestions = _promptQuestions.asStateFlow()

    fun loadQuestionsForEmotion(emotion: String) {
        viewModelScope.launch {
            // Try to get opening question
            var opening = questionsDao.getOpeningQuestion(emotion)
            
            // If missing, seed defaults and try again
            if (opening == null) {
                seedDefaultQuestions()
                opening = questionsDao.getOpeningQuestion(emotion)
            }
            
            _openingQuestion.value = opening
            _promptQuestions.value = questionsDao.getPromptQuestions(emotion)
        }
    }

    private suspend fun seedDefaultQuestions() {
        val defaults = com.fredcodecrafts.moodlens.database.PreloadedQuestions.questions
        questionsDao.insertAll(defaults)
    }

    fun saveReflection(
        session: ReflectionSession,
        aiReflection: String,
        additionalNotes: String?,
        messages: List<Message>,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Update Journal Entry
                val existingEntry = journalRepo.getEntryById(session.entryId)
                val entryToSave = if (existingEntry != null) {
                    existingEntry.copy(aiReflection = aiReflection)
                } else {
                    // Fallback (should not happen if CameraScan saved it)
                    JournalEntry(
                        entryId = session.entryId,
                        userId = com.fredcodecrafts.moodlens.utils.SessionManager.currentUserId ?: "default_user",
                        mood = session.mood,
                        timestamp = session.startTime,
                        locationName = null,
                        aiReflection = aiReflection
                    )
                }
                journalRepo.insertEntry(entryToSave) // Triggers Sync

                // 2. Save Notes
                if (!additionalNotes.isNullOrBlank()) {
                    val note = Note(
                        noteId = java.util.UUID.randomUUID().toString(),
                        entryId = session.entryId,
                        content = additionalNotes
                    )
                    notesRepo.insert(note) // Triggers Sync
                }

                // 3. Save Messages
                if (messages.isNotEmpty()) {
                    // We iterate because Repo sync might be single item or list. 
                    // Our repo has insertAll but we only added sync to insert(single). 
                    // Let's use loop or update repo. Currently repo syncs on insert(single).
                    // We'll verify MessageRepository.
                    
                    // Optimization: We can insertAll locally, then sync one by one or batch sync.
                    // For now, let's just loop locally to ensure sync triggers if insertAll doesn't have it.
                    // Wait, I updated insert(Message). Did I update insertAll? No.
                    // So I should call insert(Message) for each to trigger sync.
                    messages.forEach { messagesRepo.insert(it) }
                }

                launch(Dispatchers.Main) {
                    onComplete()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
