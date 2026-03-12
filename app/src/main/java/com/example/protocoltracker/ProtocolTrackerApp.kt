package com.example.protocoltracker

import android.app.Application
import com.example.protocoltracker.data.local.AppDatabase
import com.example.protocoltracker.data.repository.ProtocolTrackerRepository

class ProtocolTrackerApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: ProtocolTrackerRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        repository = ProtocolTrackerRepository.getInstance(database)
    }
}