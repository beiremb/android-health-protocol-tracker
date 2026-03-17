package com.example.protocoltracker.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
import com.example.protocoltracker.domain.metrics.DailyMetrics
import com.example.protocoltracker.domain.metrics.MetricChange
import com.example.protocoltracker.domain.metrics.MetricsCalculator
import com.example.protocoltracker.domain.metrics.TrendDirection
import com.example.protocoltracker.domain.metrics.WeeklyMetrics
import com.example.protocoltracker.ui.common.AppPageTitle
import com.example.protocoltracker.ui.common.CompactMetricCard
import com.example.protocoltracker.ui.common.SectionCard
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private enum class ChartMode {
    DAILY,
    WEEKLY
}

private enum class ProgressMetric {
    WEIGHT,
    CALORIES,
    STEPS,
    FASTING,
    WORKOUT,
    DRINK
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
    val today = LocalDate.now().toString()

    val weightEntries by remember { repository.observeWeightEntries() }.collectAsState(initial = emptyList())
    val foodEntries by remember { repository.observeFoodDrinkEntries() }.collectAsState(initial = emptyList())
    val workoutEntries by remember { repository.observeWorkoutEntries() }.collectAsState(initial = emptyList())
    val waistEntries by remember { repository.observeWaistEntries() }.collectAsState(initial = emptyList())
    val stepsEntries by remember { repository.observeDailySteps() }.collectAsState(initial = emptyList())
    val milestones by remember { repository.observeMilestones() }.collectAsState(initial = emptyList())

    var chartMode by rememberSaveable { mutableStateOf(ChartMode.WEEKLY) }
    var selectedMetric by rememberSaveable { mutableStateOf(ProgressMetric.WEIGHT) }
    var showMilestoneDialog by rememberSaveable { mutableStateOf(false) }
    var editingMilestoneId by rememberSaveable { mutableStateOf<Long?>(null) }

    fun closeMilestoneDialog() {
        showMilestoneDialog = false
        editingMilestoneId = null
    }
    val dailyMetrics = remember(today, foodEntries, workoutEntries, weightEntries, waistEntries, stepsEntries) {
        MetricsCalculator.calculateDailyMetrics(
            date = today,
            foodEntries = foodEntries,
            workoutEntries = workoutEntries,
            weightEntries = weightEntries,
            waistEntries = waistEntries,
            dailyStepsEntries = stepsEntries
        )
    }

    val weeklyMetrics = remember(foodEntries, workoutEntries, weightEntries, waistEntries, stepsEntries) {
        buildWeeklyMetricsSeries(
            foodEntries = foodEntries,
            workoutEntries = workoutEntries,
            weightEntries = weightEntries,
            waistEntries = waistEntries,
            stepsEntries = stepsEntries
        )
    }

    val currentWeek = weeklyMetrics.lastOrNull()
    val previousWeek = weeklyMetrics.getOrNull(weeklyMetrics.lastIndex - 1)
    val weeklyChanges = remember(currentWeek, previousWeek) {
        currentWeek?.let { MetricsCalculator.calculateWeeklyMetricChanges(it, previousWeek) }
    }
    val currentFastingHours = remember(foodEntries) {
        MetricsCalculator.calculateCurrentFastingHours(foodEntries)
    }

    val chartPoints = remember(
        selectedMetric,
        chartMode,
        weeklyMetrics,
        foodEntries,
        workoutEntries,
        weightEntries,
        waistEntries,
        stepsEntries
    ) {
        when (chartMode) {
            ChartMode.DAILY -> buildDailyMetricPoints(
                metric = selectedMetric,
                foodEntries = foodEntries,
                workoutEntries = workoutEntries,
                weightEntries = weightEntries,
                waistEntries = waistEntries,
                stepsEntries = stepsEntries
            )

            ChartMode.WEEKLY -> buildWeeklyMetricPoints(
                metric = selectedMetric,
                weeklyMetrics = weeklyMetrics
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AppPageTitle(
                title = "Progress",
                subtitle = "Overview first, trends below."
            )
        }

        item {
            SectionCard(title = "Overview") {
                MetricRow(
                    title1 = "Weight",
                    primary1 = currentWeek?.averageWeightKg?.let { "${formatDouble(it)} kg" } ?: "—",
                    secondary1 = dailyMetrics.latestWeightKg?.let { "Today ${formatDouble(it)} kg" } ?: "Today —",
                    change1 = weeklyChanges?.weight,
                    title2 = "Waist",
                    primary2 = currentWeek?.latestWaistCm?.let { "${formatDouble(it)} cm" } ?: "—",
                    secondary2 = "Latest weekly measure",
                    change2 = weeklyChanges?.waist
                )

                MetricRow(
                    title1 = "Calories",
                    primary1 = currentWeek?.let { formatDouble(it.averageDailyCalories) } ?: "—",
                    secondary1 = "Today ${dailyMetrics.totalCalories}",
                    change1 = weeklyChanges?.calories,
                    title2 = "Steps",
                    primary2 = currentWeek?.let { formatDouble(it.averageDailySteps) } ?: "—",
                    secondary2 = "Today ${dailyMetrics.steps}",
                    change2 = weeklyChanges?.steps
                )

                MetricRow(
                    title1 = "Hours fasted",
                    primary1 = currentWeek?.let { formatDouble(it.averageDailyFastingHours) } ?: "—",
                    secondary1 = "Now ${formatDouble(currentFastingHours)} h",
                    change1 = weeklyChanges?.fastingHours,
                    title2 = "Workout",
                    primary2 = currentWeek?.let { "${it.totalWorkoutMinutes} min" } ?: "—",
                    secondary2 = "Today ${dailyMetrics.workoutMinutes} min",
                    change2 = weeklyChanges?.workoutMinutes
                )

                CompactMetricCard(
                    title = "Drink calories",
                    primary = currentWeek?.let { "${it.drinkCalories}" } ?: "—",
                    secondary = "Today ${dailyMetrics.drinkCalories}",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            SectionCard(title = "Chart") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { chartMode = ChartMode.DAILY },
                        modifier = Modifier.weight(1f),
                        enabled = chartMode != ChartMode.DAILY
                    ) {
                        Text("Daily")
                    }

                    Button(
                        onClick = { chartMode = ChartMode.WEEKLY },
                        modifier = Modifier.weight(1f),
                        enabled = chartMode != ChartMode.WEEKLY
                    ) {
                        Text("Weekly")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricChip("Weight", selectedMetric == ProgressMetric.WEIGHT) {
                        selectedMetric = ProgressMetric.WEIGHT
                    }
                    MetricChip("Calories", selectedMetric == ProgressMetric.CALORIES) {
                        selectedMetric = ProgressMetric.CALORIES
                    }
                    MetricChip("Steps", selectedMetric == ProgressMetric.STEPS) {
                        selectedMetric = ProgressMetric.STEPS
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricChip("Fasting", selectedMetric == ProgressMetric.FASTING) {
                        selectedMetric = ProgressMetric.FASTING
                    }
                    MetricChip("Workout", selectedMetric == ProgressMetric.WORKOUT) {
                        selectedMetric = ProgressMetric.WORKOUT
                    }
                    MetricChip("Drink", selectedMetric == ProgressMetric.DRINK) {
                        selectedMetric = ProgressMetric.DRINK
                    }
                }

                MetricChartCard(
                    title = chartTitle(selectedMetric, chartMode),
                    points = chartPoints
                )
            }
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
                SectionCard {
                    Text("No milestones yet.")
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
                SectionCard {
                    Text("No weekly history yet.")
                }
            }
        } else {
            items(
                items = weeklyMetrics.sortedByDescending { it.weekStartDate },
                key = { it.weekStartDate }
            ) { weekly ->
                WeeklyHistoryCard(weekly)
            }
        }
    }

    if (showMilestoneDialog) {
        val editingMilestone = milestones.firstOrNull { it.id == editingMilestoneId }

        SimpleMilestoneDialog(
            initialMilestone = editingMilestone,
            onDismiss = ::closeMilestoneDialog,
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

                    closeMilestoneDialog()
                }
            },
            onDelete = editingMilestone?.let {
                {
                    scope.launch {
                        repository.deleteMilestone(it)
                        closeMilestoneDialog()
                    }
                }
            }
        )
    }
}

@Composable
private fun MetricRow(
    title1: String,
    primary1: String,
    secondary1: String,
    change1: MetricChange?,
    title2: String,
    primary2: String,
    secondary2: String,
    change2: MetricChange?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CompactMetricCard(
            title = title1,
            primary = primary1,
            secondary = secondary1,
            delta = change1?.let(::formatDelta),
            deltaColor = change1?.let(::deltaColor),
            modifier = Modifier.weight(1f)
        )
        CompactMetricCard(
            title = title2,
            primary = primary2,
            secondary = secondary2,
            delta = change2?.let(::formatDelta),
            deltaColor = change2?.let(::deltaColor),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RowScope.MetricChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = Modifier.weight(1f)
        ) {
            Text(label)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.weight(1f)
        ) {
            Text(label)
        }
    }
}

@Composable
private fun MetricChartCard(
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
                Text("No data yet.")
                return@Column
            }

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

@Composable
private fun WeeklyHistoryCard(
    weekly: WeeklyMetrics
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = formatWeekRange(weekly.weekStartDate, weekly.weekEndDate),
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeeklyMiniStat(
                    label = "Weight",
                    value = weekly.averageWeightKg?.let(::formatDouble) ?: "—",
                    modifier = Modifier.weight(1f)
                )
                WeeklyMiniStat(
                    label = "Waist",
                    value = weekly.latestWaistCm?.let(::formatDouble) ?: "—",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeeklyMiniStat(
                    label = "Calories",
                    value = formatDouble(weekly.averageDailyCalories),
                    modifier = Modifier.weight(1f)
                )
                WeeklyMiniStat(
                    label = "Steps",
                    value = formatDouble(weekly.averageDailySteps),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeeklyMiniStat(
                    label = "Fasting",
                    value = formatDouble(weekly.averageDailyFastingHours),
                    modifier = Modifier.weight(1f)
                )
                WeeklyMiniStat(
                    label = "Workout",
                    value = "${weekly.totalWorkoutMinutes}",
                    modifier = Modifier.weight(1f)
                )
            }

            WeeklyMiniStat(
                label = "Drink calories",
                value = "${weekly.drinkCalories}",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WeeklyMiniStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium
            )
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
    var weight by rememberSaveable(initialMilestone?.id) {
        mutableStateOf(initialMilestone?.let { formatDouble(it.targetWeightKg) } ?: "")
    }
    var date by rememberSaveable(initialMilestone?.id) {
        mutableStateOf(initialMilestone?.targetDate ?: "")
    }
    var reward by rememberSaveable(initialMilestone?.id) {
        mutableStateOf(initialMilestone?.rewardText ?: "")
    }
    var error by rememberSaveable(initialMilestone?.id) {
        mutableStateOf<String?>(null)
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

private fun buildDailyMetricPoints(
    metric: ProgressMetric,
    foodEntries: List<FoodDrinkEntry>,
    workoutEntries: List<WorkoutEntry>,
    weightEntries: List<WeightEntry>,
    waistEntries: List<WaistEntry>,
    stepsEntries: List<DailyStepsEntry>
): List<ChartPoint> {
    val dates = allTrackedDates(foodEntries, workoutEntries, weightEntries, waistEntries, stepsEntries)

    return dates.mapNotNull { date ->
        val daily = MetricsCalculator.calculateDailyMetrics(
            date = date,
            foodEntries = foodEntries,
            workoutEntries = workoutEntries,
            weightEntries = weightEntries,
            waistEntries = waistEntries,
            dailyStepsEntries = stepsEntries
        )

        metricValue(metric, daily)?.let { value ->
            ChartPoint(label = date, value = value)
        }
    }
}

private fun buildWeeklyMetricPoints(
    metric: ProgressMetric,
    weeklyMetrics: List<WeeklyMetrics>
): List<ChartPoint> {
    return weeklyMetrics.mapNotNull { weekly ->
        weeklyMetricValue(metric, weekly)?.let { value ->
            ChartPoint(label = weekly.weekStartDate, value = value)
        }
    }
}

private fun buildWeeklyMetricsSeries(
    foodEntries: List<FoodDrinkEntry>,
    workoutEntries: List<WorkoutEntry>,
    weightEntries: List<WeightEntry>,
    waistEntries: List<WaistEntry>,
    stepsEntries: List<DailyStepsEntry>
): List<WeeklyMetrics> {
    val dates = allTrackedDates(foodEntries, workoutEntries, weightEntries, waistEntries, stepsEntries)
        .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }

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

private fun allTrackedDates(
    foodEntries: List<FoodDrinkEntry>,
    workoutEntries: List<WorkoutEntry>,
    weightEntries: List<WeightEntry>,
    waistEntries: List<WaistEntry>,
    stepsEntries: List<DailyStepsEntry>
): List<String> {
    return buildSet {
        addAll(foodEntries.map { it.entryDate })
        addAll(workoutEntries.map { it.entryDate })
        addAll(weightEntries.map { it.entryDate })
        addAll(waistEntries.map { it.entryDate })
        addAll(stepsEntries.map { it.entryDate })
    }.toList().sorted()
}

private fun metricValue(
    metric: ProgressMetric,
    daily: DailyMetrics
): Double? {
    return when (metric) {
        ProgressMetric.WEIGHT -> daily.latestWeightKg
        ProgressMetric.CALORIES -> daily.totalCalories.toDouble()
        ProgressMetric.STEPS -> daily.steps.toDouble()
        ProgressMetric.FASTING -> daily.fastingHours
        ProgressMetric.WORKOUT -> daily.workoutMinutes.toDouble()
        ProgressMetric.DRINK -> daily.drinkCalories.toDouble()
    }
}

private fun weeklyMetricValue(
    metric: ProgressMetric,
    weekly: WeeklyMetrics
): Double? {
    return when (metric) {
        ProgressMetric.WEIGHT -> weekly.averageWeightKg
        ProgressMetric.CALORIES -> weekly.averageDailyCalories
        ProgressMetric.STEPS -> weekly.averageDailySteps
        ProgressMetric.FASTING -> weekly.averageDailyFastingHours
        ProgressMetric.WORKOUT -> weekly.totalWorkoutMinutes.toDouble()
        ProgressMetric.DRINK -> weekly.drinkCalories.toDouble()
    }
}

private fun chartTitle(
    metric: ProgressMetric,
    mode: ChartMode
): String {
    val metricLabel = when (metric) {
        ProgressMetric.WEIGHT -> "Weight"
        ProgressMetric.CALORIES -> "Calories"
        ProgressMetric.STEPS -> "Steps"
        ProgressMetric.FASTING -> "Fasting hours"
        ProgressMetric.WORKOUT -> "Workout minutes"
        ProgressMetric.DRINK -> "Drink calories"
    }

    val modeLabel = when (mode) {
        ChartMode.DAILY -> "daily"
        ChartMode.WEEKLY -> "weekly"
    }

    return "$metricLabel — $modeLabel"
}

private fun formatDelta(change: MetricChange): String? {
    val delta = change.delta ?: return null
    if (delta == 0.0) return "0.0"

    return when (change.direction) {
        TrendDirection.BETTER -> "↑ ${formatDouble(abs(delta))}"
        TrendDirection.WORSE -> "↓ ${formatDouble(abs(delta))}"
        TrendDirection.SAME -> "0.0"
        TrendDirection.NONE -> null
    }
}

private fun deltaColor(change: MetricChange): Color? =
    when (change.direction) {
        TrendDirection.BETTER -> Color(0xFF2E7D32)
        TrendDirection.WORSE -> Color(0xFFC62828)
        TrendDirection.SAME,
        TrendDirection.NONE -> null
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