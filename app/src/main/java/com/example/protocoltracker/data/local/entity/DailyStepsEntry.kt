package com.example.protocoltracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_steps")
data class DailyStepsEntry(
    @PrimaryKey
    val entryDate: String,   // yyyy-MM-dd
    val steps: Int
)