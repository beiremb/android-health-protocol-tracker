package com.example.protocoltracker

import android.app.Application
import com.example.protocoltracker.background.reminder.ReminderScheduler
import com.example.protocoltracker.data.local.AppDatabase
import com.example.protocoltracker.data.repository.ProtocolTrackerRepository
import com.example.protocoltracker.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ProtocolTrackerApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: ProtocolTrackerRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        repository = ProtocolTrackerRepository.getInstance(database)
        settingsRepository = SettingsRepository(this)

        applicationScope.launch {
            ReminderScheduler.schedule(this@ProtocolTrackerApp)
        }
    }
}