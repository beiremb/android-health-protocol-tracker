package com.example.protocoltracker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.protocoltracker.ProtocolTrackerApp
import com.example.protocoltracker.domain.metrics.DailyMetrics
import com.example.protocoltracker.domain.metrics.MetricChange
import com.example.protocoltracker.domain.metrics.MetricsCalculator
import com.example.protocoltracker.domain.metrics.TrendDirection
import com.example.protocoltracker.domain.metrics.WeeklyMetrics
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

private enum class DashboardMode {
    DAILY,
    WEEKLY
}

@Composable
fun HomeScreen(
    onOpenLog: () -> Unit
) {
    val app = LocalContext.current.applicationContext as ProtocolTrackerApp
    val repository = app.repository
    val today = LocalDate.now()
    val todayString = today.toString()
    val currentWeekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1L)
    val previousWeekStart = currentWeekStart.minusDays(7)

    val foodEntries by remember {
        repository.observeFoodDrinkEntries()
    }.collectAsState(initial = emptyList())

    val workoutEntries by remember {
        repository.observeWorkoutEntries()
    }.collectAsState(initial = emptyList())

    val weightEntries by remember {
        repository.observeWeightEntries()
    }.collectAsState(initial = emptyList())

    val waistEntries by remember {
        repository.observeWaistEntries()
    }.collectAsState(initial = emptyList())

    val stepsEntries by remember {
        repository.observeDailySteps()
    }.collectAsState(initial = emptyList())

    val dailyMetrics = remember(
        todayString,
        foodEntries,
        workoutEntries,
        weightEntries,
        waistEntries,
        stepsEntries
    ) {
        MetricsCalculator.calculateDailyMetrics(
            date = todayString,
            foodEntries = foodEntries,
            workoutEntries = workoutEntries,
            weightEntries = weightEntries,
            waistEntries = waistEntries,
            dailyStepsEntries = stepsEntries
        )
    }

    val weeklyMetrics = remember(
        currentWeekStart,
        foodEntries,
        workoutEntries,
        weightEntries,
        waistEntries,
        stepsEntries
    ) {
        MetricsCalculator.calculateWeeklyMetrics(
            weekStartDate = currentWeekStart.toString(),
            foodEntries = foodEntries,
            workoutEntries = workoutEntries,
            weightEntries = weightEntries,
            waistEntries = waistEntries,
            dailyStepsEntries = stepsEntries
        )
    }

    val previousWeeklyMetrics = remember(
        previousWeekStart,
        foodEntries,
        workoutEntries,
        weightEntries,
        waistEntries,
        stepsEntries
    ) {
        MetricsCalculator.calculateWeeklyMetrics(
            weekStartDate = previousWeekStart.toString(),
            foodEntries = foodEntries,
            workoutEntries = workoutEntries,
            weightEntries = weightEntries,
            waistEntries = waistEntries,
            dailyStepsEntries = stepsEntries
        )
    }

    val changes = remember(weeklyMetrics, previousWeeklyMetrics) {
        MetricsCalculator.calculateWeeklyMetricChanges(
            current = weeklyMetrics,
            previous = previousWeeklyMetrics
        )
    }

    val currentFastingHours = remember(foodEntries) {
        MetricsCalculator.calculateCurrentFastingHours(foodEntries)
    }

    val latestWeightTodayExists = weightEntries.any { it.entryDate == todayString }
    val latestWaistTodayExists = waistEntries.any { it.entryDate == todayString }
    val isSunday = today.dayOfWeek.value == 7
    val inEatingWindow = LocalTime.now() >= LocalTime.of(13, 0) &&
            LocalTime.now() <= LocalTime.of(21, 0)

    var mode = DashboardMode.DAILY

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Home",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val dailySelected = mode == DashboardMode.DAILY
                val weeklySelected = mode == DashboardMode.WEEKLY

                Button(
                    onClick = { mode = DashboardMode.DAILY },
                    modifier = Modifier.weight(1f),
                    enabled = !dailySelected
                ) {
                    Text("Daily")
                }

                Button(
                    onClick = { mode = DashboardMode.WEEKLY },
                    modifier = Modifier.weight(1f),
                    enabled = !weeklySelected
                ) {
                    Text("Weekly")
                }
            }
        }

        if (mode == DashboardMode.DAILY) {
            item { MetricCard("Weight", dailyWeightText(dailyMetrics)) }
            item { MetricCard("Calories", "${dailyMetrics.totalCalories}") }
            item { MetricCard("Waist", dailyWaistText(dailyMetrics)) }
            item { MetricCard("Steps", "${dailyMetrics.steps}") }
            item { MetricCard("Hours fasted", formatDouble(currentFastingHours)) }
            item { MetricCard("Workout minutes", "${dailyMetrics.workoutMinutes}") }
        } else {
            item {
                WeeklyMetricCard(
                    title = "Weight",
                    value = weeklyWeightText(weeklyMetrics),
                    change = changes.weight
                )
            }
            item {
                WeeklyMetricCard(
                    title = "Calories",
                    value = formatDouble(weeklyMetrics.averageDailyCalories),
                    change = changes.calories
                )
            }
            item {
                WeeklyMetricCard(
                    title = "Waist",
                    value = weeklyWaistText(weeklyMetrics),
                    change = changes.waist
                )
            }
            item {
                WeeklyMetricCard(
                    title = "Steps",
                    value = formatDouble(weeklyMetrics.averageDailySteps),
                    change = changes.steps
                )
            }
            item {
                WeeklyMetricCard(
                    title = "Hours fasted",
                    value = formatDouble(weeklyMetrics.averageDailyFastingHours),
                    change = changes.fastingHours
                )
            }
            item {
                WeeklyMetricCard(
                    title = "Workout minutes",
                    value = "${weeklyMetrics.totalWorkoutMinutes}",
                    change = changes.workoutMinutes
                )
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Status", style = MaterialTheme.typography.titleMedium)
                    Text("Eating window: 13:00–21:00")
                    Text(if (inEatingWindow) "Current state: EATING WINDOW" else "Current state: FASTING")
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Due today", style = MaterialTheme.typography.titleMedium)
                    if (!latestWeightTodayExists) {
                        Text("• Weight missing")
                    }
                    if (isSunday && !latestWaistTodayExists) {
                        Text("• Waist missing")
                    }
                    if (latestWeightTodayExists && (!isSunday || latestWaistTodayExists)) {
                        Text("Nothing due")
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onOpenLog,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open log")
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun WeeklyMetricCard(
    title: String,
    value: String,
    change: MetricChange
) {
    val changeText = when (val delta = change.delta) {
        null -> "No previous week"
        0.0 -> "No change"
        else -> formatSignedDouble(delta)
    }

    val changeColor = when (change.direction) {
        TrendDirection.BETTER -> Color(0xFF2E7D32)
        TrendDirection.WORSE -> Color(0xFFC62828)
        TrendDirection.SAME,
        TrendDirection.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = changeText,
                color = changeColor
            )
        }
    }
}

private fun dailyWeightText(metrics: DailyMetrics): String =
    metrics.latestWeightKg?.let { formatDouble(it) } ?: "Not logged"

private fun dailyWaistText(metrics: DailyMetrics): String =
    metrics.latestWaistCm?.let { formatDouble(it) } ?: "Not logged"

private fun weeklyWeightText(metrics: WeeklyMetrics): String =
    metrics.averageWeightKg?.let { formatDouble(it) } ?: "No data"

private fun weeklyWaistText(metrics: WeeklyMetrics): String =
    metrics.latestWaistCm?.let { formatDouble(it) } ?: "No data"

private fun formatDouble(value: Double): String =
    String.format(Locale.US, "%.1f", value)

private fun formatSignedDouble(value: Double): String =
    if (value > 0) {
        String.format(Locale.US, "+%.1f", value)
    } else {
        String.format(Locale.US, "%.1f", value)
    }