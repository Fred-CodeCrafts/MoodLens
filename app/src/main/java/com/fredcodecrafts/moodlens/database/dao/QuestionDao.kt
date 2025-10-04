package com.fredcodecrafts.moodlens.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fredcodecrafts.moodlens.database.entities.Question

@Dao
interface QuestionsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<Question>)

    @Query("SELECT * FROM questions_offline WHERE emotionLabel = :emotion AND type = 'opening'")
    suspend fun getOpeningQuestion(emotion: String): Question?

    @Query("SELECT * FROM questions_offline WHERE emotionLabel = :emotion AND type = 'prompt'")
    suspend fun getPromptQuestions(emotion: String): List<Question>
}
