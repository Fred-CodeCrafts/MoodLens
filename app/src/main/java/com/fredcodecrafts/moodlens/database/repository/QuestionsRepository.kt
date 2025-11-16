package com.fredcodecrafts.moodlens.database.repository

import com.fredcodecrafts.moodlens.database.dao.QuestionsDao
import com.fredcodecrafts.moodlens.database.entities.Question

class QuestionsRepository(
    private val questionsDao: QuestionsDao
) {

    suspend fun getAllQuestions(): List<Question> {
        return questionsDao.getAllQuestions()
    }

    suspend fun insertAll(questions: List<Question>) {
        questionsDao.insertAll(questions)
    }

    suspend fun getOpeningQuestion(emotion: String): Question? {
        return questionsDao.getOpeningQuestion(emotion)
    }

    suspend fun getPromptQuestions(emotion: String): List<Question> {
        return questionsDao.getPromptQuestions(emotion)
    }
}
