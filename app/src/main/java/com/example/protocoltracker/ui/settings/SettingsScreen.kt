package com.example.protocoltracker.ui.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.protocoltracker.ProtocolTrackerApp
import com.example.protocoltracker.background.reminder.ReminderScheduler
import com.example.protocoltracker.data.settings.AppSettings
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as ProtocolTrackerApp
    val settingsRepository = app.settingsRepository
    val repository = app.repository
    val scope = rememberCoroutineScope()

    val settings by settingsRepository.settingsFlow.collectAsState(initial = AppSettings())

    val foodEntries by remember { repository.observeFoodDrinkEntries() }.collectAsState(initial = emptyList())
    val workoutEntries by remember { repository.observeWorkoutEntries() }.collectAsState(initial = emptyList())
    val weightEntries by remember { repository.observeWeightEntries() }.collectAsState(initial = emptyList())
    val waistEntries by remember { repository.observeWaistEntries() }.collectAsState(initial = emptyList())
    val stepsEntries by remember { repository.observeDailySteps() }.collectAsState(initial = emptyList())
    val milestones by remember { repository.observeMilestones() }.collectAsState(initial = emptyList())

    var startDate by rememberSaveable(settings.startDate) { mutableStateOf(settings.startDate ?: "") }
    var startWeight by rememberSaveable(settings.startWeightKg) { mutableStateOf(settings.startWeightKg?.toString() ?: "") }
    var goalWeight by rememberSaveable(settings.goalWeightKg) { mutableStateOf(settings.goalWeightKg?.toString() ?: "") }
    var calorieBudget by rememberSaveable(settings.calorieBudget) { mutableStateOf(settings.calorieBudget?.toString() ?: "") }
    var proteinTarget by rememberSaveable(settings.proteinTarget) { mutableStateOf(settings.proteinTarget?.toString() ?: "") }
    var fastingStart by rememberSaveable(settings.fastingStart) { mutableStateOf(settings.fastingStart) }
    var fastingEnd by rememberSaveable(settings.fastingEnd) { mutableStateOf(settings.fastingEnd) }
    var reminderTime by rememberSaveable(settings.reminderTime) { mutableStateOf(settings.reminderTime) }

    var statusText by rememberSaveable { mutableStateOf<String?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = RequestPermission()
    ) { granted ->
        statusText = if (granted) "Notifications allowed" else "Notifications not allowed"
    }

    val notificationsGranted = if (Build.VERSION.SDK_INT >= 33) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    val versionName = remember { getAppVersionName(context) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        item {
            SectionCard(
                title = "Targets",
                subtitle = "Keep this tight. These values drive your core tracking context."
            ) {
                SingleSettingField(
                    label = "Start date (yyyy-mm-dd)",
                    value = startDate,
                    onValueChange = { startDate = it }
                )

                SettingFieldRow(
                    leftLabel = "Start weight (kg)",
                    leftValue = startWeight,
                    onLeftChange = { startWeight = it },
                    rightLabel = "Goal weight (kg)",
                    rightValue = goalWeight,
                    onRightChange = { goalWeight = it }
                )

                SettingFieldRow(
                    leftLabel = "Calorie budget",
                    leftValue = calorieBudget,
                    onLeftChange = { calorieBudget = it },
                    rightLabel = "Protein target",
                    rightValue = proteinTarget,
                    onRightChange = { proteinTarget = it }
                )

                SettingFieldRow(
                    leftLabel = "Fasting start (HH:mm)",
                    leftValue = fastingStart,
                    onLeftChange = { fastingStart = it },
                    rightLabel = "Fasting end (HH:mm)",
                    rightValue = fastingEnd,
                    onRightChange = { fastingEnd = it }
                )
            }
        }

        item {
            SectionCard(
                title = "Reminders",
                subtitle = "Only sends a reminder when nothing was logged that day."
            ) {
                SingleSettingField(
                    label = "Reminder time (HH:mm)",
                    value = reminderTime,
                    onValueChange = { reminderTime = it }
                )

                if (Build.VERSION.SDK_INT >= 33) {
                    if (!notificationsGranted) {
                        Button(
                            onClick = {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Allow notifications")
                        }
                    } else {
                        Text(
                            text = "Notifications ready",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = "Notifications ready",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    val parsedStartWeight = startWeight.toDoubleOrNull()
                    val parsedGoalWeight = goalWeight.toDoubleOrNull()
                    val parsedCalorieBudget = calorieBudget.toIntOrNull()
                    val parsedProteinTarget = proteinTarget.toIntOrNull()

                    when {
                        startWeight.isNotBlank() && parsedStartWeight == null -> {
                            statusText = "Start weight must be a number"
                        }
                        goalWeight.isNotBlank() && parsedGoalWeight == null -> {
                            statusText = "Goal weight must be a number"
                        }
                        calorieBudget.isNotBlank() && parsedCalorieBudget == null -> {
                            statusText = "Calorie budget must be a whole number"
                        }
                        proteinTarget.isNotBlank() && parsedProteinTarget == null -> {
                            statusText = "Protein target must be a whole number"
                        }
                        else -> {
                            scope.launch {
                                settingsRepository.setStartDate(startDate.trim())
                                parsedStartWeight?.let { settingsRepository.setStartWeightKg(it) }
                                parsedGoalWeight?.let { settingsRepository.setGoalWeightKg(it) }
                                parsedCalorieBudget?.let { settingsRepository.setCalorieBudget(it) }
                                parsedProteinTarget?.let { settingsRepository.setProteinTarget(it) }
                                settingsRepository.setFastingStart(fastingStart.trim())
                                settingsRepository.setFastingEnd(fastingEnd.trim())
                                settingsRepository.setReminderTime(reminderTime.trim())
                                ReminderScheduler.schedule(context)
                                statusText = "Settings saved"
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save settings")
            }
        }

        item {
            SectionCard(
                title = "Export",
                subtitle = "Creates one ZIP with all CSV files."
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val exportZip = ExportAllManager.createExportZip(
                                    context = context,
                                    foodEntries = foodEntries,
                                    workoutEntries = workoutEntries,
                                    weightEntries = weightEntries,
                                    waistEntries = waistEntries,
                                    stepsEntries = stepsEntries,
                                    milestones = milestones
                                )
                                ExportAllManager.shareZip(context, exportZip)
                                statusText = "Prepared ${exportZip.fileName}"
                            } catch (_: Exception) {
                                statusText = "Export failed"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Export all")
                }

                Text(
                    text = "Includes food/drinks, workouts, weights, waist, steps, and milestones.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            SectionCard(title = "About") {
                Text(
                    text = "Version $versionName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        statusText?.let { message ->
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            content()
        }
    }
}

@Composable
private fun SettingFieldRow(
    leftLabel: String,
    leftValue: String,
    onLeftChange: (String) -> Unit,
    rightLabel: String,
    rightValue: String,
    onRightChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = leftValue,
            onValueChange = onLeftChange,
            modifier = Modifier.weight(1f),
            label = { Text(leftLabel) },
            singleLine = true
        )

        OutlinedTextField(
            value = rightValue,
            onValueChange = onRightChange,
            modifier = Modifier.weight(1f),
            label = { Text(rightLabel) },
            singleLine = true
        )
    }
}

@Composable
private fun SingleSettingField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true
    )
}

private fun getAppVersionName(context: Context): String {
    return runCatching {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        packageInfo.versionName ?: "dev"
    }.getOrDefault("dev")
}