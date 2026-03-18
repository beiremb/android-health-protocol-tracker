package com.example.protocoltracker.ui.progress

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.ButtonDefaults
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
import com.example.protocoltracker.data.settings.AppSettings
import com.example.protocoltracker.domain.metrics.DailyMetrics
import com.example.protocoltracker.domain.metrics.MetricChange
import com.example.protocoltracker.domain.metrics.MetricsCalculator
import com.example.protocoltracker.domain.metrics.TrendDirection
import com.example.protocoltracker.domain.metrics.WeeklyMetrics
import com.example.protocoltracker.ui.common.AppPageTitle
import com.example.protocoltracker.ui.common.CompactMetricCard
import com.example.protocoltracker.ui.common.SectionCard
import com.example.protocoltracker.ui.theme.BrandGray
import com.example.protocoltracker.ui.theme.BrandOrange
import com.example.protocoltracker.ui.theme.BrandTeal
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

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
    val settingsRepository = app.settingsRepository
    val scope = rememberCoroutineScope()
    val today = LocalDate.now().toString()

    val settings by settingsRepository.settingsFlow.collectAsState(initial = AppSettings())

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

    val fixedWeightChartMin = settings.weightChartMinKg
    val fixedWeightChartMax = if (settings.weightChartMaxKg > settings.weightChartMinKg) {
        settings.weightChartMaxKg
    } else {
        settings.weightChartMinKg + 1.0
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
                    primary1 = currentWeek?.averageWeightKg?.let { "${formatSmartNumber(it)} kg" } ?: "—",
                    secondary1 = dailyMetrics.latestWeightKg?.let { "Today ${formatSmartNumber(it)} kg" } ?: "Today —",
                    change1 = weeklyChanges?.weight,
                    change1WholeOnly = false,
                    title2 = "Waist",
                    primary2 = currentWeek?.latestWaistCm?.let { "${formatSmartNumber(it)} cm" } ?: "—",
                    secondary2 = "Latest weekly measure",
                    change2 = weeklyChanges?.waist,
                    change2WholeOnly = false
                )

                MetricRow(
                    title1 = "Calories",
                    primary1 = currentWeek?.let { formatWholeDouble(it.averageDailyCalories) } ?: "—",
                    secondary1 = "Today ${dailyMetrics.totalCalories}",
                    change1 = weeklyChanges?.calories,
                    change1WholeOnly = true,
                    title2 = "Steps",
                    primary2 = currentWeek?.let { formatWholeDouble(it.averageDailySteps) } ?: "—",
                    secondary2 = "Today ${dailyMetrics.steps}",
                    change2 = weeklyChanges?.steps,
                    change2WholeOnly = true
                )

                MetricRow(
                    title1 = "Hours fasted",
                    primary1 = currentWeek?.let { formatSmartNumber(it.averageDailyFastingHours) } ?: "—",
                    secondary1 = "Now ${formatSmartNumber(currentFastingHours)} h",
                    change1 = weeklyChanges?.fastingHours,
                    change1WholeOnly = false,
                    title2 = "Workout",
                    primary2 = currentWeek?.let { "${it.totalWorkoutMinutes} min" } ?: "—",
                    secondary2 = "Today ${dailyMetrics.workoutMinutes} min",
                    change2 = weeklyChanges?.workoutMinutes,
                    change2WholeOnly = true
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
                        enabled = chartMode != ChartMode.DAILY,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandTeal,
                            contentColor = Color.White,
                            disabledContainerColor = BrandTeal.copy(alpha = 0.18f),
                            disabledContentColor = BrandTeal
                        )
                    ) {
                        Text("Daily")
                    }

                    Button(
                        onClick = { chartMode = ChartMode.WEEKLY },
                        modifier = Modifier.weight(1f),
                        enabled = chartMode != ChartMode.WEEKLY,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandTeal,
                            contentColor = Color.White,
                            disabledContainerColor = BrandTeal.copy(alpha = 0.18f),
                            disabledContentColor = BrandTeal
                        )
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
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricChip("Steps", selectedMetric == ProgressMetric.STEPS) {
                        selectedMetric = ProgressMetric.STEPS
                    }
                    MetricChip("Fasting", selectedMetric == ProgressMetric.FASTING) {
                        selectedMetric = ProgressMetric.FASTING
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricChip("Workout", selectedMetric == ProgressMetric.WORKOUT) {
                        selectedMetric = ProgressMetric.WORKOUT
                    }
                    MetricChip("Drink", selectedMetric == ProgressMetric.DRINK) {
                        selectedMetric = ProgressMetric.DRINK
                    }
                }

                MetricChartCard(
                    title = chartTitle(selectedMetric, chartMode),
                    points = chartPoints,
                    valueFormatter = { value -> formatMetricValue(selectedMetric, value) },
                    fixedMinValue = if (selectedMetric == ProgressMetric.WEIGHT) fixedWeightChartMin else null,
                    fixedMaxValue = if (selectedMetric == ProgressMetric.WEIGHT) fixedWeightChartMax else null
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
                    },
                    border = BorderStroke(1.dp, BrandOrange),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = BrandOrange
                    )
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
    change1WholeOnly: Boolean,
    title2: String,
    primary2: String,
    secondary2: String,
    change2: MetricChange?,
    change2WholeOnly: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CompactMetricCard(
            title = title1,
            primary = primary1,
            secondary = secondary1,
            delta = change1?.let { formatDelta(it, change1WholeOnly) },
            deltaColor = change1?.let(::deltaColor),
            modifier = Modifier.weight(1f)
        )
        CompactMetricCard(
            title = title2,
            primary = primary2,
            secondary = secondary2,
            delta = change2?.let { formatDelta(it, change2WholeOnly) },
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
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandOrange,
                contentColor = Color.White
            )
        ) {
            Text(label)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.weight(1f),
            border = BorderStroke(1.dp, BrandOrange),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = BrandOrange
            )
        ) {
            Text(label)
        }
    }
}

@Composable
private fun MetricChartCard(
    title: String,
    points: List<ChartPoint>,
    valueFormatter: (Double) -> String,
    fixedMinValue: Double? = null,
    fixedMaxValue: Double? = null
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

            val rawMinValue = points.minOf { it.value }
            val rawMaxValue = points.maxOf { it.value }
            val displayMinValue = fixedMinValue ?: rawMinValue
            val displayMaxValue = fixedMaxValue ?: rawMaxValue
            val plottedMinValue = displayMinValue
            val plottedMaxValue = if (displayMaxValue > displayMinValue) {
                displayMaxValue
            } else {
                displayMinValue + 1.0
            }

            val lineColor = MaterialTheme.colorScheme.primary
            val pointColor = MaterialTheme.colorScheme.primary
            val axisColor = MaterialTheme.colorScheme.outline

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier.height(220.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = valueFormatter(displayMaxValue),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = valueFormatter(displayMinValue),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(220.dp)
                ) {
                    val padding = 24f
                    val width = size.width - padding * 2
                    val height = size.height - padding * 2
                    val valueRange = plottedMaxValue - plottedMinValue

                    drawLine(
                        color = axisColor,
                        start = Offset(padding, size.height - padding),
                        end = Offset(size.width - padding, size.height - padding),
                        strokeWidth = 2f
                    )

                    val offsets = points.mapIndexed { index, point ->
                        val x = if (points.size == 1) {
                            padding + width / 2f
                        } else {
                            padding + (index.toFloat() / points.lastIndex.coerceAtLeast(1)) * width
                        }

                        val normalized = ((point.value - plottedMinValue) / valueRange)
                            .toFloat()
                            .coerceIn(0f, 1f)

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
            }

            points.takeLast(5).forEach { point ->
                Text("${point.label}: ${valueFormatter(point.value)}")
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
                    value = weekly.averageWeightKg?.let(::formatSmartNumber) ?: "—",
                    modifier = Modifier.weight(1f)
                )
                WeeklyMiniStat(
                    label = "Waist",
                    value = weekly.latestWaistCm?.let(::formatSmartNumber) ?: "—",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeeklyMiniStat(
                    label = "Calories",
                    value = formatWholeDouble(weekly.averageDailyCalories),
                    modifier = Modifier.weight(1f)
                )
                WeeklyMiniStat(
                    label = "Steps",
                    value = formatWholeDouble(weekly.averageDailySteps),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeeklyMiniStat(
                    label = "Fasting",
                    value = formatSmartNumber(weekly.averageDailyFastingHours),
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
                text = "${formatSmartNumber(milestone.targetWeightKg)} kg",
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
        mutableStateOf(initialMilestone?.let { formatSmartNumber(it.targetWeightKg) } ?: "")
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
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, BrandOrange),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BrandOrange
                        )
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
        ProgressMetric.FASTING -> "Fasting"
        ProgressMetric.WORKOUT -> "Workout"
        ProgressMetric.DRINK -> "Drink calories"
    }

    val modeLabel = when (mode) {
        ChartMode.DAILY -> "daily"
        ChartMode.WEEKLY -> "weekly"
    }

    return "$metricLabel — $modeLabel"
}

private fun formatMetricValue(
    metric: ProgressMetric,
    value: Double
): String {
    return when (metric) {
        ProgressMetric.WEIGHT -> formatSmartNumber(value)
        ProgressMetric.FASTING -> formatSmartNumber(value)
        ProgressMetric.CALORIES,
        ProgressMetric.STEPS,
        ProgressMetric.WORKOUT,
        ProgressMetric.DRINK -> formatWholeDouble(value)
    }
}

private fun formatDelta(
    change: MetricChange,
    wholeOnly: Boolean
): String? {
    val delta = change.delta ?: return null
    val formatted = if (wholeOnly) {
        formatWholeDouble(abs(delta))
    } else {
        formatSmartNumber(abs(delta))
    }

    return when (change.direction) {
        TrendDirection.BETTER -> "↑ $formatted"
        TrendDirection.WORSE -> "↓ $formatted"
        TrendDirection.SAME -> "0"
        TrendDirection.NONE -> null
    }
}

private fun deltaColor(change: MetricChange): Color? =
    when (change.direction) {
        TrendDirection.BETTER -> BrandTeal
        TrendDirection.WORSE -> BrandOrange
        TrendDirection.SAME -> BrandGray
        TrendDirection.NONE -> null
    }

private fun mondayOf(date: LocalDate): LocalDate =
    date.minusDays((date.dayOfWeek.value - 1).toLong())

private fun formatSmartNumber(value: Double): String {
    val rounded = value.roundToInt().toDouble()
    return if (abs(value - rounded) < 0.0001) {
        rounded.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}

private fun formatWholeDouble(value: Double): String =
    value.roundToInt().toString()

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