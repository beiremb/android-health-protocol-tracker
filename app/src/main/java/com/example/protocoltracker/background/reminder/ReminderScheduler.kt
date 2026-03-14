package com.example.protocoltracker.background.reminder

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.protocoltracker.data.settings.SettingsRepository
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val UNIQUE_WORK_NAME = "daily_inactivity_reminder"

    suspend fun schedule(context: Context) {
        val settings = SettingsRepository(context).settingsFlow.first()
        val reminderTime = parseReminderTime(settings.reminderTime)

        val now = LocalDateTime.now()
        val nextRun = now.withHour(reminderTime.hour)
            .withMinute(reminderTime.minute)
            .withSecond(0)
            .withNano(0)
            .let { if (it <= now) it.plusDays(1) else it }

        val initialDelayMinutes = Duration.between(now, nextRun).toMinutes()

        val request = PeriodicWorkRequestBuilder<InactivityReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun parseReminderTime(value: String): LocalTime {
        return runCatching { LocalTime.parse(value) }.getOrElse { LocalTime.of(20, 0) }
    }
}