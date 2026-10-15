package com.blackout.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.blackout.app.domain.model.FrequencyType
import java.time.DayOfWeek
import java.time.LocalDate

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val desc: String,
    val color: Int,
    val iconName: String,
    val timesPerDay: Int,
    val frequencyType: FrequencyType,
    val customInterval: Int,
    val weeklyDays: Set<DayOfWeek>,
    val monthlyDays: Set<Int>,
    val startDate: LocalDate,
    val category: String,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "habit_completions",
    primaryKeys = ["habitId", "date"],
    foreignKeys = [ForeignKey(
        entity = HabitEntity::class,
        parentColumns = ["id"],
        childColumns = ["habitId"],
        onDelete = ForeignKey.CASCADE,
    )],
)
data class HabitCompletionEntity(
    val habitId: String,
    val date: LocalDate,
    val count: Int,
)

data class HabitWithCompletions(
    @Embedded val habit: HabitEntity,
    @Relation(parentColumn = "id", entityColumn = "habitId")
    val completions: List<HabitCompletionEntity>,
)
