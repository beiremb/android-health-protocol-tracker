package com.example.protocoltracker.data.settings

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val context: Context) {

    private object Keys {
        val startDate = stringPreferencesKey("start_date")
        val startWeightKg = doublePreferencesKey("start_weight_kg")
        val goalWeightKg = doublePreferencesKey("goal_weight_kg")
        val calorieBudget = intPreferencesKey("calorie_budget")
        val proteinTarget = intPreferencesKey("protein_target")
        val fastingStart = stringPreferencesKey("fasting_start")
        val fastingEnd = stringPreferencesKey("fasting_end")
        val reminderTime = stringPreferencesKey("reminder_time")
    }

    val settingsFlow: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            startDate = prefs[Keys.startDate],
            startWeightKg = prefs[Keys.startWeightKg],
            goalWeightKg = prefs[Keys.goalWeightKg],
            calorieBudget = prefs[Keys.calorieBudget],
            proteinTarget = prefs[Keys.proteinTarget],
            fastingStart = prefs[Keys.fastingStart] ?: "13:00",
            fastingEnd = prefs[Keys.fastingEnd] ?: "21:00",
            reminderTime = prefs[Keys.reminderTime] ?: "20:00"
        )
    }

    suspend fun setStartDate(value: String) {
        context.settingsDataStore.edit { it[Keys.startDate] = value }
    }

    suspend fun setStartWeightKg(value: Double) {
        context.settingsDataStore.edit { it[Keys.startWeightKg] = value }
    }

    suspend fun setGoalWeightKg(value: Double) {
        context.settingsDataStore.edit { it[Keys.goalWeightKg] = value }
    }

    suspend fun setCalorieBudget(value: Int) {
        context.settingsDataStore.edit { it[Keys.calorieBudget] = value }
    }

    suspend fun setProteinTarget(value: Int) {
        context.settingsDataStore.edit { it[Keys.proteinTarget] = value }
    }

    suspend fun setFastingStart(value: String) {
        context.settingsDataStore.edit { it[Keys.fastingStart] = value }
    }

    suspend fun setFastingEnd(value: String) {
        context.settingsDataStore.edit { it[Keys.fastingEnd] = value }
    }

    suspend fun setReminderTime(value: String) {
        context.settingsDataStore.edit { it[Keys.reminderTime] = value }
    }
}