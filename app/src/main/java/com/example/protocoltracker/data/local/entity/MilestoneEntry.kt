package com.example.protocoltracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "milestones",
    indices = [Index(value = ["sortOrder"], unique = true)]
)
data class MilestoneEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val targetWeightKg: Double,
    val targetDate: String,   // yyyy-MM-dd
    val rewardText: String,
    val sortOrder: Int
)