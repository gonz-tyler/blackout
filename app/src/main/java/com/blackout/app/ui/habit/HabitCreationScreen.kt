package com.blackout.app.ui.habit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.blackout.app.R
import com.blackout.app.domain.model.FrequencyType
import com.blackout.app.domain.model.Habit
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

val habitIcons = listOf(
    "star" to Icons.Default.Star,
    "favorite" to Icons.Default.Favorite,
    "fitness_center" to Icons.Default.FitnessCenter,
    "book" to Icons.Default.Book,
    "music_note" to Icons.Default.MusicNote,
    "brush" to Icons.Default.Brush,
    "nature" to Icons.Default.Eco,
    "pets" to Icons.Default.Pets,
    "fastfood" to Icons.Default.Restaurant,
    "local_florist" to Icons.Default.LocalFlorist
)

val habitColors = listOf(
    Color(0xFFF44336),
    Color(0xFFE91E63),
    Color(0xFF9C27B0),
    Color(0xFF673AB7),
    Color(0xFF3F51B5),
    Color(0xFF2196F3),
    Color(0xFF03A9F4),
    Color(0xFF00BCD4),
    Color(0xFF009688),
    Color(0xFF4CAF50),
    Color(0xFF8BC34A),
    Color(0xFFCDDC39),
    Color(0xFFFFEB3B),
    Color(0xFFFFC107),
    Color(0xFFFF9800),
    Color(0xFFFF5722),
    Color(0xFF795548)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCreationScreen(
    habitId: String? = null,
    viewModel: HabitViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val habits by viewModel.habits.collectAsState()
    val habitToEdit = remember(habits, habitId) {
        habitId?.let { id -> habits.find { it.id == id } }
    }

    var name by remember(habitToEdit) { mutableStateOf(habitToEdit?.name ?: "") }
    var desc by remember(habitToEdit) { mutableStateOf(habitToEdit?.desc ?: "") }
    var category by remember(habitToEdit) { mutableStateOf(habitToEdit?.category ?: "") }
    var timesPerDay by remember(habitToEdit) { mutableStateOf(habitToEdit?.timesPerDay ?: 1) }

    var selectedColor by remember(habitToEdit) {
        mutableStateOf(habitToEdit?.color?.let { Color(it) } ?: habitColors[5])
    }
    var selectedIconIndex by remember(habitToEdit) {
        mutableStateOf(
            habitToEdit?.iconName?.let { name -> habitIcons.indexOfFirst { it.first == name }.takeIf { it >= 0 } } ?: 0
        )
    }

    var frequencyType by remember(habitToEdit) { mutableStateOf(habitToEdit?.frequencyType ?: FrequencyType.DAILY) }
    var customIntervalText by remember(habitToEdit) { mutableStateOf(habitToEdit?.customInterval?.toString() ?: "1") }
    var weeklyDays by remember(habitToEdit) {
        mutableStateOf(habitToEdit?.weeklyDays?.takeIf { it.isNotEmpty() } ?: setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))
    }
    var monthlyDays by remember(habitToEdit) {
        mutableStateOf(habitToEdit?.monthlyDays?.takeIf { it.isNotEmpty() } ?: setOf(1))
    }
    var startDate by remember(habitToEdit) { mutableStateOf(habitToEdit?.startDate ?: LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var customError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val isNameBlank = name.trim().isEmpty()

    val habitNameRequired = stringResource(R.string.habit_name_required)
    val habitNameDuplicate = stringResource(R.string.habit_name_duplicate)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = {
                            nameError = null
                            if (isNameBlank) {
                                nameError = habitNameRequired
                                coroutineScope.launch { scrollState.animateScrollTo(0) }
                                return@Button
                            }
                            val duplicate = habits.any { h ->
                                h.name.trim().equals(name.trim(), ignoreCase = true) &&
                                        (habitToEdit == null || h.id != habitToEdit.id)
                            }
                            if (duplicate) {
                                nameError = habitNameDuplicate
                                coroutineScope.launch { scrollState.animateScrollTo(0) }
                                return@Button
                            }

                            val customNum = customIntervalText.toIntOrNull()
                            if (frequencyType == FrequencyType.CUSTOM && (customNum == null || customNum < 1)) {
                                customError = "Must be at least 1"
                                return@Button
                            }
                            val intervalToSave = customNum ?: 1

                            val habit = Habit(
                                id = habitToEdit?.id ?: UUID.randomUUID().toString(),
                                name = name.trim(),
                                desc = desc.trim(),
                                color = selectedColor.toArgb(),
                                iconName = habitIcons[selectedIconIndex].first,
                                timesPerDay = timesPerDay,
                                frequencyType = frequencyType,
                                customInterval = intervalToSave,
                                weeklyDays = weeklyDays,
                                monthlyDays = monthlyDays,
                                startDate = startDate,
                                completedDates = habitToEdit?.completedDates ?: emptyMap(),
                                category = category.trim()
                            )
                            viewModel.saveHabit(habit)
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = if (habitToEdit == null) stringResource(R.string.add) else stringResource(R.string.update),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (habitToEdit == null) stringResource(R.string.create_habit) else stringResource(R.string.edit_habit),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Icon Picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Icon",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(habitIcons) { index, (_, icon) ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedIconIndex == index) selectedColor
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedIconIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (selectedIconIndex == index) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = { Text("Habit Name *") },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description
            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            // Color Picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Color",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(habitColors) { _, color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = color },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == color) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Frequency Type
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Frequency",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(FrequencyType.DAILY, FrequencyType.ALTERNATE, FrequencyType.WEEKLY, FrequencyType.MONTHLY, FrequencyType.CUSTOM).forEach { type ->
                        FilterChip(
                            selected = frequencyType == type,
                            onClick = { frequencyType = type },
                            label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }

            // Conditional Frequency Options
            when (frequencyType) {
                FrequencyType.WEEKLY -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Select Days",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DayOfWeek.entries.forEach { day ->
                                val selected = day in weeklyDays
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        weeklyDays = if (selected) {
                                            if (weeklyDays.size > 1) weeklyDays - day else weeklyDays
                                        } else {
                                            weeklyDays + day
                                        }
                                    },
                                    label = { Text(day.name.take(1)) }
                                )
                            }
                        }
                    }
                }
                FrequencyType.MONTHLY -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Select Days of Month (1-31)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Note: Selecting 29, 30, or 31 triggers on the last day of shorter months (e.g., February).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 1000.dp),
                            userScrollEnabled = false,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(31) { index ->
                                val day = index + 1
                                val selected = day in monthlyDays
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (selected) selectedColor
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable {
                                            monthlyDays = if (selected) {
                                                if (monthlyDays.size > 1) monthlyDays - day else monthlyDays
                                            } else {
                                                monthlyDays + day
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.toString(),
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                FrequencyType.CUSTOM -> {
                    OutlinedTextField(
                        value = customIntervalText,
                        onValueChange = { newVal ->
                            customIntervalText = newVal
                            val num = newVal.toIntOrNull()
                            customError = if (num == null || num < 1) "Must be at least 1" else null
                        },
                        label = { Text("Every X days from start date") },
                        isError = customError != null,
                        supportingText = customError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                else -> {}
            }

            // Start Date
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Start Date",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {
                    OutlinedTextField(
                        value = startDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Start Date") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Times per day
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Times per day",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (timesPerDay > 1) timesPerDay-- },
                        enabled = timesPerDay > 1
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    Text(
                        text = timesPerDay.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { if (timesPerDay < 10) timesPerDay++ },
                        enabled = timesPerDay < 10
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }

        if (showDatePicker) {
            StartDatePickerDialog(
                initialDate = startDate,
                onDateSelected = { startDate = it },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val initialMillis = initialDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        onDateSelected(selectedDate)
                    }
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
