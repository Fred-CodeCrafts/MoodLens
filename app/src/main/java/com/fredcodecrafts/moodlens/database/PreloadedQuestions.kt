package com.fredcodecrafts.moodlens.database

import com.fredcodecrafts.moodlens.database.entities.Question

object PreloadedQuestions {
    val questions = listOf(
        // SADNESS
        Question(
            questionId = "sad_opening",
            emotionLabel = "Sadness",
            type = "opening",
            text = "It looks like you’ve been feeling low. Let’s take a moment to explore where this feeling comes from and how you can care for yourself."
        ),
        Question(
            questionId = "sad_1",
            emotionLabel = "Sadness",
            type = "prompt",
            text = "What triggered this sadness today? Did something happen, or is it more of a general mood?"
        ),
        Question(
            questionId = "sad_2",
            emotionLabel = "Sadness",
            type = "prompt",
            text = "Is there a thought you keep repeating to yourself?"
        ),
        Question(
            questionId = "sad_3",
            emotionLabel = "Sadness",
            type = "prompt",
            text = "What’s one small thing that might bring comfort right now?"
        ),

        // ANGER
        Question(
            questionId = "anger_opening",
            emotionLabel = "Anger",
            type = "opening",
            text = "Anger can feel overwhelming, but it often points to something important—like boundaries being crossed."
        ),
        Question(
            questionId = "anger_1",
            emotionLabel = "Anger",
            type = "prompt",
            text = "What or who triggered your anger?"
        ),
        Question(
            questionId = "anger_2",
            emotionLabel = "Anger",
            type = "prompt",
            text = "Is the anger masking another feeling (hurt, fear, disappointment)?"
        ),
        Question(
            questionId = "anger_3",
            emotionLabel = "Anger",
            type = "prompt",
            text = "What would be a healthy way to express or release it?"
        ),
        Question(
            questionId = "anger_4",
            emotionLabel = "Anger",
            type = "prompt",
            text = "Which boundaries might need reinforcing?"
        ),

        // ANXIETY / FEAR
        Question(
            questionId = "anxiety_opening",
            emotionLabel = "Anxiety",
            type = "opening",
            text = "Anxiety often comes from uncertainty or overthinking the future. Let’s untangle it a little."
        ),
        Question(
            questionId = "anxiety_1",
            emotionLabel = "Anxiety",
            type = "prompt",
            text = "What situation is making you feel anxious right now?"
        ),
        Question(
            questionId = "anxiety_2",
            emotionLabel = "Anxiety",
            type = "prompt",
            text = "What’s the worst-case scenario you’re imagining?"
        ),
        Question(
            questionId = "anxiety_3",
            emotionLabel = "Anxiety",
            type = "prompt",
            text = "How likely is it, really?"
        ),
        Question(
            questionId = "anxiety_4",
            emotionLabel = "Anxiety",
            type = "prompt",
            text = "What’s one thing you can control right now?"
        ),

        // HAPPINESS / JOY
        Question(
            questionId = "joy_opening",
            emotionLabel = "Happiness",
            type = "opening",
            text = "That’s wonderful! Let’s pause to capture what’s bringing you joy so you can return to it later."
        ),
        Question(
            questionId = "joy_1",
            emotionLabel = "Happiness",
            type = "prompt",
            text = "What made you feel happy today?"
        ),
        Question(
            questionId = "joy_2",
            emotionLabel = "Happiness",
            type = "prompt",
            text = "Did anyone in particular make you feel this way?"
        ),
        Question(
            questionId = "joy_3",
            emotionLabel = "Happiness",
            type = "prompt",
            text = "How are you feeling physically?"
        ),
        Question(
            questionId = "joy_4",
            emotionLabel = "Happiness",
            type = "prompt",
            text = "How can you create more moments like this?"
        ),

        // STRESS / OVERWHELM
        Question(
            questionId = "stress_opening",
            emotionLabel = "Stress",
            type = "opening",
            text = "Stress tells us our resources feel stretched. Let’s see what’s weighing on you."
        ),
        Question(
            questionId = "stress_1",
            emotionLabel = "Stress",
            type = "prompt",
            text = "What’s the biggest source of stress right now?"
        ),
        Question(
            questionId = "stress_2",
            emotionLabel = "Stress",
            type = "prompt",
            text = "Which part of it can you control, and which part is outside your control?"
        ),
        Question(
            questionId = "stress_3",
            emotionLabel = "Stress",
            type = "prompt",
            text = "What’s one small step you can take to reduce the load?"
        ),
        Question(
            questionId = "stress_4",
            emotionLabel = "Stress",
            type = "prompt",
            text = "Who or what can support you?"
        )
    )

}