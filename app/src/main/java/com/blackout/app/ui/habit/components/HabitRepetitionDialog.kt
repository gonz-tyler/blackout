package com.blackout.app.ui.habit.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blackout.app.domain.model.Habit
import java.time.LocalDate
import com.blackout.app.R

@Composable
fun HabitRepetitionDialog(
    habit: Habit,
    selectedDate: LocalDate = LocalDate.now(),
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val currentReps = habit.completedDates[selectedDate] ?: 0
    var selectedReps by remember { mutableStateOf(currentReps) }
    val maxReps = habit.timesPerDay.coerceAtLeast(1)
    val habitColor = Color(habit.color)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
//                text = "Complete ${habit.name}",
                text = stringResource(R.string.complete_habit, habit.name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
//                    text = "How many times did you complete this habit on ${selectedDate.dayOfMonth}/${selectedDate.monthValue}/${selectedDate.year}?",
                    text = stringResource(R.string.how_many_completions_date, selectedDate.dayOfMonth, selectedDate.monthValue, selectedDate.year),
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { if (selectedReps > 0) selectedReps-- },
                        enabled = selectedReps > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveCircleOutline,
                            contentDescription = stringResource(R.string.decrease),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "$selectedReps",
                        style = MaterialTheme.typography.headlineLarge,
                        color = habitColor,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { if (selectedReps < maxReps) selectedReps++ },
                        enabled = selectedReps < maxReps
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = stringResource(R.string.increase),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Text(
                    text = "$selectedReps / $maxReps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedReps >= maxReps) Color(0xFF4CAF50) else habitColor
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedReps)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = habitColor, contentColor = Color.White)
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
