package com.blackout.app.ui.habit.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blackout.app.domain.model.Habit
import java.time.LocalDate

@Composable
fun HabitStreakHeatmap(
    habit: Habit,
    modifier: Modifier = Modifier,
    bubbleSize: Dp = 9.dp,
    bubbleSpacing: Dp = 2.dp,
    futureWeeks: Long = 60L,
    today: LocalDate = LocalDate.now()
) {
    val density = LocalDensity.current
    val weeks = remember(habit.startDate, today, futureWeeks) {
        generateWeeks(habit.startDate, today, futureWeeks)
    }

    val bubbleSizePx = with(density) { bubbleSize.toPx() }
    val bubbleSpacingPx = with(density) { bubbleSpacing.toPx() }
    val cellStepPx = bubbleSizePx + bubbleSpacingPx

    val gridHeight = bubbleSize * 7 + bubbleSpacing * 6
    val gridWidthPx = cellStepPx * weeks.size

    val scrollState = rememberScrollState()

    LaunchedEffect(weeks) {
        if (weeks.isNotEmpty()) {
            val targetWeekIndex = weeks.indexOfFirst { week -> week.any { it == today } }.takeIf { it >= 0 } ?: 0
            val targetPx = targetWeekIndex * cellStepPx
            scrollState.scrollTo(targetPx.toInt())
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(gridHeight)
            .horizontalScroll(scrollState)
    ) {
        val habitColor = Color(habit.color)
        Canvas(
            modifier = Modifier
                .width(with(density) { gridWidthPx.toDp() })
                .height(gridHeight)
        ) {
            for (weekIdx in weeks.indices) {
                val week = weeks[weekIdx]
                for (dayIdx in 0 until 7) {
                    val day = week[dayIdx] ?: continue
                    val x = weekIdx * cellStepPx
                    val y = dayIdx * cellStepPx

                    val isToday = day == today
                    val isFuture = day.isAfter(today)
                    val isBeforeStartDate = day.isBefore(habit.startDate)

                    val bubbleColor = when {
                        isBeforeStartDate -> Color.Gray.copy(alpha = 0.05f)
                        isFuture -> Color.Gray.copy(alpha = 0.2f)
                        else -> {
                            val completions = habit.completedDates[day] ?: 0
                            if (completions == 0) {
                                habitColor.copy(alpha = 0.15f)
                            } else {
                                val ratio = (completions.toFloat() / habit.timesPerDay.coerceAtLeast(1)).coerceIn(0f, 1f)
                                val opacity = (0.3f + 0.7f * ratio).coerceIn(0.3f, 1.0f)
                                habitColor.copy(alpha = opacity)
                            }
                        }
                    }

                    drawRoundRect(
                        color = bubbleColor,
                        topLeft = Offset(x, y),
                        size = Size(bubbleSizePx, bubbleSizePx),
                        cornerRadius = CornerRadius(2.dp.toPx())
                    )

                    if (isToday) {
                        drawRoundRect(
                            color = habitColor,
                            topLeft = Offset(x, y),
                            size = Size(bubbleSizePx, bubbleSizePx),
                            cornerRadius = CornerRadius(2.dp.toPx()),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

fun generateWeeks(startDate: LocalDate, today: LocalDate = LocalDate.now(), futureWeeks: Long = 60L): List<List<LocalDate?>> {
    val endDate = today.plusWeeks(futureWeeks)
    if (startDate.isAfter(endDate)) return emptyList()

    var curr = startDate
    val days = mutableListOf<LocalDate>()
    while (!curr.isAfter(endDate)) {
        days.add(curr)
        curr = curr.plusDays(1)
    }
    if (days.isEmpty()) return emptyList()

    val result = mutableListOf<List<LocalDate?>>()
    var currentWeek = MutableList<LocalDate?>(7) { null }

    val firstDayIndex = days.first().dayOfWeek.value - 1
    currentWeek[firstDayIndex] = days.first()

    for (i in 1 until days.size) {
        val dayIndex = days[i].dayOfWeek.value - 1
        val prevDayIndex = days[i - 1].dayOfWeek.value - 1

        if (dayIndex < prevDayIndex) {
            result.add(currentWeek)
            currentWeek = MutableList(7) { null }
        }
        currentWeek[dayIndex] = days[i]
    }
    result.add(currentWeek)
    return result
}
