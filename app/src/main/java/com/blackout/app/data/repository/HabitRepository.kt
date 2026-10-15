package com.blackout.app.data.repository

import com.blackout.app.data.local.dao.HabitDao
import com.blackout.app.data.local.entity.HabitCompletionEntity
import com.blackout.app.data.mapper.toCompletionEntities
import com.blackout.app.data.mapper.toDomain
import com.blackout.app.data.mapper.toEntity
import com.blackout.app.domain.model.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

interface HabitRepository {
    fun observeHabits(): Flow<List<Habit>>
    suspend fun saveHabit(habit: Habit)
    suspend fun setHabitCompletionForDate(habitId: String, date: LocalDate, count: Int)
    suspend fun deleteHabit(habitId: String)
}

class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao
) : HabitRepository {
    override fun observeHabits(): Flow<List<Habit>> {
        return habitDao.observeHabitsWithCompletions().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveHabit(habit: Habit) {
        habitDao.upsertHabit(habit.toEntity())
        habitDao.deleteCompletionsForHabit(habit.id)
        val completions = habit.toCompletionEntities()
        if (completions.isNotEmpty()) {
            habitDao.insertCompletions(completions)
        }
    }

    override suspend fun setHabitCompletionForDate(habitId: String, date: LocalDate, count: Int) {
        if (count > 0) {
            habitDao.insertCompletions(listOf(HabitCompletionEntity(habitId = habitId, date = date, count = count)))
        } else {
            habitDao.deleteCompletionForDate(habitId, date)
        }
        habitDao.touchHabit(habitId, System.currentTimeMillis())
    }

    override suspend fun deleteHabit(habitId: String) {
        habitDao.deleteCompletionsForHabit(habitId)
        habitDao.deleteHabit(habitId)
    }
}
