package com.blackout.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.blackout.app.data.local.entity.HabitCompletionEntity
import com.blackout.app.data.local.entity.HabitEntity
import com.blackout.app.data.local.entity.HabitWithCompletions
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HabitDao {
    @Transaction
    @Query("SELECT * FROM habits")
    fun observeHabitsWithCompletions(): Flow<List<HabitWithCompletions>>

    @Upsert
    suspend fun upsertHabit(habit: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletions(completions: List<HabitCompletionEntity>)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId")
    suspend fun deleteCompletionsForHabit(habitId: String)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun deleteCompletionForDate(habitId: String, date: LocalDate)

    @Query("UPDATE habits SET updatedAt = :timestamp WHERE id = :habitId")
    suspend fun touchHabit(habitId: String, timestamp: Long)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabit(habitId: String)
}
