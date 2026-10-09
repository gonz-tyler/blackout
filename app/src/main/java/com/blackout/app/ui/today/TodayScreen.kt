package com.blackout.app.ui.today

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.blackout.app.R
import com.blackout.app.data.repository.QuotesRepository
import com.blackout.app.domain.model.Quote
import com.core.designsystem.theme.AppTheme
import kotlinx.coroutines.launch
import java.time.LocalTime

@Composable
fun TodayScreen(
    quotesRepository: QuotesRepository,
    onStartQuiz: () -> Unit,
    languageCode: String = "en",
    modifier: Modifier = Modifier
) {
    var currentQuote by remember { mutableStateOf<Quote?>(null) }
    val favoriteIds by quotesRepository.favoriteQuoteIds.collectAsState(initial = emptySet())
    val coroutineScope = rememberCoroutineScope()
    val hour = LocalTime.now().hour   // 0..23

    val greeting = stringResource (
        when (hour) {
            in 4..11 -> R.string.morning_greeting      // 04:00 to 11:59
            in 12..19 -> R.string.afternoon_greeting   // 12:00 to 19:59
            else -> R.string.evening_greeting                // 20:00 to 03:59
        }
    )
    val subgreeting = stringResource(
        when (hour) {
            in 4..11 -> R.string.morning_subgreeting     // 04:00 to 11:59
            in 12..19 -> R.string.afternoon_subgreeting  // 12:00 to 19:59
            else -> R.string.evening_subgreeting               // 20:00 to 03:59
        }
    )

    // Pick a new random quote each time the screen enters composition
    LaunchedEffect(Unit) {
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

        // Quote Card
        item {
            val quote = currentQuote
            if (quote != null) {
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
                                    currentQuote = quotesRepository.getRandomQuote()
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
            } else {
                Text(
                    text = stringResource(R.string.no_quotes_available),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
