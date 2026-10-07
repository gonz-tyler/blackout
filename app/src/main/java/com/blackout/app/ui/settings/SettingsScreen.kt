package com.blackout.app.ui.settings

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.blackout.app.data.datastore.FeatureToggles
import com.blackout.app.data.datastore.SettingsDataStore
import com.blackout.app.R
import com.blackout.app.domain.notification.WorkManagerScheduler
import com.blackout.app.ui.components.ModernTimePickerDialog
import kotlinx.coroutines.launch

data class ChoiceOption<T>(val value: T, val label: String)

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settingsDataStore: SettingsDataStore,
    activityContext: Context,
    onNavigateBack: () -> Unit,
    features: FeatureToggles
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val themeMode by settingsDataStore.themeMode.collectAsState(initial = "system")
    val streakEnabled by settingsDataStore.streakEnabled.collectAsState(initial = true)
    val language by settingsDataStore.language.collectAsState(initial = "en")
    val seedColorInt by settingsDataStore.seedColor.collectAsState(initial = 0xFFC9A24B.toInt())
    val reminderTime by settingsDataStore.reminderTime.collectAsState(initial = "09:00")
    val notificationsEnabled by settingsDataStore.notificationsEnabled.collectAsState(initial = true)

    var showTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_description)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // --- Language & Preferences -
            item {
                SettingsSection(
                    title = stringResource(R.string.language),
                    icon = Icons.Rounded.Language
                ) {
                    SettingsContainer {
                        Column {
                            RadioOption(
                                label = stringResource(R.string.system_default),
                                isSelected = language == "system",
                                onClick = {
                                    coroutineScope.launch {
                                        settingsDataStore.setLanguage("system")
                                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                                    }
                                }
                            )
                            HorizontalDivider()
                            RadioOption(
                                label = stringResource(R.string.english),
                                isSelected = language == "en",
                                onClick = {
                                    coroutineScope.launch {
                                        settingsDataStore.setLanguage("en")
                                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                                    }
                                }
                            )
                            HorizontalDivider()
                            RadioOption(
                                label = stringResource(R.string.spanish),
                                isSelected = language == "es",
                                onClick = {
                                    coroutineScope.launch {
                                        settingsDataStore.setLanguage("es")
                                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es"))
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // --- Appearance ---
            item {
                SettingsSection(
                    title = stringResource(R.string.appearance),
                    icon = Icons.Rounded.Palette
                ) {
                    SettingsContainer {
                        SegmentedSettingRow(
                            title = "",
                            options = listOf(
                                ChoiceOption("system", stringResource(R.string.system_default)),
                                ChoiceOption("light", stringResource(R.string.light_mode)),
                                ChoiceOption("dark", stringResource(R.string.dark_mode))
                            ),
                            selected = themeMode,
                            onSelect = { mode ->
                                coroutineScope.launch { settingsDataStore.setThemeMode(mode) }
                            }
                        )
                    }
                }
            }

            // --- Color Theme ---
            item {
                SettingsSection(
                    title = stringResource(R.string.theme),
                    icon = Icons.Rounded.Brush
                ) {
                    SettingsContainer {
                        Column(modifier = Modifier.padding(16.dp)) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val colorSwatches = listOf(
                                    "Gold" to 0xFFC9A24B,
                                    "Rose Gold" to 0xFFE8B4B8,
                                    "Crimson" to 0xFFB71C1C,
                                    "Red" to 0xFFF44336,
                                    "Pink" to 0xFFE91E63,
                                    "Purple" to 0xFF9C27B0,
                                    "Violet" to 0xFF673AB7,
                                    "Indigo" to 0xFF3F51B5,
                                    "Blue" to 0xFF2196F3,
                                    "LightBlue" to 0xFF03A9F4,
                                    "Cyan" to 0xFF00BCD4,
                                    "Teal" to 0xFF009688,
                                    "Green" to 0xFF4CAF50,
                                    "LightGreen" to 0xFF8BC34A,
                                    "Lime" to 0xFFCDDC39,
                                    "Amber" to 0xFFFFC107,
                                    "Orange" to 0xFFFF9800,
                                    "Brown" to 0xFF795548
                                )
                                colorSwatches.forEach { (_, argb) ->
                                    ColorChip(
                                        color = Color(argb),
                                        isSelected = seedColorInt == argb.toInt(),
                                        onClick = {
                                            coroutineScope.launch {
                                                settingsDataStore.setSeedColor(argb.toInt())
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- Features & Modules ---
            item {
                SettingsSection(
                    title = stringResource(R.string.features),
                    icon = Icons.Rounded.Extension
                ) {
                    SettingsContainer {
                        Column {
                            ToggleOption(
                                title = stringResource(R.string.streak),
                                subtitle = stringResource(R.string.streak_description),
                                checked = streakEnabled,
                                onCheckedChange = { coroutineScope.launch { settingsDataStore.setStreakEnabled(it) } }
                            )
                        }
                    }
                }
            }

            // --- Notifications ---
            item {
                SettingsSection(
                    title = stringResource(R.string.notifications).uppercase(),
                    icon = Icons.Rounded.Notifications
                ) {
                    SettingsContainer {
                        val toastMsg = stringResource(R.string.enable_reminder_first)
                        val msgEnabled = stringResource(R.string.reminders_enabled)
                        val msgDisabled = stringResource(R.string.reminders_disabled)

                        ListItem(
                            modifier = Modifier.clickable {
                                if (notificationsEnabled) {
                                    showTimePicker = true
                                } else {
                                    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            leadingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Alarm,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            },
                            headlineContent = {
                                Text(stringResource(R.string.daily_reminder), fontWeight = FontWeight.SemiBold)
                            },
                            supportingContent = {
                                Text(if (notificationsEnabled) stringResource(R.string.at_reminder_time, reminderTime) else stringResource(R.string.reminder_is_off))
                            },
                            trailingContent = {
                                Switch(
                                    checked = notificationsEnabled,
                                    onCheckedChange = { enabled ->
                                        coroutineScope.launch {
                                            settingsDataStore.setNotificationsEnabled(enabled)
                                            WorkManagerScheduler.updateNotificationSchedule(context, enabled, reminderTime)
                                            Toast.makeText(context, if (enabled) msgEnabled else msgDisabled, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        )
                    }
                }
            }

            // --- Data & Privacy ---
            item {
                SettingsSection(
                    title = stringResource(R.string.data_and_privacy),
                    icon = Icons.Rounded.Security
                ) {
                    SettingsContainer {
                        Column {
                            DataOption(
                                title = stringResource(R.string.export_data),
                                subtitle = stringResource(R.string.export_data_description),
                                icon = Icons.Rounded.Upload,
                                onClick = { /* TODO */ }
                            )
                            HorizontalDivider()
                            DataOption(
                                title = stringResource(R.string.import_data),
                                subtitle = stringResource(R.string.import_data_description),
                                icon = Icons.Rounded.Download,
                                onClick = { /* TODO */ }
                            )
                        }
                    }
                }
            }

            // --- Destructive Action ---
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    DataOption(
                        title = stringResource(R.string.clear_all_data),
                        subtitle = stringResource(R.string.clear_all_data_description),
                        icon = Icons.Rounded.DeleteForever,
                        iconColor = MaterialTheme.colorScheme.error,
                        onClick = { /* TODO */ },
                        isDestructive = true
                    )
                }
                Spacer(Modifier.height(64.dp))
            }
        }
    }

    if (showTimePicker) {
        val parts = reminderTime.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0

        ModernTimePickerDialog(
            initialHour = h,
            initialMinute = m,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                val timeStr = "%02d:%02d".format(hour, minute)
                val msg = context.getString(R.string.reminder_set, timeStr)
                coroutineScope.launch {
                    settingsDataStore.setReminderTime(timeStr)
                    WorkManagerScheduler.scheduleDailyReminder(context, timeStr)
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
                showTimePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SegmentedSettingRow(
    title: String,
    description: String? = null,
    options: List<ChoiceOption<T>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (title.isNotEmpty()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
        } else if (title.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
        }

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option.value == selected,
                    onClick = { onSelect(option.value) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                ) {
                    Text(text = option.label)
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        content()
    }
}

@Composable
private fun SettingsContainer(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        content()
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String? = null) {
    Column(modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ToggleOption(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        modifier = Modifier.clickable { onCheckedChange(!checked) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = {
            Text(text = title, fontWeight = FontWeight.SemiBold)
        },
        supportingContent = {
            Text(text = subtitle)
        },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = null)
        }
    )
}

@Composable
private fun RadioOption(
    label: String,
    isSelected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = {
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        },
        trailingContent = {
            RadioButton(selected = isSelected, onClick = null, enabled = enabled)
        }
    )
}

@Composable
private fun DataOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val contentColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = (if (isDestructive) MaterialTheme.colorScheme.error else iconColor).copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive) MaterialTheme.colorScheme.error else iconColor
                )
            }
        },
        headlineContent = {
            Text(text = title, fontWeight = FontWeight.SemiBold, color = contentColor)
        },
        supportingContent = {
            Text(
                text = subtitle,
                color = if (isDestructive) MaterialTheme.colorScheme.error.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@Composable
private fun ColorChip(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}