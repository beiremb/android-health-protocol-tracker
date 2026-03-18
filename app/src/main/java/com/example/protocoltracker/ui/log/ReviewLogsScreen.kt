package com.example.protocoltracker.ui.log

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import com.example.protocoltracker.ui.common.SectionCard
import com.example.protocoltracker.ui.theme.BrandTeal
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private sealed class ReviewLogItem {
    abstract val key: String
    abstract val sortDate: String
    abstract val sortTime: String
    abstract val header: String
    abstract val details: String

    data class Food(val entry: FoodDrinkEntry) : ReviewLogItem() {
        override val key: String = "food_${entry.id}"
        override val sortDate: String = entry.entryDate
        override val sortTime: String = entry.timeSlot
        override val header: String = "${entry.entryDate} · Food & drink"
        override val details: String = buildString {
            append("${entry.timeSlot} · ${entry.entryType.label()} · ${entry.name} · ${entry.calories} kcal")
            entry.proteinGrams?.let { append(" · ${it}g protein") }
        }
    }

    data class Workout(val entry: WorkoutEntry) : ReviewLogItem() {
        override val key: String = "workout_${entry.id}"
        override val sortDate: String = entry.entryDate
        override val sortTime: String = "12:00"
        override val header: String = "${entry.entryDate} · Workout"
        override val details: String = "${entry.workoutType.label()} · ${entry.intensity.label()} · ${entry.minutes} min"
    }

    data class Weight(val entry: WeightEntry) : ReviewLogItem() {
        override val key: String = "weight_${entry.id}"
        override val sortDate: String = entry.entryDate
        override val sortTime: String = entry.entryTime
        override val header: String = "${entry.entryDate} · Weight"
        override val details: String = "${entry.entryTime} · ${formatSmartNumber(entry.weightKg)} kg"
    }

    data class Waist(val entry: WaistEntry) : ReviewLogItem() {
        override val key: String = "waist_${entry.id}"
        override val sortDate: String = entry.entryDate
        override val sortTime: String = "12:00"
        override val header: String = "${entry.entryDate} · Waist"
        override val details: String = "${formatSmartNumber(entry.waistCm)} cm"
    }

    data class Steps(val entry: DailyStepsEntry) : ReviewLogItem() {
        override val key: String = "steps_${entry.entryDate}"
        override val sortDate: String = entry.entryDate
        override val sortTime: String = "12:00"
        override val header: String = "${entry.entryDate} · Steps"
        override val details: String = "${entry.steps} steps"
    }
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

private fun allTimeSlots(): List<String> =
    (0..23).flatMap { hour ->
        listOf("%02d:00".format(hour), "%02d:30".format(hour))
    }

@Composable
fun ReviewLogsScreen(
    onBack: () -> Unit
) {
    val app = LocalContext.current.applicationContext as ProtocolTrackerApp
    val repository = app.repository
    val scope = rememberCoroutineScope()

    val foodEntries by remember { repository.observeFoodDrinkEntries() }.collectAsState(initial = emptyList())
    val workoutEntries by remember { repository.observeWorkoutEntries() }.collectAsState(initial = emptyList())
    val weightEntries by remember { repository.observeWeightEntries() }.collectAsState(initial = emptyList())
    val waistEntries by remember { repository.observeWaistEntries() }.collectAsState(initial = emptyList())
    val stepsEntries by remember { repository.observeDailySteps() }.collectAsState(initial = emptyList())

    var editTarget by remember { mutableStateOf<ReviewLogItem?>(null) }

    fun closeEditTarget() {
        editTarget = null
    }

    val reviewItems = remember(
        foodEntries,
        workoutEntries,
        weightEntries,
        waistEntries,
        stepsEntries
    ) {
        buildList {
            addAll(foodEntries.map { ReviewLogItem.Food(it) })
            addAll(workoutEntries.map { ReviewLogItem.Workout(it) })
            addAll(weightEntries.map { ReviewLogItem.Weight(it) })
            addAll(waistEntries.map { ReviewLogItem.Waist(it) })
            addAll(stepsEntries.map { ReviewLogItem.Steps(it) })
        }.sortedWith(
            compareByDescending<ReviewLogItem> { it.sortDate }
                .thenByDescending { it.sortTime }
                .thenByDescending { it.key }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AppPageTitle(
                    title = "Review logs",
                    subtitle = "Newest first. Edit or delete any entry."
                )
            }

            OutlinedButton(
                onClick = onBack
            ) {
                Text("Back")
            }
        }

        if (reviewItems.isEmpty()) {
            SectionCard {
                Text("No logs yet.")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = reviewItems,
                    key = { it.key }
                ) { item ->
                    ReviewLogRow(
                        item = item,
                        onEdit = { editTarget = item },
                        onDelete = {
                            scope.launch {
                                when (item) {
                                    is ReviewLogItem.Food -> repository.deleteFoodDrinkEntry(item.entry)
                                    is ReviewLogItem.Workout -> repository.deleteWorkoutEntry(item.entry)
                                    is ReviewLogItem.Weight -> repository.deleteWeightEntry(item.entry)
                                    is ReviewLogItem.Waist -> repository.deleteWaistEntry(item.entry)
                                    is ReviewLogItem.Steps -> repository.deleteDailySteps(item.entry)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    when (val target = editTarget) {
        is ReviewLogItem.Food -> EditFoodDialog(
            entry = target.entry,
            onDismiss = ::closeEditTarget,
            onSave = { updated ->
                scope.launch {
                    repository.updateFoodDrinkEntry(updated)
                    closeEditTarget()
                }
            }
        )

        is ReviewLogItem.Workout -> EditWorkoutDialog(
            entry = target.entry,
            onDismiss = ::closeEditTarget,
            onSave = { updated ->
                scope.launch {
                    repository.updateWorkoutEntry(updated)
                    closeEditTarget()
                }
            }
        )

        is ReviewLogItem.Weight -> EditWeightDialog(
            entry = target.entry,
            onDismiss = ::closeEditTarget,
            onSave = { updated ->
                scope.launch {
                    repository.updateWeightEntry(updated)
                    closeEditTarget()
                }
            }
        )

        is ReviewLogItem.Waist -> EditWaistDialog(
            entry = target.entry,
            onDismiss = ::closeEditTarget,
            onSave = { updated ->
                scope.launch {
                    repository.updateWaistEntry(updated)
                    closeEditTarget()
                }
            }
        )

        is ReviewLogItem.Steps -> EditStepsDialog(
            entry = target.entry,
            onDismiss = ::closeEditTarget,
            onSave = { updated ->
                scope.launch {
                    if (updated.entryDate != target.entry.entryDate) {
                        repository.deleteDailySteps(target.entry)
                    }
                    repository.upsertDailySteps(updated)
                    closeEditTarget()
                }
            }
        )

        null -> Unit
    }
}

@Composable
private fun ReviewLogRow(
    item: ReviewLogItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = item.header,
                style = MaterialTheme.typography.labelLarge
            )

            Text(
                text = item.details,
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, BrandTeal),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = BrandTeal
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit"
                    )
                    Text(" Edit")
                }

                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete"
                    )
                    Text(" Delete")
                }
            }
        }
    }
}

@Composable
private fun EditFoodDialog(
    entry: FoodDrinkEntry,
    onDismiss: () -> Unit,
    onSave: (FoodDrinkEntry) -> Unit
) {
    var entryDate by remember(entry.id) { mutableStateOf(entry.entryDate) }
    var timeSlot by remember(entry.id) { mutableStateOf(entry.timeSlot) }
    var selectedEntryType by remember(entry.id) { mutableStateOf(entry.entryType.name) }
    var name by remember(entry.id) { mutableStateOf(entry.name) }
    var calories by remember(entry.id) { mutableStateOf(entry.calories.toString()) }
    var proteinGrams by remember(entry.id) { mutableStateOf(entry.proteinGrams?.toString() ?: "") }
    var errorText by remember(entry.id) { mutableStateOf<String?>(null) }
    var timeMenuExpanded by remember(entry.id) { mutableStateOf(false) }
    var typeMenuExpanded by remember(entry.id) { mutableStateOf(false) }

    AppDialogCard(
        title = "Edit food or drink",
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
                        proteinGrams.isNotBlank() && proteinValue == null -> errorText = "Protein must be a whole number."
                        else -> {
                            onSave(
                                entry.copy(
                                    entryDate = entryDate,
                                    timeSlot = timeSlot,
                                    entryType = FoodDrinkType.valueOf(selectedEntryType),
                                    name = trimmedName,
                                    calories = calorieValue,
                                    proteinGrams = proteinValue
                                )
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save changes")
            }
        }
    }
}

@Composable
private fun EditWorkoutDialog(
    entry: WorkoutEntry,
    onDismiss: () -> Unit,
    onSave: (WorkoutEntry) -> Unit
) {
    var workoutDate by remember(entry.id) { mutableStateOf(entry.entryDate) }
    var workoutType by remember(entry.id) { mutableStateOf(entry.workoutType.name) }
    var workoutIntensity by remember(entry.id) { mutableStateOf(entry.intensity.name) }
    var workoutMinutes by remember(entry.id) { mutableStateOf(entry.minutes.toString()) }
    var workoutTypeMenuExpanded by remember(entry.id) { mutableStateOf(false) }
    var workoutIntensityMenuExpanded by remember(entry.id) { mutableStateOf(false) }
    var errorText by remember(entry.id) { mutableStateOf<String?>(null) }

    AppDialogCard(
        title = "Edit workout",
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
                            onSave(
                                entry.copy(
                                    entryDate = workoutDate,
                                    workoutType = WorkoutType.valueOf(workoutType),
                                    intensity = WorkoutIntensity.valueOf(workoutIntensity),
                                    minutes = minutesValue
                                )
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save changes")
            }
        }
    }
}

@Composable
private fun EditWeightDialog(
    entry: WeightEntry,
    onDismiss: () -> Unit,
    onSave: (WeightEntry) -> Unit
) {
    var weightDate by remember(entry.id) { mutableStateOf(entry.entryDate) }
    var weightKg by remember(entry.id) { mutableStateOf(formatSmartNumber(entry.weightKg)) }
    var errorText by remember(entry.id) { mutableStateOf<String?>(null) }

    AppDialogCard(
        title = "Edit weight",
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
                            onSave(
                                entry.copy(
                                    entryDate = weightDate,
                                    weightKg = weightValue
                                )
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save changes")
            }
        }
    }
}

@Composable
private fun EditWaistDialog(
    entry: WaistEntry,
    onDismiss: () -> Unit,
    onSave: (WaistEntry) -> Unit
) {
    var waistDate by remember(entry.id) { mutableStateOf(entry.entryDate) }
    var waistCm by remember(entry.id) { mutableStateOf(formatSmartNumber(entry.waistCm)) }
    var errorText by remember(entry.id) { mutableStateOf<String?>(null) }

    AppDialogCard(
        title = "Edit waist",
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
                            onSave(
                                entry.copy(
                                    entryDate = waistDate,
                                    waistCm = waistValue
                                )
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save changes")
            }
        }
    }
}

@Composable
private fun EditStepsDialog(
    entry: DailyStepsEntry,
    onDismiss: () -> Unit,
    onSave: (DailyStepsEntry) -> Unit
) {
    var stepsDate by remember(entry.entryDate) { mutableStateOf(entry.entryDate) }
    var stepsValue by remember(entry.entryDate) { mutableStateOf(entry.steps.toString()) }
    var errorText by remember(entry.entryDate) { mutableStateOf<String?>(null) }

    AppDialogCard(
        title = "Edit steps",
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
                            onSave(
                                DailyStepsEntry(
                                    entryDate = stepsDate,
                                    steps = parsedSteps
                                )
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save changes")
            }
        }
    }
}

private fun formatSmartNumber(value: Double): String {
    val rounded = value.roundToInt().toDouble()
    return if (abs(value - rounded) < 0.0001) {
        rounded.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}