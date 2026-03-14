package com.example.protocoltracker.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import com.example.protocoltracker.data.local.entity.DailyStepsEntry
import com.example.protocoltracker.data.local.entity.FoodDrinkEntry
import com.example.protocoltracker.data.local.entity.MilestoneEntry
import com.example.protocoltracker.data.local.entity.WaistEntry
import com.example.protocoltracker.data.local.entity.WeightEntry
import com.example.protocoltracker.data.local.entity.WorkoutEntry
import com.example.protocoltracker.data.settings.AppSettings
import kotlinx.coroutines.launch

private data class PendingExport(
    val fileName: String,
    val content: String
)

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
    var pendingExport by remember { mutableStateOf<PendingExport?>(null) }

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

    val exportLauncher = rememberLauncherForActivityResult(
        contract = CreateDocument("text/csv")
    ) { uri: Uri? ->
        val export = pendingExport
        if (uri == null || export == null) return@rememberLauncherForActivityResult

        scope.launch {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(export.content.toByteArray())
                }
            }.onSuccess {
                statusText = "Exported ${export.fileName}"
            }.onFailure {
                statusText = "Export failed"
            }
            pendingExport = null
        }
    }

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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Core settings", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Start date (yyyy-mm-dd)") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = startWeight,
                        onValueChange = { startWeight = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Start weight (kg)") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = goalWeight,
                        onValueChange = { goalWeight = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Goal weight (kg)") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = calorieBudget,
                        onValueChange = { calorieBudget = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Calorie budget") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = proteinTarget,
                        onValueChange = { proteinTarget = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Protein target") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = fastingStart,
                        onValueChange = { fastingStart = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Fasting start (HH:mm)") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = fastingEnd,
                        onValueChange = { fastingEnd = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Fasting end (HH:mm)") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = reminderTime,
                        onValueChange = { reminderTime = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Reminder time (HH:mm)") },
                        singleLine = true
                    )

                    if (Build.VERSION.SDK_INT >= 33) {
                        Button(
                            onClick = {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (notificationsGranted) "Notifications allowed" else "Allow notifications")
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                settingsRepository.setStartDate(startDate)
                                startWeight.toDoubleOrNull()?.let { settingsRepository.setStartWeightKg(it) }
                                goalWeight.toDoubleOrNull()?.let { settingsRepository.setGoalWeightKg(it) }
                                calorieBudget.toIntOrNull()?.let { settingsRepository.setCalorieBudget(it) }
                                proteinTarget.toIntOrNull()?.let { settingsRepository.setProteinTarget(it) }
                                settingsRepository.setFastingStart(fastingStart)
                                settingsRepository.setFastingEnd(fastingEnd)
                                settingsRepository.setReminderTime(reminderTime)
                                ReminderScheduler.schedule(context)
                                statusText = "Settings saved"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save settings")
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Export CSV", style = MaterialTheme.typography.titleMedium)

                    ExportButton("food_drinks.csv") {
                        launchExport(
                            fileName = "food_drinks.csv",
                            content = foodDrinksCsv(foodEntries),
                            pendingExportSetter = { pendingExport = it },
                            launcher = { exportLauncher.launch(it) }
                        )
                    }

                    ExportButton("workouts.csv") {
                        launchExport(
                            fileName = "workouts.csv",
                            content = workoutsCsv(workoutEntries),
                            pendingExportSetter = { pendingExport = it },
                            launcher = { exportLauncher.launch(it) }
                        )
                    }

                    ExportButton("weights.csv") {
                        launchExport(
                            fileName = "weights.csv",
                            content = weightsCsv(weightEntries),
                            pendingExportSetter = { pendingExport = it },
                            launcher = { exportLauncher.launch(it) }
                        )
                    }

                    ExportButton("waist.csv") {
                        launchExport(
                            fileName = "waist.csv",
                            content = waistCsv(waistEntries),
                            pendingExportSetter = { pendingExport = it },
                            launcher = { exportLauncher.launch(it) }
                        )
                    }

                    ExportButton("steps.csv") {
                        launchExport(
                            fileName = "steps.csv",
                            content = stepsCsv(stepsEntries),
                            pendingExportSetter = { pendingExport = it },
                            launcher = { exportLauncher.launch(it) }
                        )
                    }

                    ExportButton("milestones.csv") {
                        launchExport(
                            fileName = "milestones.csv",
                            content = milestonesCsv(milestones),
                            pendingExportSetter = { pendingExport = it },
                            launcher = { exportLauncher.launch(it) }
                        )
                    }
                }
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
private fun ExportButton(
    fileName: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Export $fileName")
    }
}

private fun launchExport(
    fileName: String,
    content: String,
    pendingExportSetter: (PendingExport) -> Unit,
    launcher: (String) -> Unit
) {
    pendingExportSetter(PendingExport(fileName, content))
    launcher(fileName)
}

private fun csvCell(value: String): String {
    val escaped = value.replace("\"", "\"\"")
    return "\"$escaped\""
}

private fun foodDrinksCsv(entries: List<FoodDrinkEntry>): String {
    val header = "id,date,time,type,name,calories,protein_grams,template_id"
    val rows = entries.map {
        listOf(
            it.id.toString(),
            csvCell(it.entryDate),
            csvCell(it.timeSlot),
            csvCell(it.entryType.name),
            csvCell(it.name),
            it.calories.toString(),
            it.proteinGrams?.toString() ?: "",
            it.templateId?.toString() ?: ""
        ).joinToString(",")
    }
    return (listOf(header) + rows).joinToString("\n")
}

private fun workoutsCsv(entries: List<WorkoutEntry>): String {
    val header = "id,date,type,intensity,minutes"
    val rows = entries.map {
        listOf(
            it.id.toString(),
            csvCell(it.entryDate),
            csvCell(it.workoutType.name),
            csvCell(it.intensity.name),
            it.minutes.toString()
        ).joinToString(",")
    }
    return (listOf(header) + rows).joinToString("\n")
}

private fun weightsCsv(entries: List<WeightEntry>): String {
    val header = "id,date,time,weight_kg"
    val rows = entries.map {
        listOf(
            it.id.toString(),
            csvCell(it.entryDate),
            csvCell(it.entryTime),
            it.weightKg.toString()
        ).joinToString(",")
    }
    return (listOf(header) + rows).joinToString("\n")
}

private fun waistCsv(entries: List<WaistEntry>): String {
    val header = "id,date,waist_cm"
    val rows = entries.map {
        listOf(
            it.id.toString(),
            csvCell(it.entryDate),
            it.waistCm.toString()
        ).joinToString(",")
    }
    return (listOf(header) + rows).joinToString("\n")
}

private fun stepsCsv(entries: List<DailyStepsEntry>): String {
    val header = "date,steps"
    val rows = entries.map {
        listOf(
            csvCell(it.entryDate),
            it.steps.toString()
        ).joinToString(",")
    }
    return (listOf(header) + rows).joinToString("\n")
}

private fun milestonesCsv(entries: List<MilestoneEntry>): String {
    val header = "id,target_weight_kg,target_date,reward_text,sort_order"
    val rows = entries.map {
        listOf(
            it.id.toString(),
            it.targetWeightKg.toString(),
            csvCell(it.targetDate),
            csvCell(it.rewardText),
            it.sortOrder.toString()
        ).joinToString(",")
    }
    return (listOf(header) + rows).joinToString("\n")
}