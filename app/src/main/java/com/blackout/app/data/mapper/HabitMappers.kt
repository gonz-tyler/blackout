package com.blackout.app.data.mapper

import com.blackout.app.data.local.entity.HabitCompletionEntity
import com.blackout.app.data.local.entity.HabitEntity
import com.blackout.app.data.local.entity.HabitWithCompletions
import com.blackout.app.domain.model.Habit

fun HabitWithCompletions.toDomain() = Habit(
    id = habit.id,
    name = habit.name,
    desc = habit.desc,
    color = habit.color,
    iconName = habit.iconName,
    timesPerDay = habit.timesPerDay,
    frequencyType = habit.frequencyType,
    customInterval = habit.customInterval,
    weeklyDays = habit.weeklyDays,
    monthlyDays = habit.monthlyDays,
    startDate = habit.startDate,
    completedDates = completions.associate { it.date to it.count },
    category = habit.category,
)

fun Habit.toEntity() = HabitEntity(
    id = id,
    name = name,
    desc = desc,
    color = color,
    iconName = iconName,
    timesPerDay = timesPerDay,
    frequencyType = frequencyType,
    customInterval = customInterval,
    weeklyDays = weeklyDays,
    monthlyDays = monthlyDays,
    startDate = startDate,
    category = category,
    updatedAt = System.currentTimeMillis(),
)

fun Habit.toCompletionEntities() = completedDates.map { (date, count) ->
    HabitCompletionEntity(habitId = id, date = date, count = count)
}
