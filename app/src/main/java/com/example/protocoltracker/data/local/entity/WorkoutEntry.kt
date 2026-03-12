package com.example.protocoltracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class WorkoutType {
    STRENGTH,
    WALKING,
    KUNG_FU,
    CYCLING,
    RUNNING,
    RECOVERY,
    TENNIS,
    OTHER
}

enum class WorkoutIntensity {
    LOW,
    MID,
    HIGH
}

@Entity(
    tableName = "workout_entries",
    indices = [Index(value = ["entryDate"])]
)
data class WorkoutEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val entryDate: String,   // yyyy-MM-dd
    val workoutType: WorkoutType,
    val intensity: WorkoutIntensity,
    val minutes: Int
)