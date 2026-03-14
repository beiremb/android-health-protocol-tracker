package com.example.protocoltracker.ui.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.protocoltracker.ProtocolTrackerApp
import com.example.protocoltracker.data.local.entity.DailyStepsEntry
import com.example.protocoltracker.data.local.entity.FoodDrinkEntry
import com.example.protocoltracker.data.local.entity.FoodDrinkType
import com.example.protocoltracker.data.local.entity.WaistEntry
import com.example.protocoltracker.data.local.entity.WeightEntry
import com.example.protocoltracker.data.local.entity.WorkoutEntry
import com.example.protocoltracker.data.local.entity.WorkoutIntensity
import com.example.protocoltracker.data.local.entity.WorkoutType
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import androidx.compose.foundation.layout.ColumnScope

private enum class LogPanel {
    WEIGHT,
    FOOD,
    DRINK,
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
    val scope = rememberCoroutineScope()
    val today = LocalDate.now().toString()

    var activePanel by remember { mutableStateOf<LogPanel?>(null) }

    // Consumption
    var entryDate by rememberSaveable { mutableStateOf(today) }
    var timeSlot by rememberSaveable { mutableStateOf(defaultTimeSlot()) }
    var entryType by rememberSaveable { mutableStateOf(FoodDrinkType.MEAL.name) }
    var name by rememberSaveable { mutableStateOf("") }
    var calories by rememberSaveable { mutableStateOf("") }
    var proteinGrams by rememberSaveable { mutableStateOf("") }
    var consumptionErrorText by rememberSaveable { mutableStateOf<String?>(null) }
    var timeMenuExpanded by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }

    // Workout
    var workoutDate by rememberSaveable { mutableStateOf(today) }
    var workoutType by rememberSaveable { mutableStateOf(WorkoutType.STRENGTH.name) }
    var workoutIntensity by rememberSaveable { mutableStateOf(WorkoutIntensity.MID.name) }
    var workoutMinutes by rememberSaveable { mutableStateOf("") }
    var workoutTypeMenuExpanded by remember { mutableStateOf(false) }
    var workoutIntensityMenuExpanded by remember { mutableStateOf(false) }
    var workoutErrorText by rememberSaveable { mutableStateOf<String?>(null) }

    // Weight
    var weightDate by rememberSaveable { mutableStateOf(today) }
    var weightKg by rememberSaveable { mutableStateOf("") }
    var weightErrorText by rememberSaveable { mutableStateOf<String?>(null) }

    // Waist
    var waistDate by rememberSaveable { mutableStateOf(today) }
    var waistCm by rememberSaveable { mutableStateOf("") }
    var waistErrorText by rememberSaveable { mutableStateOf<String?>(null) }

    // Steps
    var stepsDate by rememberSaveable { mutableStateOf(today) }
    var stepsValue by rememberSaveable { mutableStateOf("") }
    var stepsErrorText by rememberSaveable { mutableStateOf<String?>(null) }

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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Log",
            style = MaterialTheme.typography.headlineSmall
        )

        LogActionBar("Weight") { activePanel = LogPanel.WEIGHT }
        LogActionBar("Food") {
            entryType = FoodDrinkType.MEAL.name
            activePanel = LogPanel.FOOD
        }
        LogActionBar("Drink") {
            entryType = FoodDrinkType.DRINK.name
            activePanel = LogPanel.DRINK
        }
        LogActionBar("Waist") { activePanel = LogPanel.WAIST }
        LogActionBar("Steps") { activePanel = LogPanel.STEPS }
        LogActionBar("Workout") { activePanel = LogPanel.WORKOUT }
    }

    if (activePanel == LogPanel.FOOD || activePanel == LogPanel.DRINK) {
        SimpleDialog(
            title = "Log your food or drink",
            onDismiss = { activePanel = null }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column {
                    OutlinedButton(
                        onClick = { typeMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Type: ${FoodDrinkType.valueOf(entryType).label()}")
                    }

                    DropdownMenu(
                        expanded = typeMenuExpanded,
                        onDismissRequest = { typeMenuExpanded = false }
                    ) {
                        FoodDrinkType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.label()) },
                                onClick = {
                                    entryType = type.name
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

                consumptionErrorText?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = {
                        val trimmedName = name.trim()
                        val calorieValue = calories.toIntOrNull()
                        val proteinValue = proteinGrams.toIntOrNull()

                        when {
                            entryDate.isBlank() -> consumptionErrorText = "Enter a date."
                            trimmedName.isBlank() -> consumptionErrorText = "Enter a name."
                            calorieValue == null -> consumptionErrorText = "Calories must be a whole number."
                            proteinGrams.isNotBlank() && proteinValue == null ->
                                consumptionErrorText = "Protein must be a whole number."
                            else -> {
                                scope.launch {
                                    repository.insertFoodDrinkEntry(
                                        FoodDrinkEntry(
                                            entryDate = entryDate,
                                            timeSlot = timeSlot,
                                            entryType = FoodDrinkType.valueOf(entryType),
                                            name = trimmedName,
                                            calories = calorieValue,
                                            proteinGrams = proteinValue,
                                            templateId = null
                                        )
                                    )
                                    name = ""
                                    calories = ""
                                    proteinGrams = ""
                                    consumptionErrorText = null
                                    activePanel = null
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

    if (activePanel == LogPanel.WORKOUT) {
        SimpleDialog(
            title = "Log your workout",
            onDismiss = { activePanel = null }
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

                workoutErrorText?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = {
                        val minutesValue = workoutMinutes.toIntOrNull()

                        when {
                            workoutDate.isBlank() -> workoutErrorText = "Enter a date."
                            minutesValue == null -> workoutErrorText = "Minutes must be a whole number."
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
                                    workoutMinutes = ""
                                    workoutErrorText = null
                                    activePanel = null
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

    if (activePanel == LogPanel.WEIGHT) {
        SimpleDialog(
            title = "Log your weight",
            onDismiss = { activePanel = null }
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

                weightErrorText?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = {
                        val weightValue = weightKg.toDoubleOrNull()

                        when {
                            weightDate.isBlank() -> weightErrorText = "Enter a date."
                            weightValue == null -> weightErrorText = "Weight must be a number."
                            else -> {
                                scope.launch {
                                    repository.insertWeightEntry(
                                        WeightEntry(
                                            entryDate = weightDate,
                                            entryTime = currentTime(),
                                            weightKg = weightValue
                                        )
                                    )
                                    weightKg = ""
                                    weightErrorText = null
                                    activePanel = null
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

    if (activePanel == LogPanel.WAIST) {
        SimpleDialog(
            title = "Log your waist",
            onDismiss = { activePanel = null }
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

                waistErrorText?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = {
                        val waistValue = waistCm.toDoubleOrNull()

                        when {
                            waistDate.isBlank() -> waistErrorText = "Enter a date."
                            waistValue == null -> waistErrorText = "Waist must be a number."
                            else -> {
                                scope.launch {
                                    repository.insertWaistEntry(
                                        WaistEntry(
                                            entryDate = waistDate,
                                            waistCm = waistValue
                                        )
                                    )
                                    waistCm = ""
                                    waistErrorText = null
                                    activePanel = null
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

    if (activePanel == LogPanel.STEPS) {
        SimpleDialog(
            title = "Log your steps",
            onDismiss = { activePanel = null }
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

                stepsErrorText?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = {
                        val parsedSteps = stepsValue.toIntOrNull()

                        when {
                            stepsDate.isBlank() -> stepsErrorText = "Enter a date."
                            parsedSteps == null -> stepsErrorText = "Steps must be a whole number."
                            else -> {
                                scope.launch {
                                    repository.upsertDailySteps(
                                        DailyStepsEntry(
                                            entryDate = stepsDate,
                                            steps = parsedSteps
                                        )
                                    )
                                    stepsValue = ""
                                    stepsErrorText = null
                                    activePanel = null
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
                    Text("Steps: ${stepsEntryToday!!.steps}")
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.LogActionBar(
    label: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun SimpleDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                content()
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun FoodEntryRow(entry: FoodDrinkEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("${entry.timeSlot} • ${entry.entryType.label()}", style = MaterialTheme.typography.labelLarge)
            Text(entry.name, style = MaterialTheme.typography.titleMedium)
            Text("Calories: ${entry.calories}")
            entry.proteinGrams?.let { Text("Protein: $it g") }
        }
    }
}

@Composable
private fun WorkoutEntryRow(entry: WorkoutEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
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
    Card(modifier = Modifier.fillMaxWidth()) {
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(entry.entryDate, style = MaterialTheme.typography.labelLarge)
            Text("${entry.waistCm} cm", style = MaterialTheme.typography.titleMedium)
        }
    }
}