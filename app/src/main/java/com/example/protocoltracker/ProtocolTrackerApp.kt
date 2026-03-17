package com.example.protocoltracker

import android.app.Application
import com.example.protocoltracker.background.reminder.ReminderScheduler
import com.example.protocoltracker.data.local.AppDatabase
import com.example.protocoltracker.data.repository.ProtocolTrackerRepository
import com.example.protocoltracker.data.settings.SettingsRepository
import com.example.protocoltracker.ui.home.HomeQuote
import com.example.protocoltracker.ui.home.HomeQuotes
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

    val sessionQuote: HomeQuote by lazy {
        HomeQuotes.nextQuote(this)
    }

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