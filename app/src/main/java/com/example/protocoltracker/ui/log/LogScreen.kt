package com.example.protocoltracker.ui.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
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
import androidx.compose.foundation.text.KeyboardOptions
import com.example.protocoltracker.ProtocolTrackerApp
import com.example.protocoltracker.data.local.entity.FoodDrinkEntry
import com.example.protocoltracker.data.local.entity.FoodDrinkType
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

private data class QuickTemplate(
    val label: String,
    val type: FoodDrinkType,
    val name: String,
    val calories: Int,
    val proteinGrams: Int? = null
)

private val quickTemplates = listOf(
    QuickTemplate("Meal", FoodDrinkType.MEAL, "Meal", 600, 40),
    QuickTemplate("Snack", FoodDrinkType.SNACK, "Snack", 250, 20),
    QuickTemplate("Alcohol", FoodDrinkType.ALCOHOL, "Alcohol", 200, null),
    QuickTemplate("Other", FoodDrinkType.OTHER, "Other", 120, null)
)

private fun allTimeSlots(): List<String> =
    (0..23).flatMap { hour ->
        listOf("%02d:00".format(hour), "%02d:30".format(hour))
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
        FoodDrinkType.ALCOHOL -> "Alcohol"
        FoodDrinkType.OTHER -> "Other"
    }

@Composable
fun LogScreen() {
    val app = LocalContext.current.applicationContext as ProtocolTrackerApp
    val repository = app.repository
    val scope = rememberCoroutineScope()

    var entryDate by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var timeSlot by rememberSaveable { mutableStateOf(defaultTimeSlot()) }
    var entryType by rememberSaveable { mutableStateOf(FoodDrinkType.MEAL.name) }
    var name by rememberSaveable { mutableStateOf("") }
    var calories by rememberSaveable { mutableStateOf("") }
    var proteinGrams by rememberSaveable { mutableStateOf("") }
    var regret by rememberSaveable { mutableStateOf(false) }
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }

    var timeMenuExpanded by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }

    val entriesFlow = remember(entryDate) {
        repository.observeFoodDrinkEntriesByDate(entryDate)
    }
    val entries by entriesFlow.collectAsState(initial = emptyList())

    val totalCalories = entries.sumOf { it.calories }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Food / Drink Log",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Quick fill",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickTemplates.take(2).forEach { template ->
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    entryType = template.type.name
                                    name = template.name
                                    calories = template.calories.toString()
                                    proteinGrams = template.proteinGrams?.toString() ?: ""
                                }
                            ) {
                                Text(template.label)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickTemplates.drop(2).forEach { template ->
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    entryType = template.type.name
                                    name = template.name
                                    calories = template.calories.toString()
                                    proteinGrams = template.proteinGrams?.toString() ?: ""
                                }
                            ) {
                                Text(template.label)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Add entry",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = entryDate,
                        onValueChange = { entryDate = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Date (yyyy-MM-dd)") },
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Checkbox(
                            checked = regret,
                            onCheckedChange = { regret = it }
                        )
                        Text(
                            text = "Regret?",
                            modifier = Modifier.padding(top = 14.dp)
                        )
                    }

                    errorText?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
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
                                                entryType = FoodDrinkType.valueOf(entryType),
                                                name = trimmedName,
                                                calories = calorieValue,
                                                proteinGrams = proteinValue,
                                                regret = regret,
                                                templateId = null
                                            )
                                        )

                                        name = ""
                                        calories = ""
                                        proteinGrams = ""
                                        regret = false
                                        errorText = null
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save entry")
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
                    Text(
                        text = "Entries for $entryDate",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("Total calories: $totalCalories")

                    if (entries.isEmpty()) {
                        Text("No entries yet.")
                    }
                }
            }
        }

        items(entries, key = { it.id }) { entry ->
            EntryRow(entry = entry)
        }
    }
}

@Composable
private fun EntryRow(entry: FoodDrinkEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "${entry.timeSlot} • ${entry.entryType.label()}",
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = entry.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text("Calories: ${entry.calories}")

            entry.proteinGrams?.let {
                Text("Protein: $it g")
            }

            if (entry.regret) {
                Text(
                    text = "Regret: yes",
                    color = MaterialTheme.colorScheme.error
                )
            }

            HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
        }
    }
}