package com.blackout.app.ui.today

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.blackout.app.R
import com.blackout.app.data.repository.HabitRepository
import com.blackout.app.data.repository.QuotesRepository
import com.blackout.app.data.repository.QuizRepository
import com.blackout.app.domain.model.Quote
import com.blackout.app.ui.habit.components.HabitCompletionButton
import com.blackout.app.ui.journey.getHabitIcon
import com.core.designsystem.theme.AppTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun TodayScreen(
    quotesRepository: QuotesRepository,
    quizRepository: QuizRepository,
    habitRepository: HabitRepository,
    onStartQuiz: () -> Unit,
    languageCode: String = "en",
    modifier: Modifier = Modifier
) {
    var currentQuote by remember { mutableStateOf<Quote?>(null) }
    val favoriteIds by quotesRepository.favoriteQuoteIds.collectAsState(initial = emptySet())
    val enabledTraditions by quotesRepository.enabledWisdomTraditions.collectAsState(initial = emptySet())
    val quizResults by quizRepository.observeResults().collectAsState(initial = emptyList())
    val habits by habitRepository.observeHabits().collectAsState(initial = emptyList())

    val coroutineScope = rememberCoroutineScope()
    val hour = LocalTime.now().hour

    val isQuizCompletedToday = remember(quizResults) {
        val today = LocalDate.now()
        quizResults.any { it.date == today }
    }

    val todayHabits = remember(habits) {
        val today = LocalDate.now()
        habits.filter { it.isDueOn(today) }
    }

    val greeting = stringResource(
        when (hour) {
            in 5..11 -> R.string.morning_greeting      // Morning (05:00 - 11:59)
            in 12..17 -> R.string.afternoon_greeting   // Afternoon (12:00 - 17:59)
            in 18..21 -> R.string.evening_greeting     // Evening (18:00 - 21:59)
            else -> R.string.night_greeting            // Night (22:00 - 04:59)
        }
    )
    val subgreeting = stringResource(
        when (hour) {
            in 5..11 -> R.string.morning_subgreeting     // Morning
            in 12..17 -> R.string.afternoon_subgreeting  // Afternoon
            in 18..21 -> R.string.evening_subgreeting    // Evening
            else -> R.string.night_subgreeting           // Night
        }
    )

    LaunchedEffect(enabledTraditions) {
        currentQuote = quotesRepository.getRandomQuote()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Daily Reflection / Check-in Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppTheme.radiusXL),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subgreeting,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isQuizCompletedToday) {
                        Surface(
                            shape = RoundedCornerShape(AppTheme.radiusS),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Quiz Completed",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = onStartQuiz,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(AppTheme.radiusS),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = "Start Check-in",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Quote Card (Only displayed if a quote matching enabled wisdom traditions is found)
        if (currentQuote != null) {
            item {
                val quote = currentQuote!!
                val isFavorite = favoriteIds.contains(quote.id)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppTheme.radiusXL),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(AppTheme.radiusS),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = quote.category.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            Row {
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        currentQuote = quotesRepository.getRandomQuote()
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = stringResource(R.string.next_quote),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        quotesRepository.toggleFavorite(quote.id)
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = stringResource(R.string.favorite_quote),
                                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = quote.getText(languageCode),
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "— ${quote.author}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Today's Goals / Habits Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppTheme.radiusXL),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Today's Goals",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (todayHabits.isEmpty()) {
                        Text(
                            text = "No habits scheduled for today.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            todayHabits.forEach { habit ->
                                val habitColor = Color(habit.color)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                                            if (habit.desc.isNotBlank()) {
                                                Text(
                                                    text = habit.desc,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }
                                    }

                                    HabitCompletionButton(
                                        habit = habit,
                                        onToggle = {
                                            val today = LocalDate.now()
                                            val currentCount = habit.completedDates[today] ?: 0
                                            val target = habit.timesPerDay.coerceAtLeast(1)
                                            val newCount = if (currentCount < target) currentCount + 1 else 0
                                            coroutineScope.launch {
                                                habitRepository.setHabitCompletionForDate(habit.id, today, newCount)
                                            }
                                        },
                                        enabled = true
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
