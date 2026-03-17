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
        val appContext = context.applicationContext
        val settings = SettingsRepository(appContext).settingsFlow.first()
        val reminderTime = parseReminderTime(settings.reminderTime)

        val now = LocalDateTime.now()
        val nextRun = nextRunDateTime(now, reminderTime)
        val initialDelayMinutes = Duration.between(now, nextRun).toMinutes().coerceAtLeast(1)

        val request = PeriodicWorkRequestBuilder<InactivityReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun nextRunDateTime(now: LocalDateTime, reminderTime: LocalTime): LocalDateTime {
        val candidate = now.withHour(reminderTime.hour)
            .withMinute(reminderTime.minute)
            .withSecond(0)
            .withNano(0)

        return if (candidate <= now) candidate.plusDays(1) else candidate
    }

    private fun parseReminderTime(value: String): LocalTime {
        return runCatching { LocalTime.parse(value.trim()) }
            .getOrElse { LocalTime.of(20, 0) }
    }
}