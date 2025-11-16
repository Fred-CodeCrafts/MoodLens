package com.fredcodecrafts.moodlens.database.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fredcodecrafts.moodlens.database.AppDatabase
import com.fredcodecrafts.moodlens.database.entities.Question
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReflectionViewModel(context: Context) : ViewModel() {

    private val db = AppDatabase.getDatabase(context)
    private val questionsDao = db.questionsDao()

    private val _openingQuestion = MutableStateFlow<Question?>(null)
    val openingQuestion = _openingQuestion.asStateFlow()

    private val _promptQuestions = MutableStateFlow<List<Question>>(emptyList())
    val promptQuestions = _promptQuestions.asStateFlow()

    fun loadQuestionsForEmotion(emotion: String) {
        viewModelScope.launch {
            _openingQuestion.value = questionsDao.getOpeningQuestion(emotion)
            _promptQuestions.value = questionsDao.getPromptQuestions(emotion)
        }
    }
}
