package com.blackout.app.ui.habit.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.blackout.app.domain.model.Habit
import java.time.LocalDate

@Composable
fun HabitCompletionButton(
    habit: Habit,
    onToggle: () -> Unit,
    enabled: Boolean = true
) {
    val today = LocalDate.now()
    val currentCount = habit.completedDates[today] ?: 0
    val target = habit.timesPerDay.coerceAtLeast(1)
    val targetFloat = currentCount.toFloat() / target

    val animProgress = remember { Animatable(targetFloat) }

    LaunchedEffect(currentCount, target) {
        val newTarget = currentCount.toFloat() / target
        if (animProgress.value >= 0.99f && newTarget == 0f) {
            animProgress.animateTo(2f, animationSpec = tween(durationMillis = 300))
            animProgress.snapTo(0f)
        } else {
            animProgress.animateTo(newTarget, animationSpec = tween(durationMillis = 300))
        }
    }

    val progressValue = animProgress.value
    val isCompleted = currentCount >= target

    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(enabled = enabled, onClick = onToggle),
        contentAlignment = Alignment.Center
    ) {
        val trackColor = if (enabled) MaterialTheme.colorScheme.surfaceVariant else Color.LightGray.copy(alpha = 0.3f)
        val primaryColor = if (enabled) Color(habit.color) else Color.Gray

        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            if (progressValue > 0f) {
                val startAngle: Float
                val sweepAngle: Float

                if (progressValue <= 1f) {
                    startAngle = -90f
                    sweepAngle = progressValue * 360f
                } else {
                    val p = progressValue - 1f
                    startAngle = -90f + (p * 360f)
                    sweepAngle = (1f - p) * 360f
                }

                drawArc(
                    color = primaryColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        if (isCompleted && progressValue <= 1f) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Completed",
                tint = primaryColor,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Increment",
                tint = primaryColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
