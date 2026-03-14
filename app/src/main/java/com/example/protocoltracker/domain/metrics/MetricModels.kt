package com.example.protocoltracker.domain.metrics

enum class TrendDirection {
    BETTER,
    WORSE,
    SAME,
    NONE
}

data class MetricChange(
    val current: Double?,
    val previous: Double? = null,
    val delta: Double? = null,
    val direction: TrendDirection = TrendDirection.NONE
)

data class DailyMetrics(
    val date: String,
    val latestWeightKg: Double?,
    val totalCalories: Int,
    val latestWaistCm: Double?,
    val steps: Int,
    val fastingHours: Double,
    val workoutMinutes: Int,
    val drinkCalories: Int
)

data class WeeklyMetrics(
    val weekStartDate: String,
    val weekEndDate: String,
    val averageWeightKg: Double?,
    val averageDailyCalories: Double,
    val latestWaistCm: Double?,
    val averageDailySteps: Double,
    val averageDailyFastingHours: Double,
    val totalWorkoutMinutes: Int,
    val drinkCalories: Int
)

data class WeeklyMetricChanges(
    val weight: MetricChange,
    val calories: MetricChange,
    val waist: MetricChange,
    val steps: MetricChange,
    val fastingHours: MetricChange,
    val workoutMinutes: MetricChange
)