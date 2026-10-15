package com.blackout.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blackout.app.R
import com.blackout.app.data.datastore.FeatureToggles
import com.blackout.app.data.repository.HabitRepository
import com.blackout.app.domain.service.StreakCalculator
import java.time.LocalDate
import java.time.Year

@Composable
fun ProfileScreen(
    habitRepository: HabitRepository,
    onNavigateToFavoriteQuotes: () -> Unit,
    onNavigateToJournalEntries: () -> Unit,
    onNavigateToSettings: () -> Unit,
    features: FeatureToggles,
    bottomPadding: Dp = 0.dp,
) {
    val habits by habitRepository.observeHabits().collectAsState(initial = emptyList())
    val streakCount = remember(habits) { StreakCalculator.calculateStreak(habits) }
    val totalCompletions = remember(habits) { StreakCalculator.calculateTotalCompletions(habits) }
    val dueToday = remember(habits) { StreakCalculator.calculateDueTodayCompletions(habits) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 16.dp + bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            // User Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.your_progress),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.keep_progressing),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.total_habits),
                            value = habits.size.toString(),
                            icon = Icons.AutoMirrored.Rounded.ListAlt
                        )
                        StatItem(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.completed_due_today),
                            value = "${dueToday.first}/${dueToday.second}",
                            icon = Icons.Rounded.CalendarToday
                        )
                        StatItem(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.total_completions),
                            value = totalCompletions.toString(),
                            icon = Icons.Rounded.CheckCircle
                        )
                    }
                }
            }
        }

        if (features.yearProgress) {
            item {
                val now = remember { LocalDate.now() }
                val dayOfYear = now.dayOfYear
                val totalDaysInYear = remember(now) { Year.of(now.year).length() }
                val progressRatio = dayOfYear.toFloat() / totalDaysInYear
                val percentageFormatted = remember(progressRatio) {
                    "%.1f".format(progressRatio * 100)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.HourglassBottom,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Year Progress ${now.year}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$dayOfYear / $totalDaysInYear days passed ($percentageFormatted%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Live today with intention.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            // Milestones & Achievements
            Text(
                text = "Milestones & Achievements",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MilestoneBadge(
                    modifier = Modifier.weight(1f),
                    title = "First Goal",
                    subtitle = "Complete 1 habit",
                    icon = Icons.Rounded.EmojiEvents,
                    iconColor = Color(0xFFFFCA28),
                    isUnlocked = totalCompletions >= 1
                )
                MilestoneBadge(
                    modifier = Modifier.weight(1f),
                    title = "Streak Master",
                    subtitle = "$streakCount-day streak",
                    icon = Icons.Rounded.LocalFireDepartment,
                    iconColor = Color(0xFFFF7043),
                    isUnlocked = streakCount >= 7
                )
                MilestoneBadge(
                    modifier = Modifier.weight(1f),
                    title = "30+ Done",
                    subtitle = "30 completions",
                    icon = Icons.Rounded.Star,
                    iconColor = Color(0xFFAB47BC),
                    isUnlocked = totalCompletions >= 30
                )
            }
        }

        item {
            // Quick Actions
            Text(
                text = stringResource(R.string.quick_actions),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionCard(
                    title = stringResource(R.string.favorite_quotes),
                    subtitle = stringResource(R.string.view_favorite_quotes),
                    icon = Icons.Rounded.Favorite,
                    iconColor = MaterialTheme.colorScheme.error,
                    onClick = onNavigateToFavoriteQuotes
                )
                ActionCard(
                    title = stringResource(R.string.journal_entries),
                    subtitle = stringResource(R.string.view_journal_entries),
                    icon = Icons.AutoMirrored.Rounded.Article,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    onClick = onNavigateToJournalEntries
                )
                ActionCard(
                    title = stringResource(R.string.settings),
                    subtitle = stringResource(R.string.customize_preferences),
                    icon = Icons.Rounded.Settings,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    onClick = onNavigateToSettings
                )
            }
        }
    }
}

@Composable
private fun MilestoneBadge(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    isUnlocked: Boolean = true
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (isUnlocked) iconColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isUnlocked) iconColor else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
