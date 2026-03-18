package com.example.protocoltracker.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import com.example.protocoltracker.ProtocolTrackerApp
import com.example.protocoltracker.domain.metrics.MetricChange
import com.example.protocoltracker.domain.metrics.MetricsCalculator
import com.example.protocoltracker.domain.metrics.TrendDirection
import com.example.protocoltracker.ui.common.AppPageTitle
import com.example.protocoltracker.ui.theme.BrandGray
import com.example.protocoltracker.ui.theme.BrandOrange
import com.example.protocoltracker.ui.theme.BrandTeal
import com.example.protocoltracker.ui.theme.BrandWhite
import java.time.LocalDate
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class HomeCardMode {
    DAILY,
    WEEKLY
}

private fun HomeCardMode.toggle(): HomeCardMode =
    if (this == HomeCardMode.DAILY) HomeCardMode.WEEKLY else HomeCardMode.DAILY

@Suppress("UNUSED_PARAMETER")
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

    val sessionQuote = remember { app.sessionQuote }

    var weightMode by rememberSaveable { mutableStateOf(HomeCardMode.DAILY) }
    var waistMode by rememberSaveable { mutableStateOf(HomeCardMode.DAILY) }
    var caloriesMode by rememberSaveable { mutableStateOf(HomeCardMode.DAILY) }
    var fastingMode by rememberSaveable { mutableStateOf(HomeCardMode.DAILY) }
    var stepsMode by rememberSaveable { mutableStateOf(HomeCardMode.DAILY) }
    var workoutMode by rememberSaveable { mutableStateOf(HomeCardMode.DAILY) }

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
        modifier = Modifier.fillMaxSize()
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
                subtitle = "Tap a card to switch between daily and weekly."
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HomeMetricToggleCard(
                    title = "Weight",
                    mode = weightMode,
                    onToggle = { weightMode = weightMode.toggle() },
                    dailyValue = dailyMetrics.latestWeightKg?.let { "${formatSmartNumber(it)} kg" } ?: "—",
                    weeklyValue = weeklyMetrics.averageWeightKg?.let { "${formatSmartNumber(it)} kg" } ?: "—",
                    weeklyDelta = formatDelta(changes.weight, wholeOnly = false),
                    weeklyDeltaColor = deltaColor(changes.weight),
                    modifier = Modifier.weight(1f)
                )

                HomeMetricToggleCard(
                    title = "Waist",
                    mode = waistMode,
                    onToggle = { waistMode = waistMode.toggle() },
                    dailyValue = dailyMetrics.latestWaistCm?.let { "${formatSmartNumber(it)} cm" } ?: "—",
                    weeklyValue = weeklyMetrics.latestWaistCm?.let { "${formatSmartNumber(it)} cm" } ?: "—",
                    weeklyDelta = formatDelta(changes.waist, wholeOnly = false),
                    weeklyDeltaColor = deltaColor(changes.waist),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HomeMetricToggleCard(
                    title = "Calories",
                    mode = caloriesMode,
                    onToggle = { caloriesMode = caloriesMode.toggle() },
                    dailyValue = formatWholeNumber(dailyMetrics.totalCalories),
                    weeklyValue = formatWholeDouble(weeklyMetrics.averageDailyCalories),
                    weeklyDelta = formatDelta(changes.calories, wholeOnly = true),
                    weeklyDeltaColor = deltaColor(changes.calories),
                    modifier = Modifier.weight(1f)
                )

                HomeMetricToggleCard(
                    title = "Hours fasted",
                    mode = fastingMode,
                    onToggle = { fastingMode = fastingMode.toggle() },
                    dailyValue = formatHours(currentFastingHours),
                    weeklyValue = formatHours(weeklyMetrics.averageDailyFastingHours),
                    weeklyDelta = formatDelta(changes.fastingHours, wholeOnly = false),
                    weeklyDeltaColor = deltaColor(changes.fastingHours),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HomeMetricToggleCard(
                    title = "Steps",
                    mode = stepsMode,
                    onToggle = { stepsMode = stepsMode.toggle() },
                    dailyValue = formatWholeNumber(dailyMetrics.steps),
                    weeklyValue = formatWholeDouble(weeklyMetrics.averageDailySteps),
                    weeklyDelta = formatDelta(changes.steps, wholeOnly = true),
                    weeklyDeltaColor = deltaColor(changes.steps),
                    modifier = Modifier.weight(1f)
                )

                HomeMetricToggleCard(
                    title = "Workout",
                    mode = workoutMode,
                    onToggle = { workoutMode = workoutMode.toggle() },
                    dailyValue = "${formatWholeNumber(dailyMetrics.workoutMinutes)} min",
                    weeklyValue = "${formatWholeNumber(weeklyMetrics.totalWorkoutMinutes)} min",
                    weeklyDelta = formatDelta(changes.workoutMinutes, wholeOnly = true),
                    weeklyDeltaColor = deltaColor(changes.workoutMinutes),
                    modifier = Modifier.weight(1f)
                )
            }

            if (missingItems.isNotEmpty()) {
                DueTodayCard(
                    missingItems = missingItems
                )
            }

            QuoteCard(
                quote = sessionQuote
            )
        }
    }
}

@Composable
private fun HomeMetricToggleCard(
    title: String,
    mode: HomeCardMode,
    onToggle: () -> Unit,
    dailyValue: String,
    weeklyValue: String,
    weeklyDelta: String?,
    weeklyDeltaColor: Color?,
    modifier: Modifier = Modifier
) {
    val containerColor = if (mode == HomeCardMode.DAILY) BrandTeal else BrandOrange

    Card(
        modifier = modifier.clickable(onClick = onToggle),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = BrandWhite
                )

                if (mode == HomeCardMode.WEEKLY && !weeklyDelta.isNullOrBlank()) {
                    DeltaBadge(
                        text = weeklyDelta,
                        color = weeklyDeltaColor ?: BrandGray
                    )
                }
            }

            Text(
                text = if (mode == HomeCardMode.DAILY) dailyValue else weeklyValue,
                style = MaterialTheme.typography.headlineSmall,
                color = BrandWhite
            )

            Text(
                text = if (mode == HomeCardMode.DAILY) "Today" else "This week",
                style = MaterialTheme.typography.labelMedium,
                color = BrandWhite.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun DeltaBadge(
    text: String,
    color: Color
) {
    Card(
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(
            containerColor = BrandWhite
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

@Composable
private fun DueTodayCard(
    missingItems: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = BrandOrange.copy(alpha = 0.14f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Due today",
                style = MaterialTheme.typography.titleMedium,
                color = BrandGray
            )
            Text(
                text = "Still missing: ${missingItems.joinToString(", ")}.",
                style = MaterialTheme.typography.bodyMedium,
                color = BrandGray
            )
        }
    }
}

@Composable
private fun QuoteCard(
    quote: HomeQuote
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 32.dp,
            topEnd = 32.dp,
            bottomStart = 20.dp,
            bottomEnd = 20.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = BrandGray
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "\"${quote.text}\"",
                style = MaterialTheme.typography.titleMedium,
                color = BrandWhite
            )
            Text(
                text = "- ${quote.author}",
                style = MaterialTheme.typography.bodySmall,
                color = BrandWhite.copy(alpha = 0.78f)
            )
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

private fun formatWholeDouble(value: Double): String =
    value.roundToInt().toString()

private fun formatWholeNumber(value: Int): String =
    value.toString()

private fun formatHours(value: Double): String =
    "${formatSmartNumber(value)} h"

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