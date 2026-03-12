package com.example.protocoltracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "waist_entries",
    indices = [Index(value = ["entryDate"])]
)
data class WaistEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val entryDate: String,   // yyyy-MM-dd
    val waistCm: Double
)