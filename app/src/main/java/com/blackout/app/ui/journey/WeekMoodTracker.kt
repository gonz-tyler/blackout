package com.blackout.app.ui.journey

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blackout.app.R
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun WeekMoodTracker(
    moodHistory: Map<LocalDate, Int>,
    selectedDay: LocalDate?,
    onDaySelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val firstMonday = remember { LocalDate.of(2020, 1, 6) }
    val currentMonday = remember(today) { today.minusDays((today.dayOfWeek.value - 1).toLong()) }
    val totalWeeks = remember(firstMonday, currentMonday) {
        (ChronoUnit.WEEKS.between(firstMonday, currentMonday).toInt() + 1).coerceAtLeast(1)
    }

    val pagerState = rememberPagerState(
        initialPage = totalWeeks - 1,
        pageCount = { totalWeeks }
    )
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            // Header Row with Navigation Buttons and Week Date Range
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (pagerState.currentPage > 0) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    },
                    enabled = pagerState.currentPage > 0
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = stringResource(R.string.previous_week)
                    )
                }

                val currentWeekMonday = remember(pagerState.currentPage) {
                    firstMonday.plusWeeks(pagerState.currentPage.toLong())
                }
                val currentWeekSunday = remember(currentWeekMonday) {
                    currentWeekMonday.plusDays(6)
                }

                Text(
                    text = "${currentWeekMonday.format(DateTimeFormatter.ofPattern("MMM d"))} - ${currentWeekSunday.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = {
                        if (pagerState.currentPage < totalWeeks - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    enabled = pagerState.currentPage < totalWeeks - 1
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = stringResource(R.string.next_week)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Weekday Headers Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    stringResource(R.string.monday_initial),
                    stringResource(R.string.tuesday_initial),
                    stringResource(R.string.wednesday_initial),
                    stringResource(R.string.thursday_initial),
                    stringResource(R.string.friday_initial),
                    stringResource(R.string.saturday_initial),
                    stringResource(R.string.sunday_initial)
                ).forEachIndexed { idx, initial ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (idx == 6) Color(0xFFB71C1C) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal Pager for Weeks
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val targetMonday = remember(page) { firstMonday.plusWeeks(page.toLong()) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(7) { dayIdx ->
                        val date = targetMonday.plusDays(dayIdx.toLong())
                        val moodScore = moodHistory[date]
                        val isFutureDay = date.isAfter(today)
                        val isSelected = selectedDay == date

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = !isFutureDay) {
                                    onDaySelected(date)
                                }
                                .padding(vertical = 4.dp),
                        ) {
                            when {
                                moodScore == null -> {
                                    Icon(
                                        imageVector = Icons.Outlined.Circle,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isFutureDay) 0.3f else 0.6f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                moodScore < 5 -> {
                                    Icon(
                                        painter = painterResource(id = R.drawable.sad),
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFFEF5350) else Color(0xFFEF5350).copy(alpha = 0.6f), // Green
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                moodScore < 12 -> {
                                    Icon(
                                        painter = painterResource(id = R.drawable.neutral),
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFFFFCA28) else Color(0xFFFFCA28).copy(alpha = 0.6f), // Green
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                else -> {
                                    Icon(
                                        painter = painterResource(id = R.drawable.happy),
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFF4CAF50) else Color(0xFF4CAF50).copy(alpha = 0.6f), // Green
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isFutureDay) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (date.dayOfMonth == 1) {
                                    Text(
                                        text = "/${date.monthValue.toString().padStart(2, '0')}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
