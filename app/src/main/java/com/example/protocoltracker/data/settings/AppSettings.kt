package com.example.protocoltracker.data.settings

data class AppSettings(
    val startDate: String? = null,
    val startWeightKg: Double? = null,
    val goalWeightKg: Double? = null,
    val calorieBudget: Int? = null,
    val proteinTarget: Int? = null,
    val fastingStart: String = "13:00",
    val fastingEnd: String = "21:00",
    val reminderTime: String = "20:00",
    val weightChartMinKg: Double = 50.0,
    val weightChartMaxKg: Double = 150.0
)