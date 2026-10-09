package com.blackout.app.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Battery0Bar
import androidx.compose.material.icons.filled.Battery1Bar
import androidx.compose.material.icons.filled.Battery3Bar
import androidx.compose.material.icons.filled.Battery4Bar
import androidx.compose.material.icons.filled.Battery6Bar
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import com.blackout.app.R
import com.blackout.app.domain.model.AnswerOption
import com.blackout.app.domain.model.Question

object QuestionBank {
    val questions: List<Question> = listOf(
        Question.Choice(
            id = "feel_today", text = R.string.q_feel_today,
            options = listOf(
                AnswerOption(value = 1, points = 1, icon = Icons.Filled.SentimentVeryDissatisfied),
                AnswerOption(value = 2, points = 2, icon = Icons.Filled.SentimentDissatisfied),
                AnswerOption(value = 3, points = 3, icon = Icons.Filled.SentimentNeutral),
                AnswerOption(value = 4, points = 4, icon = Icons.Filled.SentimentSatisfied),
                AnswerOption(value = 5, points = 5, icon = Icons.Filled.SentimentVerySatisfied),
            ),
        ),
        Question.Choice(
            id = "rested", text = R.string.q_rested,
            options = listOf(
                AnswerOption(value = 1, points = 1, icon = Icons.Filled.Battery0Bar),
                AnswerOption(value = 2, points = 2, icon = Icons.Filled.Battery1Bar),
                AnswerOption(value = 3, points = 3, icon = Icons.Filled.Battery4Bar),
                AnswerOption(value = 4, points = 4, icon = Icons.Filled.Battery6Bar),
                AnswerOption(value = 5, points = 5, icon = Icons.Filled.BatteryFull),
            ),
        ),
        Question.FreeText(id = "focus", text = R.string.q_focus),
        Question.Choice(
            id = "motivated", text = R.string.q_motivated,
            options = listOf(
                AnswerOption(value = 1, points = 1, icon = Icons.Filled.ArrowDownward),
                AnswerOption(value = 2, points = 2, icon = Icons.Filled.Battery1Bar),
                AnswerOption(value = 3, points = 3, icon = Icons.Filled.Battery4Bar),
                AnswerOption(value = 4, points = 4, icon = Icons.Filled.Battery6Bar),
                AnswerOption(value = 5, points = 5, icon = Icons.Filled.BatteryFull),
            ),
        ),
        Question.FreeText(id = "important_task", text = R.string.q_important_task),
        Question.FreeText(id = "gratitude", text = R.string.q_gratitude),
        Question.Choice(
            id = "prepared", text = R.string.q_prepared,
            options = listOf(
                AnswerOption(value = 1, points = 0, label = R.string.a_prepared),
                AnswerOption(value = 0, points = 0, label = R.string.a_not_prepared),
            ),
        ),

    )
}