package com.blackout.app.domain.service

import java.time.LocalDate

data class GoalChange(val effectiveFrom: LocalDate, val goal: Int) // effectiveFrom is always a Monday

data class StreakState(
    val startDate: LocalDate? = null,
    val brokenThrough: LocalDate? = null,   // last day of the most recent failed week
    val goalHistory: List<GoalChange> = emptyList()
)