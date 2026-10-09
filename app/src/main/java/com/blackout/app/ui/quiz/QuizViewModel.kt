package com.blackout.app.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blackout.app.data.repository.QuizRepository
import com.blackout.app.domain.model.Answer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repo: QuizRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(QuizUIState(questions = repo.getQuestions()))
    val state = _state.asStateFlow()

    fun onAnswer(answer: Answer) = _state.update {
        val q = it.questions[it.currentIndex]
        it.copy(answers = it.answers + (q.id to answer))
    }

    fun onNext() {
        val s = _state.value
        if (s.currentIndex < s.questions.lastIndex) {
            _state.update { it.copy(currentIndex = it.currentIndex + 1) }
        } else finish()
    }

    fun onBack() = _state.update { it.copy(currentIndex = (it.currentIndex - 1).coerceAtLeast(0)) }

    private fun finish() = viewModelScope.launch {
        repo.saveResult(LocalDate.now(), state.value.answers)
        _state.update { it.copy(isFinished = true) }
    }
}