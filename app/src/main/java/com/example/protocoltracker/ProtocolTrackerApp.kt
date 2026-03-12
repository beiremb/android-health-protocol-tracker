package com.example.protocoltracker

import android.app.Application
import com.example.protocoltracker.data.local.AppDatabase
import com.example.protocoltracker.data.repository.ProtocolTrackerRepository
import com.example.protocoltracker.data.settings.SettingsRepository

class ProtocolTrackerApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: ProtocolTrackerRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        repository = ProtocolTrackerRepository.getInstance(database)
        settingsRepository = SettingsRepository(this)
    }
}