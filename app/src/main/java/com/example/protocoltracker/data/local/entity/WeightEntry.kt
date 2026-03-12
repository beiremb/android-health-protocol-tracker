package com.example.protocoltracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weight_entries",
    indices = [
        Index(value = ["entryDate"]),
        Index(value = ["entryDate", "entryTime"])
    ]
)
data class WeightEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val entryDate: String,   // yyyy-MM-dd
    val entryTime: String,   // HH:mm
    val weightKg: Double
)