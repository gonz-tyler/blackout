package com.blackout.app.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class FrequencyType { DAILY, ALTERNATE, WEEKLY, MONTHLY, CUSTOM }

data class Habit(
    val id: String,
    val name: String,
    val desc: String,
    val color: Int,                       // ARGB; use Color(color) in Compose
    val iconName: String,                 // or iconCodePoint: Int, see note below
    val timesPerDay: Int = 1,
    val frequencyType: FrequencyType = FrequencyType.DAILY,
    val customInterval: Int = 1,
    val weeklyDays: Set<DayOfWeek> = emptySet(),
    val monthlyDays: Set<Int> = emptySet(),
    val startDate: LocalDate = LocalDate.now(),
    val completedDates: Map<LocalDate, Int> = emptyMap(),
    val category: String = "",
) {
    fun isDueOn(date: LocalDate): Boolean {
        if (date.isBefore(startDate)) return false

        val daysSinceStart = ChronoUnit.DAYS.between(startDate, date)

        return when (frequencyType) {
            FrequencyType.DAILY -> true
            FrequencyType.ALTERNATE -> daysSinceStart % 2 == 0L
            FrequencyType.WEEKLY -> date.dayOfWeek in weeklyDays
            FrequencyType.MONTHLY -> {
                val lengthOfMonth = date.lengthOfMonth()
                monthlyDays.any { d ->
                    date.dayOfMonth == d || (d >= lengthOfMonth && date.dayOfMonth == lengthOfMonth)
                }
            }
            FrequencyType.CUSTOM -> daysSinceStart % customInterval.coerceAtLeast(1) == 0L
        }
    }

    fun getNextDueDate(from: LocalDate = LocalDate.now()): LocalDate? {
        var checkDate = if (from.isBefore(startDate)) startDate else from
        for (i in 0..365) {
            val isCompleted = (completedDates[checkDate] ?: 0) >= timesPerDay.coerceAtLeast(1)
            if (isDueOn(checkDate) && !isCompleted) {
                return checkDate
            }
            checkDate = checkDate.plusDays(1)
        }
        return null
    }
}
