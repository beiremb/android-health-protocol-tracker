package com.example.protocoltracker.background.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.protocoltracker.data.local.AppDatabase
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class InactivityReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return runCatching {
            if (shouldSendReminder()) {
                ReminderNotifications.showInactivityNotification(applicationContext)
            }
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }

    private suspend fun shouldSendReminder(): Boolean {
        val today = LocalDate.now().toString()
        val db = AppDatabase.getDatabase(applicationContext)

        val hasFoodDrink = db.foodDrinkEntryDao().observeByDate(today).first().isNotEmpty()
        val hasWorkout = db.workoutEntryDao().observeByDate(today).first().isNotEmpty()
        val hasWeight = db.weightEntryDao().observeByDate(today).first().isNotEmpty()
        val hasWaist = db.waistEntryDao().observeByDate(today).first().isNotEmpty()
        val hasSteps = db.dailyStepsDao().observeByDate(today).first() != null

        val hasAnyLog = hasFoodDrink || hasWorkout || hasWeight || hasWaist || hasSteps
        return !hasAnyLog
    }
}