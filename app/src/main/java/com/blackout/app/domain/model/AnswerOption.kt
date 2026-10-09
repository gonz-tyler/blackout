package com.blackout.app.domain.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class AnswerOption(
    val value: Int,                 // stable value you store
    val points: Int,
    @StringRes val label: Int? = null,
    val icon: ImageVector? = null,
)