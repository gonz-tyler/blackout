package com.blackout.app.ui.habit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.blackout.app.domain.model.Habit
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import com.blackout.app.R
import com.blackout.app.ui.journey.getHabitIcon

@Composable
fun HabitCalendarDialog(
    habit: Habit,
    onCompletionChanged: (LocalDate, Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    val today = LocalDate.now()
    val normalizedStartDate = habit.startDate

    var localCompletions by remember(habit.completedDates) {
        mutableStateOf(habit.completedDates)
    }

    var currentMonth by remember {
        val initial = if (today.isBefore(normalizedStartDate)) normalizedStartDate else today
        mutableStateOf(YearMonth.from(initial))
    }

    val startMonth = YearMonth.from(normalizedStartDate)
    val nowMonth = YearMonth.from(today)

    val isPrevDisabled = currentMonth <= startMonth
    val isNextDisabled = currentMonth >= nowMonth

    val habitColor = Color(habit.color)

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.75f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = getHabitIcon(habit.iconName),
                        contentDescription = null,
                        tint = habitColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                    }
                }

                if (habit.desc.isNotBlank()) {
                    Text(
                        text = habit.desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Month Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { currentMonth = currentMonth.minusMonths(1) },
                        enabled = !isPrevDisabled
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = stringResource(R.string.previous_month))
                    }

                    Text(
                        text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { currentMonth = currentMonth.plusMonths(1) },
                        enabled = !isNextDisabled
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = stringResource(R.string.next_month))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Weekday Labels
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf(stringResource(R.string.monday_initial), stringResource(R.string.tuesday_initial), stringResource(R.string.wednesday_initial), stringResource(R.string.thursday_initial), stringResource(R.string.friday_initial), stringResource(R.string.saturday_initial), stringResource(R.string.sunday_initial)).forEach { dayLabel ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 42 Days Grid
                val daysInGrid = remember(currentMonth) { getDaysInGrid(currentMonth) }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(daysInGrid) { day ->
                        val isCurrentMonth = day.month == currentMonth.month
                        val isFuture = day.isAfter(today)
                        val isBeforeStart = day.isBefore(normalizedStartDate)
                        val isDue = habit.isDueOn(day)
                        val isClickable = isCurrentMonth && !isFuture && !isBeforeStart && isDue

                        val completions = localCompletions[day] ?: 0
                        val maxCompletions = habit.timesPerDay.coerceAtLeast(1)
                        val isToday = day == today

                        val textColor = when {
                            !isCurrentMonth || isBeforeStart || isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            !isDue -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            isToday -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        val containerBg = if (isToday && isDue) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        } else null

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .then(
                                    if (containerBg != null) Modifier.background(containerBg) else Modifier
                                )
                                .then(
                                    if (completions >= maxCompletions && completions > 0) {
                                        Modifier.border(2.dp, habitColor, RoundedCornerShape(8.dp))
                                    } else Modifier
                                )
                                .clickable(enabled = isClickable) {
                                    val nextCompletions = if (completions >= maxCompletions) 0 else completions + 1
                                    localCompletions = localCompletions.toMutableMap().apply {
                                        if (nextCompletions > 0) {
                                            this[day] = nextCompletions
                                        } else {
                                            remove(day)
                                        }
                                    }
                                    onCompletionChanged(day, nextCompletions)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = day.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = textColor
                                )

                                if (isClickable && completions > 0) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (completions <= 4) {
                                            repeat(completions) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(habitColor)
                                                )
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(habitColor)
                                            )
                                            Text(
                                                text = "$completions",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = habitColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Legend
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(habitColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.tap_valid_past_dates, habit.timesPerDay),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

fun getDaysInGrid(month: YearMonth): List<LocalDate> {
    val firstDayOfMonth = month.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // Mon=1, Sun=7
    val startDay = firstDayOfMonth.minusDays((firstDayOfWeek - 1).toLong())
    return (0 until 42).map { startDay.plusDays(it.toLong()) }
}
