package com.blackout.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey val date: LocalDate,
    val score: Int,
)