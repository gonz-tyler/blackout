package com.blackout.app.ui.journey

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.blackout.app.domain.model.Habit
import com.blackout.app.ui.habit.HabitViewModel
import com.blackout.app.ui.habit.components.HabitCalendarDialog
import com.blackout.app.ui.habit.components.HabitCompletionButton
import com.blackout.app.ui.habit.components.HabitRepetitionDialog
import com.blackout.app.ui.habit.components.HabitStreakHeatmap
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun JourneyScreen(
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 0.dp,
    onNavigateToHabitCreation: (String?) -> Unit,
    viewModel: HabitViewModel = hiltViewModel()
) {
    val habits by viewModel.habits.collectAsState()
    val quizResults by viewModel.quizResults.collectAsState()
    var selectedDay by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }

    var selectedHabitForModal by remember { mutableStateOf<Habit?>(null) }
    var selectedHabitForRepetition by remember { mutableStateOf<Habit?>(null) }
    var selectedHabitForCalendar by remember { mutableStateOf<Habit?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 16.dp + bottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                WeekMoodTracker(
                    moodHistory = quizResults,
                    selectedDay = selectedDay,
                    onDaySelected = { date -> selectedDay = date }
                )
            }

            if (habits.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No habits yet. Tap + to add one!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                items(habits) { habit ->
                    HabitStreakCard(
                        habit = habit,
                        onCardClick = { selectedHabitForModal = habit },
                        onToggleCompletion = {
                            toggleHabitCompletion(habit, viewModel)
                        }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                onNavigateToHabitCreation(null)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp + bottomPadding)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Habit")
        }

        selectedHabitForModal?.let { habit ->
            HabitDetailsBottomSheet(
                habit = habit,
                onDismissRequest = { selectedHabitForModal = null },
                onEdit = {
                    val habitId = habit.id
                    selectedHabitForModal = null
                    onNavigateToHabitCreation(habitId)
                },
                onDelete = {
                    viewModel.deleteHabit(habit.id)
                    selectedHabitForModal = null
                },
                onOpenCalendar = {
                    selectedHabitForCalendar = habit
                    selectedHabitForModal = null
                },
                onToggleCompletion = {
                    toggleHabitCompletion(habit, viewModel)
                },
                onOpenRepetitionDialog = {
                    selectedHabitForRepetition = habit
                    selectedHabitForModal = null
                }
            )
        }

        selectedHabitForRepetition?.let { habit ->
            HabitRepetitionDialog(
                habit = habit,
                selectedDate = LocalDate.now(),
                onConfirm = { reps ->
                    viewModel.setHabitCompletionForDate(habit.id, LocalDate.now(), reps)
                    selectedHabitForRepetition = null
                },
                onDismiss = { selectedHabitForRepetition = null }
            )
        }

        selectedHabitForCalendar?.let { habit ->
            HabitCalendarDialog(
                habit = habit,
                onCompletionChanged = { date, completions ->
                    viewModel.setHabitCompletionForDate(habit.id, date, completions)
                },
                onDismissRequest = { selectedHabitForCalendar = null }
            )
        }
    }
}

fun toggleHabitCompletion(habit: Habit, viewModel: HabitViewModel) {
    val today = LocalDate.now()
    val currentCount = habit.completedDates[today] ?: 0
    val target = habit.timesPerDay.coerceAtLeast(1)
    val newCount = if (currentCount < target) currentCount + 1 else 0
    viewModel.setHabitCompletionForDate(habit.id, today, newCount)
}

@Composable
fun HabitStreakCard(
    habit: Habit,
    onCardClick: () -> Unit,
    onToggleCompletion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val isDueToday = habit.isDueOn(today)
    val habitColor = Color(habit.color)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 2.dp, color = habitColor.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onCardClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = getHabitIcon(habit.iconName),
                        contentDescription = null,
                        tint = habitColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        habit.getNextDueDate()?.let { nextDue ->
                            Text(
                                text = "Next due: ${formatDueDate(nextDue)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                HabitCompletionButton(
                    habit = habit,
                    onToggle = onToggleCompletion,
                    enabled = isDueToday
                )
            }

            HabitStreakHeatmap(habit = habit)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailsBottomSheet(
    habit: Habit,
    onDismissRequest: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenCalendar: () -> Unit,
    onToggleCompletion: () -> Unit,
    onOpenRepetitionDialog: () -> Unit
) {
    val today = LocalDate.now()
    val isDueToday = habit.isDueOn(today)
    val completionsToday = habit.completedDates[today] ?: 0
    val isTodayCompleted = completionsToday >= habit.timesPerDay
    val habitColor = Color(habit.color)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getHabitIcon(habit.iconName),
                    contentDescription = null,
                    tint = habitColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
                IconButton(onClick = onOpenCalendar) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar")
                }
            }

            if (isDueToday) {
                Button(
                    onClick = {
                        if (habit.timesPerDay > 1 && !isTodayCompleted) {
                            onOpenRepetitionDialog()
                        } else {
                            onToggleCompletion()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTodayCompleted) Color.Gray else habitColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(
                        imageVector = if (isTodayCompleted) Icons.Default.CheckCircle else Icons.Default.AddCircle,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTodayCompleted) "Completed Today ($completionsToday/${habit.timesPerDay})" else "Complete Today ($completionsToday/${habit.timesPerDay})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "This habit is not scheduled for today.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

fun formatDueDate(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "Today"
        today.plusDays(1) -> "Tomorrow"
        else -> date.format(DateTimeFormatter.ofPattern("MMM d"))
    }
}

fun getHabitIcon(iconName: String): ImageVector {
    return when (iconName) {
        "star" -> Icons.Default.Star
        "favorite" -> Icons.Default.Favorite
        "fitness_center" -> Icons.Default.FitnessCenter
        "book" -> Icons.Default.Book
        "music_note" -> Icons.Default.MusicNote
        "brush" -> Icons.Default.Brush
        "nature" -> Icons.Default.Eco
        "pets" -> Icons.Default.Pets
        "fastfood" -> Icons.Default.Restaurant
        "local_florist" -> Icons.Default.LocalFlorist
        else -> Icons.Default.Star
    }
}
