package com.blackout.app.ui.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blackout.app.data.repository.HabitRepository
import com.blackout.app.data.repository.QuizRepository
import com.blackout.app.domain.model.Habit
import com.blackout.app.domain.service.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val quizRepository: QuizRepository
) : ViewModel() {

    val habits: StateFlow<List<Habit>> = repository.observeHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val quizResults: StateFlow<Map<LocalDate, Int>> = quizRepository.observeResults()
        .map { list -> list.associate { it.date to it.score } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val streakCount: StateFlow<Int> = habits
        .map { list -> StreakCalculator.calculateStreak(list) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val totalCompletions: StateFlow<Int> = habits
        .map { list -> StreakCalculator.calculateTotalCompletions(list) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val dueTodayStats: StateFlow<Pair<Int, Int>> = habits
        .map { list -> StreakCalculator.calculateDueTodayCompletions(list) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0 to 0
        )

    fun saveHabit(habit: Habit) {
        viewModelScope.launch {
            repository.saveHabit(habit)
        }
    }

    fun setHabitCompletionForDate(habitId: String, date: LocalDate, count: Int) {
        viewModelScope.launch {
            repository.setHabitCompletionForDate(habitId, date, count)
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
        }
    }
}
