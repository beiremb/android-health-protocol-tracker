package com.example.protocoltracker.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.protocoltracker.ProtocolTrackerApp
import com.example.protocoltracker.data.local.entity.DailyStepsEntry
import com.example.protocoltracker.data.local.entity.FoodDrinkEntry
import com.example.protocoltracker.data.local.entity.MilestoneEntry
import com.example.protocoltracker.data.local.entity.WaistEntry
import com.example.protocoltracker.data.local.entity.WeightEntry
import com.example.protocoltracker.data.local.entity.WorkoutEntry
import com.example.protocoltracker.domain.metrics.MetricsCalculator
import com.example.protocoltracker.domain.metrics.WeeklyMetrics
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class WeightChartMode {
    DAILY,
    WEEKLY
}

private data class ChartPoint(
    val label: String,
    val value: Double
)

@Composable
fun ProgressScreen() {
    val app = LocalContext.current.applicationContext as ProtocolTrackerApp
    val repository = app.repository
    val scope = rememberCoroutineScope()

    val weightEntries by remember { repository.observeWeightEntries() }.collectAsState(initial = emptyList())
    val foodEntries by remember { repository.observeFoodDrinkEntries() }.collectAsState(initial = emptyList())
    val workoutEntries by remember { repository.observeWorkoutEntries() }.collectAsState(initial = emptyList())
    val waistEntries by remember { repository.observeWaistEntries() }.collectAsState(initial = emptyList())
    val stepsEntries by remember { repository.observeDailySteps() }.collectAsState(initial = emptyList())
    val milestones by remember { repository.observeMilestones() }.collectAsState(initial = emptyList())

    var chartMode by rememberSaveable { mutableStateOf(WeightChartMode.DAILY) }

    val dailyWeightPoints = remember(weightEntries) { buildDailyWeightPoints(weightEntries) }
    val weeklyMetrics = remember(foodEntries, workoutEntries, weightEntries, waistEntries, stepsEntries) {
        buildWeeklyMetricsSeries(
            foodEntries = foodEntries,
            workoutEntries = workoutEntries,
            weightEntries = weightEntries,
            waistEntries = waistEntries,
            stepsEntries = stepsEntries
        )
    }
    val weeklyWeightPoints = remember(weeklyMetrics) {
        weeklyMetrics.mapNotNull { weekly ->
            weekly.averageWeightKg?.let {
                ChartPoint(
                    label = weekly.weekStartDate,
                    value = it
                )
            }
        }
    }

    var showMilestoneDialog by rememberSaveable { mutableStateOf(false) }
    var editingMilestoneId by rememberSaveable { mutableStateOf<Long?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Progress",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { chartMode = WeightChartMode.DAILY },
                    modifier = Modifier.weight(1f),
                    enabled = chartMode != WeightChartMode.DAILY
                ) {
                    Text("Daily logs")
                }

                Button(
                    onClick = { chartMode = WeightChartMode.WEEKLY },
                    modifier = Modifier.weight(1f),
                    enabled = chartMode != WeightChartMode.WEEKLY
                ) {
                    Text("Weekly averages")
                }
            }
        }

        item {
            WeightChartCard(
                title = if (chartMode == WeightChartMode.DAILY) {
                    "Weight — daily logs"
                } else {
                    "Weight — weekly averages"
                },
                points = if (chartMode == WeightChartMode.DAILY) {
                    dailyWeightPoints
                } else {
                    weeklyWeightPoints
                }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Milestones",
                    style = MaterialTheme.typography.titleLarge
                )
                OutlinedButton(
                    onClick = {
                        editingMilestoneId = null
                        showMilestoneDialog = true
                    }
                ) {
                    Text("Add")
                }
            }
        }

        if (milestones.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No milestones yet.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(milestones, key = { it.id }) { milestone ->
                MilestoneRow(
                    milestone = milestone,
                    onClick = {
                        editingMilestoneId = milestone.id
                        showMilestoneDialog = true
                    }
                )
            }
        }

        item {
            Text(
                text = "Weekly history",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (weeklyMetrics.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No weekly history yet.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(weeklyMetrics.sortedByDescending { it.weekStartDate }, key = { it.weekStartDate }) { weekly ->
                WeeklyHistoryCard(weekly)
            }
        }
    }

    if (showMilestoneDialog) {
        val editingMilestone = milestones.firstOrNull { it.id == editingMilestoneId }

        SimpleMilestoneDialog(
            initialMilestone = editingMilestone,
            onDismiss = { showMilestoneDialog = false },
            onSave = { weight, date, reward ->
                scope.launch {
                    val entry = MilestoneEntry(
                        id = editingMilestone?.id ?: 0L,
                        targetWeightKg = weight,
                        targetDate = date,
                        rewardText = reward,
                        sortOrder = editingMilestone?.sortOrder
                            ?: ((milestones.maxOfOrNull { it.sortOrder } ?: 0) + 1)
                    )

                    if (editingMilestone == null) {
                        repository.insertMilestone(entry)
                    } else {
                        repository.updateMilestone(entry)
                    }

                    showMilestoneDialog = false
                }
            },
            onDelete = if (editingMilestone != null) {
                {
                    scope.launch {
                        repository.deleteMilestone(editingMilestone)
                        showMilestoneDialog = false
                    }
                }
            } else {
                null
            }
        )
    }
}

@Composable
private fun WeightChartCard(
    title: String,
    points: List<ChartPoint>
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            if (points.isEmpty()) {
                Text("No weight data yet.")
            } else {
                val lineColor = MaterialTheme.colorScheme.primary
                val pointColor = MaterialTheme.colorScheme.primary
                val axisColor = MaterialTheme.colorScheme.outline

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    val padding = 24f
                    val width = size.width - padding * 2
                    val height = size.height - padding * 2

                    drawLine(
                        color = axisColor,
                        start = Offset(padding, size.height - padding),
                        end = Offset(size.width - padding, size.height - padding),
                        strokeWidth = 2f
                    )

                    val minValue = points.minOf { it.value }
                    val maxValue = points.maxOf { it.value }
                    val valueRange = if (maxValue - minValue == 0.0) 1.0 else maxValue - minValue

                    val offsets = points.mapIndexed { index, point ->
                        val x = if (points.size == 1) {
                            padding + width / 2f
                        } else {
                            padding + (index.toFloat() / points.lastIndex.coerceAtLeast(1)) * width
                        }

                        val normalized = ((point.value - minValue) / valueRange).toFloat()
                        val y = (size.height - padding) - (normalized * height)
                        Offset(x, y)
                    }

                    offsets.zipWithNext().forEach { (start, end) ->
                        drawLine(
                            color = lineColor,
                            start = start,
                            end = end,
                            strokeWidth = 6f,
                            cap = StrokeCap.Round
                        )
                    }

                    offsets.forEach { offset ->
                        drawCircle(
                            color = pointColor,
                            radius = 7f,
                            center = offset
                        )
                    }
                }

                points.takeLast(5).forEach { point ->
                    Text("${point.label}: ${formatDouble(point.value)}")
                }
            }
        }
    }
}

@Composable
private fun WeeklyHistoryCard(
    weekly: WeeklyMetrics
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = formatWeekRange(weekly.weekStartDate, weekly.weekEndDate),
                style = MaterialTheme.typography.titleMedium
            )
            Text("Average weight: ${weekly.averageWeightKg?.let { formatDouble(it) } ?: "No data"}")
            Text("Average daily calories: ${formatDouble(weekly.averageDailyCalories)}")
            Text("Waist: ${weekly.latestWaistCm?.let { formatDouble(it) } ?: "No data"}")
            Text("Average daily steps: ${formatDouble(weekly.averageDailySteps)}")
            Text("Average daily fasting hours: ${formatDouble(weekly.averageDailyFastingHours)}")
            Text("Total workout minutes: ${weekly.totalWorkoutMinutes}")
            Text("Drink calories: ${weekly.drinkCalories}")
        }
    }
}

@Composable
private fun MilestoneRow(
    milestone: MilestoneEntry,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${formatDouble(milestone.targetWeightKg)} kg",
                style = MaterialTheme.typography.titleMedium
            )
            Text("Date: ${milestone.targetDate}")
            Text("Reward: ${milestone.rewardText.ifBlank { "—" }}")
        }
    }
}

@Composable
private fun SimpleMilestoneDialog(
    initialMilestone: MilestoneEntry?,
    onDismiss: () -> Unit,
    onSave: (Double, String, String) -> Unit,
    onDelete: (() -> Unit)?
) {
    var weight by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf("") }
    var reward by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(initialMilestone?.id) {
        weight = initialMilestone?.let { formatDouble(it.targetWeightKg) } ?: ""
        date = initialMilestone?.targetDate ?: ""
        reward = initialMilestone?.rewardText ?: ""
        error = null
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (initialMilestone == null) "Add milestone" else "Edit milestone",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Target weight (kg)") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Date (yyyy-mm-dd)") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = reward,
                    onValueChange = { reward = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Reward") },
                    singleLine = true
                )

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    onClick = {
                        val parsedWeight = weight.toDoubleOrNull()
                        when {
                            parsedWeight == null -> error = "Target weight must be a number."
                            date.isBlank() -> error = "Enter a date."
                            else -> onSave(parsedWeight, date, reward)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }

                onDelete?.let {
                    OutlinedButton(
                        onClick = it,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete")
                    }
                }

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

private fun buildDailyWeightPoints(
    weightEntries: List<WeightEntry>
): List<ChartPoint> {
    return weightEntries
        .groupBy { it.entryDate }
        .mapNotNull { (date, entries) ->
            val latest = entries.maxByOrNull { it.entryTime }
            latest?.let {
                ChartPoint(
                    label = date,
                    value = it.weightKg
                )
            }
        }
        .sortedBy { it.label }
}

private fun buildWeeklyMetricsSeries(
    foodEntries: List<FoodDrinkEntry>,
    workoutEntries: List<WorkoutEntry>,
    weightEntries: List<WeightEntry>,
    waistEntries: List<WaistEntry>,
    stepsEntries: List<DailyStepsEntry>
): List<WeeklyMetrics> {
    val dates = buildList {
        addAll(foodEntries.map { it.entryDate })
        addAll(workoutEntries.map { it.entryDate })
        addAll(weightEntries.map { it.entryDate })
        addAll(waistEntries.map { it.entryDate })
        addAll(stepsEntries.map { it.entryDate })
    }.mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }

    if (dates.isEmpty()) return emptyList()

    val firstWeekStart = mondayOf(dates.minOrNull()!!)
    val latestDataDate = dates.maxOrNull()!!
    val currentWeekStart = mondayOf(LocalDate.now())
    val finalSourceDate = if (latestDataDate.isAfter(currentWeekStart)) latestDataDate else currentWeekStart
    val lastWeekStart = mondayOf(finalSourceDate)

    val weeks = mutableListOf<WeeklyMetrics>()
    var current = firstWeekStart

    while (!current.isAfter(lastWeekStart)) {
        weeks += MetricsCalculator.calculateWeeklyMetrics(
            weekStartDate = current.toString(),
            foodEntries = foodEntries,
            workoutEntries = workoutEntries,
            weightEntries = weightEntries,
            waistEntries = waistEntries,
            dailyStepsEntries = stepsEntries
        )
        current = current.plusDays(7)
    }

    return weeks
}

private fun mondayOf(date: LocalDate): LocalDate =
    date.minusDays((date.dayOfWeek.value - 1).toLong())

private fun formatDouble(value: Double): String =
    String.format(Locale.US, "%.1f", value)

private fun formatWeekRange(start: String, end: String): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM", Locale.US)
    val startDate = runCatching { LocalDate.parse(start) }.getOrNull()
    val endDate = runCatching { LocalDate.parse(end) }.getOrNull()

    return if (startDate != null && endDate != null) {
        "${startDate.format(formatter)} - ${endDate.format(formatter)}"
    } else {
        "$start - $end"
    }
}