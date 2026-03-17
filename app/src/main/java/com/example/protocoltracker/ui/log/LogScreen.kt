package com.example.protocoltracker.ui.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.protocoltracker.ProtocolTrackerApp
import com.example.protocoltracker.data.local.entity.DailyStepsEntry
import com.example.protocoltracker.data.local.entity.FoodDrinkEntry
import com.example.protocoltracker.data.local.entity.FoodDrinkType
import com.example.protocoltracker.data.local.entity.WaistEntry
import com.example.protocoltracker.data.local.entity.WeightEntry
import com.example.protocoltracker.data.local.entity.WorkoutEntry
import com.example.protocoltracker.data.local.entity.WorkoutIntensity
import com.example.protocoltracker.data.local.entity.WorkoutType
import com.example.protocoltracker.ui.common.AppDialogCard
import com.example.protocoltracker.ui.common.AppPageTitle
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

private enum class LogPanel {
    WEIGHT,
    FOOD,
    WAIST,
    STEPS,
    WORKOUT
}

private fun allTimeSlots(): List<String> =
    (0..23).flatMap { hour ->
        listOf("%02d:00".format(hour), "%02d:30".format(hour))
    }

private fun currentTime(): String {
    val now = LocalTime.now()
    return "%02d:%02d".format(now.hour, now.minute)
}

private fun defaultTimeSlot(): String {
    val now = LocalTime.now()
    val minute = if (now.minute < 30) "00" else "30"
    return "%02d:%s".format(now.hour, minute)
}

private fun FoodDrinkType.label(): String =
    when (this) {
        FoodDrinkType.MEAL -> "Meal"
        FoodDrinkType.SNACK -> "Snack"
        FoodDrinkType.DRINK -> "Drink"
    }

private fun WorkoutType.label(): String =
    when (this) {
        WorkoutType.STRENGTH -> "Strength"
        WorkoutType.WALKING -> "Walking"
        WorkoutType.KUNG_FU -> "Kung fu"
        WorkoutType.CYCLING -> "Cycling"
        WorkoutType.RUNNING -> "Running"
        WorkoutType.RECOVERY -> "Recovery"
        WorkoutType.TENNIS -> "Tennis"
        WorkoutType.OTHER -> "Other"
    }

private fun WorkoutIntensity.label(): String =
    when (this) {
        WorkoutIntensity.LOW -> "Low"
        WorkoutIntensity.MID -> "Mid"
        WorkoutIntensity.HIGH -> "High"
    }

@Composable
fun LogScreen() {
    val app = LocalContext.current.applicationContext as ProtocolTrackerApp
    val repository = app.repository
    val today = LocalDate.now().toString()

    var activePanel by remember { mutableStateOf<LogPanel?>(null) }

    fun closePanel() {
        activePanel = null
    }
    val foodEntriesToday by remember(today) {
        repository.observeFoodDrinkEntriesByDate(today)
    }.collectAsState(initial = emptyList())

    val workoutEntriesToday by remember(today) {
        repository.observeWorkoutEntriesByDate(today)
    }.collectAsState(initial = emptyList())

    val weightEntriesToday by remember(today) {
        repository.observeWeightEntriesByDate(today)
    }.collectAsState(initial = emptyList())

    val waistEntriesToday by remember(today) {
        repository.observeWaistEntriesByDate(today)
    }.collectAsState(initial = emptyList())

    val stepsEntryToday by remember(today) {
        repository.observeDailyStepsByDate(today)
    }.collectAsState(initial = null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppPageTitle(
            title = "Log",
            subtitle = "Choose what to log."
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BrandLogButton(
                label = "Weight",
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = { activePanel = LogPanel.WEIGHT },
                modifier = Modifier.weight(1f)
            )
            BrandLogButton(
                label = "Food & drink",
                containerColor = MaterialTheme.colorScheme.secondary,
                onClick = { activePanel = LogPanel.FOOD },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BrandLogButton(
                label = "Waist",
                containerColor = MaterialTheme.colorScheme.secondary,
                onClick = { activePanel = LogPanel.WAIST },
                modifier = Modifier.weight(1f)
            )
            BrandLogButton(
                label = "Steps",
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = { activePanel = LogPanel.STEPS },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            BrandLogButton(
                label = "Workout",
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = { activePanel = LogPanel.WORKOUT },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    when (activePanel) {
        LogPanel.FOOD -> FoodDialog(
            today = today,
            foodEntriesToday = foodEntriesToday,
            onDismiss = ::closePanel
        )

        LogPanel.WORKOUT -> WorkoutDialog(
            today = today,
            workoutEntriesToday = workoutEntriesToday,
            onDismiss = ::closePanel
        )

        LogPanel.WEIGHT -> WeightDialog(
            today = today,
            weightEntriesToday = weightEntriesToday,
            onDismiss = ::closePanel
        )

        LogPanel.WAIST -> WaistDialog(
            today = today,
            waistEntriesToday = waistEntriesToday,
            onDismiss = ::closePanel
        )

        LogPanel.STEPS -> StepsDialog(
            today = today,
            stepsEntryToday = stepsEntryToday,
            onDismiss = ::closePanel
        )

        null -> Unit
    }
}

@Composable
private fun FoodDialog(
    today: String,
    foodEntriesToday: List<FoodDrinkEntry>,
    onDismiss: () -> Unit
) {
    val app = LocalContext.current.applicationContext as ProtocolTrackerApp
    val repository = app.repository
    val scope = rememberCoroutineScope()

    var entryDate by remember { mutableStateOf(today) }
    var timeSlot by remember { mutableStateOf(defaultTimeSlot()) }
    var selectedEntryType by remember { mutableStateOf(FoodDrinkType.MEAL.name) }
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var proteinGrams by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var timeMenuExpanded by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }

    AppDialogCard(
        title = "Log food or drink",
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column {
                OutlinedButton(
                    onClick = { typeMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Type: ${FoodDrinkType.valueOf(selectedEntryType).label()}")
                }

                DropdownMenu(
                    expanded = typeMenuExpanded,
                    onDismissRequest = { typeMenuExpanded = false }
                ) {
                    FoodDrinkType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.label()) },
                            onClick = {
                                selectedEntryType = type.name
                                typeMenuExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = entryDate,
                onValueChange = { entryDate = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date (yyyy-mm-dd)") },
                singleLine = true
            )

            Column {
                OutlinedButton(
                    onClick = { timeMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Time: $timeSlot")
                }

                DropdownMenu(
                    expanded = timeMenuExpanded,
                    onDismissRequest = { timeMenuExpanded = false }
                ) {
                    allTimeSlots().forEach { slot ->
                        DropdownMenuItem(
                            text = { Text(slot) },
                            onClick = {
                                timeSlot = slot
                                timeMenuExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Name") },
                singleLine = true
            )

            OutlinedTextField(
                value = calories,
                onValueChange = { calories = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Calories") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = proteinGrams,
                onValueChange = { proteinGrams = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Protein grams (optional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            errorText?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val trimmedName = name.trim()
                    val calorieValue = calories.toIntOrNull()
                    val proteinValue = proteinGrams.toIntOrNull()

                    when {
                        entryDate.isBlank() -> errorText = "Enter a date."
                        trimmedName.isBlank() -> errorText = "Enter a name."
                        calorieValue == null -> errorText = "Calories must be a whole number."
                        proteinGrams.isNotBlank() && proteinValue == null ->
                            errorText = "Protein must be a whole number."
                        else -> {
                            scope.launch {
                                repository.insertFoodDrinkEntry(
                                    FoodDrinkEntry(
                                        entryDate = entryDate,
                                        timeSlot = timeSlot,
                                        entryType = FoodDrinkType.valueOf(selectedEntryType),
                                        name = trimmedName,
                                        calories = calorieValue,
                                        proteinGrams = proteinValue,
                                        templateId = null
                                    )
                                )
                                onDismiss()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }

            HorizontalDivider()

            Text("Today's logs", style = MaterialTheme.typography.titleSmall)

            if (foodEntriesToday.isEmpty()) {
                Text("No logs yet.")
            } else {
                foodEntriesToday.forEach { entry ->
                    FoodEntryRow(entry)
                }
            }
        }
    }
}

@Composable
private fun WorkoutDialog(
    today: String,
    workoutEntriesToday: List<WorkoutEntry>,
    onDismiss: () -> Unit
) {
    val app = LocalContext.current.applicationContext as ProtocolTrackerApp
    val repository = app.repository
    val scope = rememberCoroutineScope()

    var workoutDate by remember { mutableStateOf(today) }
    var workoutType by remember { mutableStateOf(WorkoutType.STRENGTH.name) }
    var workoutIntensity by remember { mutableStateOf(WorkoutIntensity.MID.name) }
    var workoutMinutes by remember { mutableStateOf("") }
    var workoutTypeMenuExpanded by remember { mutableStateOf(false) }
    var workoutIntensityMenuExpanded by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AppDialogCard(
        title = "Log your workout",
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = workoutDate,
                onValueChange = { workoutDate = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date (yyyy-mm-dd)") },
                singleLine = true
            )

            Column {
                OutlinedButton(
                    onClick = { workoutTypeMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Type: ${WorkoutType.valueOf(workoutType).label()}")
                }

                DropdownMenu(
                    expanded = workoutTypeMenuExpanded,
                    onDismissRequest = { workoutTypeMenuExpanded = false }
                ) {
                    WorkoutType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.label()) },
                            onClick = {
                                workoutType = type.name
                                workoutTypeMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Column {
                OutlinedButton(
                    onClick = { workoutIntensityMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Intensity: ${WorkoutIntensity.valueOf(workoutIntensity).label()}")
                }

                DropdownMenu(
                    expanded = workoutIntensityMenuExpanded,
                    onDismissRequest = { workoutIntensityMenuExpanded = false }
                ) {
                    WorkoutIntensity.entries.forEach { intensity ->
                        DropdownMenuItem(
                            text = { Text(intensity.label()) },
                            onClick = {
                                workoutIntensity = intensity.name
                                workoutIntensityMenuExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = workoutMinutes,
                onValueChange = { workoutMinutes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Minutes") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            errorText?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val minutesValue = workoutMinutes.toIntOrNull()

                    when {
                        workoutDate.isBlank() -> errorText = "Enter a date."
                        minutesValue == null -> errorText = "Minutes must be a whole number."
                        else -> {
                            scope.launch {
                                repository.insertWorkoutEntry(
                                    WorkoutEntry(
                                        entryDate = workoutDate,
                                        workoutType = WorkoutType.valueOf(workoutType),
                                        intensity = WorkoutIntensity.valueOf(workoutIntensity),
                                        minutes = minutesValue
                                    )
                                )
                                onDismiss()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }

            HorizontalDivider()

            Text("Today's logs", style = MaterialTheme.typography.titleSmall)

            if (workoutEntriesToday.isEmpty()) {
                Text("No logs yet.")
            } else {
                workoutEntriesToday.forEach { entry ->
                    WorkoutEntryRow(entry)
                }
            }
        }
    }
}

@Composable
private fun WeightDialog(
    today: String,
    weightEntriesToday: List<WeightEntry>,
    onDismiss: () -> Unit
) {
    val app = LocalContext.current.applicationContext as ProtocolTrackerApp
    val repository = app.repository
    val scope = rememberCoroutineScope()

    var weightDate by remember { mutableStateOf(today) }
    var weightKg by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AppDialogCard(
        title = "Log your weight",
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = weightDate,
                onValueChange = { weightDate = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date (yyyy-mm-dd)") },
                singleLine = true
            )

            OutlinedTextField(
                value = weightKg,
                onValueChange = { weightKg = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Weight (kg)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            errorText?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val weightValue = weightKg.toDoubleOrNull()

                    when {
                        weightDate.isBlank() -> errorText = "Enter a date."
                        weightValue == null -> errorText = "Weight must be a number."
                        else -> {
                            scope.launch {
                                repository.insertWeightEntry(
                                    WeightEntry(
                                        entryDate = weightDate,
                                        entryTime = currentTime(),
                                        weightKg = weightValue
                                    )
                                )
                                onDismiss()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }

            HorizontalDivider()

            Text("Today's logs", style = MaterialTheme.typography.titleSmall)

            if (weightEntriesToday.isEmpty()) {
                Text("No logs yet.")
            } else {
                weightEntriesToday.forEach { entry ->
                    WeightEntryRow(entry)
                }
            }
        }
    }
}

@Composable
private fun WaistDialog(
    today: String,
    waistEntriesToday: List<WaistEntry>,
    onDismiss: () -> Unit
) {
    val app = LocalContext.current.applicationContext as ProtocolTrackerApp
    val repository = app.repository
    val scope = rememberCoroutineScope()

    var waistDate by remember { mutableStateOf(today) }
    var waistCm by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AppDialogCard(
        title = "Log your waist",
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = waistDate,
                onValueChange = { waistDate = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date (yyyy-mm-dd)") },
                singleLine = true
            )

            OutlinedTextField(
                value = waistCm,
                onValueChange = { waistCm = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Waist (cm)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            errorText?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val waistValue = waistCm.toDoubleOrNull()

                    when {
                        waistDate.isBlank() -> errorText = "Enter a date."
                        waistValue == null -> errorText = "Waist must be a number."
                        else -> {
                            scope.launch {
                                repository.insertWaistEntry(
                                    WaistEntry(
                                        entryDate = waistDate,
                                        waistCm = waistValue
                                    )
                                )
                                onDismiss()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }

            HorizontalDivider()

            Text("Today's logs", style = MaterialTheme.typography.titleSmall)

            if (waistEntriesToday.isEmpty()) {
                Text("No logs yet.")
            } else {
                waistEntriesToday.forEach { entry ->
                    WaistEntryRow(entry)
                }
            }
        }
    }
}

@Composable
private fun StepsDialog(
    today: String,
    stepsEntryToday: DailyStepsEntry?,
    onDismiss: () -> Unit
) {
    val app = LocalContext.current.applicationContext as ProtocolTrackerApp
    val repository = app.repository
    val scope = rememberCoroutineScope()

    var stepsDate by remember { mutableStateOf(today) }
    var stepsValue by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AppDialogCard(
        title = "Log your steps",
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = stepsDate,
                onValueChange = { stepsDate = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date (yyyy-mm-dd)") },
                singleLine = true
            )

            OutlinedTextField(
                value = stepsValue,
                onValueChange = { stepsValue = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Steps") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            errorText?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val parsedSteps = stepsValue.toIntOrNull()

                    when {
                        stepsDate.isBlank() -> errorText = "Enter a date."
                        parsedSteps == null -> errorText = "Steps must be a whole number."
                        else -> {
                            scope.launch {
                                repository.upsertDailySteps(
                                    DailyStepsEntry(
                                        entryDate = stepsDate,
                                        steps = parsedSteps
                                    )
                                )
                                onDismiss()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }

            HorizontalDivider()

            Text("Today's logs", style = MaterialTheme.typography.titleSmall)

            if (stepsEntryToday == null) {
                Text("No logs yet.")
            } else {
                Text("Steps: ${stepsEntryToday.steps}")
            }
        }
    }
}

@Composable
private fun BrandLogButton(
    label: String,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun FoodEntryRow(entry: FoodDrinkEntry) {
    androidx.compose.material3.Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "${entry.timeSlot} • ${entry.entryType.label()}",
                style = MaterialTheme.typography.labelLarge
            )
            Text(entry.name, style = MaterialTheme.typography.titleMedium)
            Text("Calories: ${entry.calories}")
            entry.proteinGrams?.let { Text("Protein: $it g") }
        }
    }
}

@Composable
private fun WorkoutEntryRow(entry: WorkoutEntry) {
    androidx.compose.material3.Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(entry.workoutType.label(), style = MaterialTheme.typography.titleMedium)
            Text("Intensity: ${entry.intensity.label()}")
            Text("Minutes: ${entry.minutes}")
        }
    }
}

@Composable
private fun WeightEntryRow(entry: WeightEntry) {
    androidx.compose.material3.Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(entry.entryDate, style = MaterialTheme.typography.labelLarge)
            Text("${entry.weightKg} kg", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun WaistEntryRow(entry: WaistEntry) {
    androidx.compose.material3.Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(entry.entryDate, style = MaterialTheme.typography.labelLarge)
            Text("${entry.waistCm} cm", style = MaterialTheme.typography.titleMedium)
        }
    }
}