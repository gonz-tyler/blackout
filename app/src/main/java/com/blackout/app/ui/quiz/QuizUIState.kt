package com.blackout.app.ui.quiz

import com.blackout.app.domain.model.Answer
import com.blackout.app.domain.model.Question

data class QuizUIState(
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val answers: Map<String, Answer> = emptyMap(),
    val isFinished: Boolean = false,
) {
//    val direction: Any
    val progress get() = if (questions.isEmpty()) 0f else (currentIndex + 1f) / questions.size
    val score get() = answers.values.sumOf { it.points }
}