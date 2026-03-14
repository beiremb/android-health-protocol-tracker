package com.example.protocoltracker.domain.metrics

import com.example.protocoltracker.data.local.entity.DailyStepsEntry
import com.example.protocoltracker.data.local.entity.FoodDrinkEntry
import com.example.protocoltracker.data.local.entity.FoodDrinkType
import com.example.protocoltracker.data.local.entity.WaistEntry
import com.example.protocoltracker.data.local.entity.WeightEntry
import com.example.protocoltracker.data.local.entity.WorkoutEntry
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object MetricsCalculator {

    fun calculateDailyMetrics(
        date: String,
        foodEntries: List<FoodDrinkEntry>,
        workoutEntries: List<WorkoutEntry>,
        weightEntries: List<WeightEntry>,
        waistEntries: List<WaistEntry>,
        dailyStepsEntries: List<DailyStepsEntry>
    ): DailyMetrics {
        val latestWeight = latestWeightForDate(date, weightEntries)
        val latestWaist = latestWaistAtOrBeforeDate(date, waistEntries)
        val totalCalories = foodEntries
            .filter { it.entryDate == date }
            .sumOf { it.calories }

        val drinkCalories = foodEntries
            .filter { it.entryDate == date && it.entryType == FoodDrinkType.DRINK }
            .sumOf { it.calories }

        val steps = dailyStepsEntries
            .firstOrNull { it.entryDate == date }
            ?.steps ?: 0

        val workoutMinutes = workoutEntries
            .filter { it.entryDate == date }
            .sumOf { it.minutes }

        val fastingHours = calculateDailyFastingHours(date, foodEntries)

        return DailyMetrics(
            date = date,
            latestWeightKg = latestWeight,
            totalCalories = totalCalories,
            latestWaistCm = latestWaist,
            steps = steps,
            fastingHours = fastingHours,
            workoutMinutes = workoutMinutes,
            drinkCalories = drinkCalories
        )
    }

    fun calculateWeeklyMetrics(
        weekStartDate: String,
        foodEntries: List<FoodDrinkEntry>,
        workoutEntries: List<WorkoutEntry>,
        weightEntries: List<WeightEntry>,
        waistEntries: List<WaistEntry>,
        dailyStepsEntries: List<DailyStepsEntry>
    ): WeeklyMetrics {
        val start = parseDate(weekStartDate)
            ?: return WeeklyMetrics(
                weekStartDate = weekStartDate,
                weekEndDate = weekStartDate,
                averageWeightKg = null,
                averageDailyCalories = 0.0,
                latestWaistCm = null,
                averageDailySteps = 0.0,
                averageDailyFastingHours = 0.0,
                totalWorkoutMinutes = 0,
                drinkCalories = 0
            )

        val days = (0..6).map { start.plusDays(it.toLong()) }
        val dayStrings = days.map { it.toString() }.toSet()
        val weekEnd = days.last().toString()

        val dailyWeights = days.mapNotNull { latestWeightForDate(it.toString(), weightEntries) }
        val averageWeight = if (dailyWeights.isEmpty()) null else dailyWeights.average()

        val averageDailyCalories = days
            .map { day ->
                foodEntries
                    .filter { it.entryDate == day.toString() }
                    .sumOf { it.calories }
                    .toDouble()
            }
            .average()

        val averageDailySteps = days
            .map { day ->
                dailyStepsEntries.firstOrNull { it.entryDate == day.toString() }?.steps?.toDouble() ?: 0.0
            }
            .average()

        val averageDailyFastingHours = days
            .map { day -> calculateDailyFastingHours(day.toString(), foodEntries) }
            .average()

        val totalWorkoutMinutes = workoutEntries
            .filter { it.entryDate in dayStrings }
            .sumOf { it.minutes }

        val drinkCalories = foodEntries
            .filter { it.entryDate in dayStrings && it.entryType == FoodDrinkType.DRINK }
            .sumOf { it.calories }

        val latestWaist = latestWaistAtOrBeforeDate(weekEnd, waistEntries)

        return WeeklyMetrics(
            weekStartDate = start.toString(),
            weekEndDate = weekEnd,
            averageWeightKg = averageWeight,
            averageDailyCalories = averageDailyCalories,
            latestWaistCm = latestWaist,
            averageDailySteps = averageDailySteps,
            averageDailyFastingHours = averageDailyFastingHours,
            totalWorkoutMinutes = totalWorkoutMinutes,
            drinkCalories = drinkCalories
        )
    }

    fun calculateWeeklyMetricChanges(
        current: WeeklyMetrics,
        previous: WeeklyMetrics?
    ): WeeklyMetricChanges {
        return WeeklyMetricChanges(
            weight = compareLowerBetter(current.averageWeightKg, previous?.averageWeightKg),
            calories = compareLowerBetter(current.averageDailyCalories, previous?.averageDailyCalories),
            waist = compareLowerBetter(current.latestWaistCm, previous?.latestWaistCm),
            steps = compareHigherBetter(current.averageDailySteps, previous?.averageDailySteps),
            fastingHours = compareHigherBetter(current.averageDailyFastingHours, previous?.averageDailyFastingHours),
            workoutMinutes = compareHigherBetter(
                current.totalWorkoutMinutes.toDouble(),
                previous?.totalWorkoutMinutes?.toDouble()
            )
        )
    }

    fun calculateCurrentFastingHours(
        foodEntries: List<FoodDrinkEntry>,
        now: LocalDateTime = LocalDateTime.now()
    ): Double {
        val lastCaloricEntry = caloricDateTimes(foodEntries)
            .filter { it <= now }
            .maxOrNull() ?: return 0.0

        return hoursBetween(lastCaloricEntry, now)
    }

    private fun calculateDailyFastingHours(
        date: String,
        foodEntries: List<FoodDrinkEntry>
    ): Double {
        val day = parseDate(date) ?: return 0.0

        val firstCaloricToday = foodEntries
            .asSequence()
            .filter { it.calories > 0 && it.entryDate == date }
            .mapNotNull { foodEntryDateTime(it) }
            .minOrNull() ?: return 0.0

        val previousCaloricEntry = caloricDateTimes(foodEntries)
            .filter { it < firstCaloricToday && it.toLocalDate() < day }
            .maxOrNull() ?: return 0.0

        return hoursBetween(previousCaloricEntry, firstCaloricToday)
    }

    private fun latestWeightForDate(
        date: String,
        weightEntries: List<WeightEntry>
    ): Double? {
        return weightEntries
            .filter { it.entryDate == date }
            .mapNotNull { entry ->
                val parsedTime = parseTime(entry.entryTime) ?: return@mapNotNull null
                LocalDateTime.of(parseDate(entry.entryDate) ?: return@mapNotNull null, parsedTime) to entry.weightKg
            }
            .maxByOrNull { it.first }
            ?.second
    }

    private fun latestWaistAtOrBeforeDate(
        date: String,
        waistEntries: List<WaistEntry>
    ): Double? {
        val target = parseDate(date) ?: return null

        return waistEntries
            .mapNotNull { entry ->
                val parsedDate = parseDate(entry.entryDate) ?: return@mapNotNull null
                if (parsedDate <= target) parsedDate to entry.waistCm else null
            }
            .maxByOrNull { it.first }
            ?.second
    }

    private fun compareLowerBetter(
        current: Double?,
        previous: Double?
    ): MetricChange {
        if (current == null || previous == null) {
            return MetricChange(current = current, previous = previous)
        }

        val delta = current - previous
        val direction = when {
            delta < 0 -> TrendDirection.BETTER
            delta > 0 -> TrendDirection.WORSE
            else -> TrendDirection.SAME
        }

        return MetricChange(
            current = current,
            previous = previous,
            delta = delta,
            direction = direction
        )
    }

    private fun compareHigherBetter(
        current: Double?,
        previous: Double?
    ): MetricChange {
        if (current == null || previous == null) {
            return MetricChange(current = current, previous = previous)
        }

        val delta = current - previous
        val direction = when {
            delta > 0 -> TrendDirection.BETTER
            delta < 0 -> TrendDirection.WORSE
            else -> TrendDirection.SAME
        }

        return MetricChange(
            current = current,
            previous = previous,
            delta = delta,
            direction = direction
        )
    }

    private fun caloricDateTimes(foodEntries: List<FoodDrinkEntry>): List<LocalDateTime> {
        return foodEntries
            .asSequence()
            .filter { it.calories > 0 }
            .mapNotNull { foodEntryDateTime(it) }
            .toList()
    }

    private fun foodEntryDateTime(entry: FoodDrinkEntry): LocalDateTime? {
        val parsedDate = parseDate(entry.entryDate) ?: return null
        val parsedTime = parseTime(entry.timeSlot) ?: return null
        return LocalDateTime.of(parsedDate, parsedTime)
    }

    private fun parseDate(value: String): LocalDate? =
        runCatching { LocalDate.parse(value) }.getOrNull()

    private fun parseTime(value: String): LocalTime? =
        runCatching { LocalTime.parse(value) }.getOrNull()

    private fun hoursBetween(
        start: LocalDateTime,
        end: LocalDateTime
    ): Double {
        val minutes = java.time.Duration.between(start, end).toMinutes()
        return minutes / 60.0
    }
}