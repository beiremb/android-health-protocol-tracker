package com.example.protocoltracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class FoodDrinkType {
    MEAL,
    SNACK,
    DRINK
}

@Entity(
    tableName = "food_drink_entries",
    indices = [
        Index(value = ["entryDate"]),
        Index(value = ["entryDate", "timeSlot"])
    ]
)
data class FoodDrinkEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val entryDate: String,   // yyyy-MM-dd
    val timeSlot: String,    // e.g. 13:00, 13:30
    val entryType: FoodDrinkType,
    val name: String,
    val calories: Int,
    val proteinGrams: Int? = null,
    val templateId: Long? = null
)