package com.blackout.app.domain.model

import androidx.annotation.StringRes

sealed interface Question {
    val id: String
    @get:StringRes
    val text: Int

    data class Choice(
        override val id: String,
        @StringRes override val text: Int,
        val options: List<AnswerOption>,
    ) : Question

    data class FreeText(
        override val id: String,
        @StringRes override val text: Int,
    ) : Question
}