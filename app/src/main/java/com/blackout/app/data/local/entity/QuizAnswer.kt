package com.blackout.app.data.local.entity

import androidx.room.Entity
import java.time.LocalDate

@Entity(
    tableName = "quiz_answers",
    primaryKeys = ["date", "questionId"],   // re-taking the quiz replaces answers
)
data class QuizAnswerEntity(
    val date: LocalDate,
    val questionId: String,
    val value: Int,
    val points: Int,
)