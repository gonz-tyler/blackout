package com.blackout.app.domain.service

import com.blackout.app.domain.model.Habit
import java.time.LocalDate

object StreakCalculator {
    fun calculateStreak(habits: List<Habit>, today: LocalDate = LocalDate.now()): Int {
        if (habits.isEmpty()) return 0

        var streak = 0
        var checkDate = today

        val dueToday = habits.filter { it.isDueOn(today) }
        val completedToday = dueToday.isNotEmpty() && dueToday.all { habit ->
            (habit.completedDates[today] ?: 0) >= habit.timesPerDay.coerceAtLeast(1)
        }

        if (completedToday) {
            streak++
            checkDate = today.minusDays(1)
        } else {
            checkDate = today.minusDays(1)
        }

        for (i in 0..365) {
            val dueOnDate = habits.filter { it.isDueOn(checkDate) }
            if (dueOnDate.isEmpty()) {
                checkDate = checkDate.minusDays(1)
                continue
            }

            val allCompleted = dueOnDate.all { habit ->
                (habit.completedDates[checkDate] ?: 0) >= habit.timesPerDay.coerceAtLeast(1)
            }

            if (allCompleted) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }

        return streak
    }

    fun calculateTotalCompletions(habits: List<Habit>): Int {
        return habits.sumOf { habit -> habit.completedDates.values.sum() }
    }

    fun calculateDueTodayCompletions(habits: List<Habit>, today: LocalDate = LocalDate.now()): Pair<Int, Int> {
        val dueToday = habits.filter { it.isDueOn(today) }
        if (dueToday.isEmpty()) return 0 to 0

        val completed = dueToday.count { habit ->
            (habit.completedDates[today] ?: 0) >= habit.timesPerDay.coerceAtLeast(1)
        }
        return completed to dueToday.size
    }
}
