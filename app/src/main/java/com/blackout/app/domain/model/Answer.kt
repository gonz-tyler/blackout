package com.blackout.app.domain.model

sealed interface Answer {
    val points: Int
    data class Choice(val value: Int, override val points: Int) : Answer
    data class Text(val text: String) : Answer { override val points = 0 }
}