package com.example.protocoltracker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import com.example.protocoltracker.ProtocolTrackerApp
import com.example.protocoltracker.domain.metrics.MetricChange
import com.example.protocoltracker.domain.metrics.MetricsCalculator
import com.example.protocoltracker.domain.metrics.TrendDirection
import com.example.protocoltracker.ui.common.AppPageTitle
import com.example.protocoltracker.ui.common.CompactMetricCard
import com.example.protocoltracker.ui.common.QuickAddFab
import com.example.protocoltracker.ui.common.SectionCard
import java.time.LocalDate
import java.util.Locale

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

    val foodEntries by remember { repository.observeFoodDrinkEntries() }.collectAsState(initial = emptyList())
    val workoutEntries by remember { repository.observeWorkoutEntries() }.collectAsState(initial = emptyList())
    val weightEntries by remember { repository.observeWeightEntries() }.collectAsState(initial = emptyList())
    val waistEntries by remember { repository.observeWaistEntries() }.collectAsState(initial = emptyList())
    val stepsEntries by remember { repository.observeDailySteps() }.collectAsState(initial = emptyList())

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

    val missingItems = remember(todayString, weightEntries, waistEntries, today.dayOfWeek.value) {
        buildList {
            if (weightEntries.none { it.entryDate == todayString }) add("weight")
            if (today.dayOfWeek.value == 7 && waistEntries.none { it.entryDate == todayString }) add("waist")
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            QuickAddFab(onClick = onOpenLog)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppPageTitle(
                title = "Home",
                subtitle = "Your main numbers for today and this week."
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CompactMetricCard(
                    title = "Weight",
                    primary = dailyMetrics.latestWeightKg?.let { "${formatNumber(it)} kg" } ?: "—",
                    secondary = weeklyMetrics.averageWeightKg?.let { "Week avg ${formatNumber(it)} kg" } ?: "No week avg",
                    delta = formatDelta(changes.weight),
                    deltaColor = deltaColor(changes.weight),
                    modifier = Modifier.weight(1f)
                )

                CompactMetricCard(
                    title = "Waist",
                    primary = dailyMetrics.latestWaistCm?.let { "${formatNumber(it)} cm" } ?: "—",
                    secondary = "Weekly measure",
                    delta = formatDelta(changes.waist),
                    deltaColor = deltaColor(changes.waist),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CompactMetricCard(
                    title = "Calories",
                    primary = "${dailyMetrics.totalCalories}",
                    secondary = "Week avg ${formatNumber(weeklyMetrics.averageDailyCalories)}",
                    delta = formatDelta(changes.calories),
                    deltaColor = deltaColor(changes.calories),
                    modifier = Modifier.weight(1f)
                )

                CompactMetricCard(
                    title = "Hours fasted",
                    primary = formatHours(currentFastingHours),
                    secondary = "Week avg ${formatHours(weeklyMetrics.averageDailyFastingHours)}",
                    delta = formatDelta(changes.fastingHours),
                    deltaColor = deltaColor(changes.fastingHours),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CompactMetricCard(
                    title = "Steps",
                    primary = formatWholeNumber(dailyMetrics.steps),
                    secondary = "Week avg ${formatWholeNumber(weeklyMetrics.averageDailySteps.toInt())}",
                    delta = formatDelta(changes.steps),
                    deltaColor = deltaColor(changes.steps),
                    modifier = Modifier.weight(1f)
                )

                CompactMetricCard(
                    title = "Workout",
                    primary = "${dailyMetrics.workoutMinutes} min",
                    secondary = "Week total ${weeklyMetrics.totalWorkoutMinutes} min",
                    delta = formatDelta(changes.workoutMinutes),
                    deltaColor = deltaColor(changes.workoutMinutes),
                    modifier = Modifier.weight(1f)
                )
            }

            if (missingItems.isNotEmpty()) {
                SectionCard(
                    title = "Due today"
                ) {
                    Text(
                        text = "Still missing: ${missingItems.joinToString(", ")}.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private fun formatNumber(value: Double): String =
    String.format(Locale.US, "%.1f", value)

private fun formatWholeNumber(value: Int): String =
    String.format(Locale.US, "%d", value)

private fun formatHours(value: Double): String =
    "${formatNumber(value)} h"

private fun formatDelta(change: MetricChange): String? {
    val delta = change.delta ?: return null
    if (delta == 0.0) return "0.0"

    return when (change.direction) {
        TrendDirection.BETTER -> "↑ ${formatNumber(kotlin.math.abs(delta))}"
        TrendDirection.WORSE -> "↓ ${formatNumber(kotlin.math.abs(delta))}"
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