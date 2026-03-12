package com.example.protocoltracker.data.local.converter

import androidx.room.TypeConverter
import com.example.protocoltracker.data.local.entity.FoodDrinkType
import com.example.protocoltracker.data.local.entity.WorkoutIntensity
import com.example.protocoltracker.data.local.entity.WorkoutType

class AppTypeConverters {

    @TypeConverter
    fun fromFoodDrinkType(value: FoodDrinkType): String = value.name

    @TypeConverter
    fun toFoodDrinkType(value: String): FoodDrinkType = FoodDrinkType.valueOf(value)

    @TypeConverter
    fun fromWorkoutType(value: WorkoutType): String = value.name

    @TypeConverter
    fun toWorkoutType(value: String): WorkoutType = WorkoutType.valueOf(value)

    @TypeConverter
    fun fromWorkoutIntensity(value: WorkoutIntensity): String = value.name

    @TypeConverter
    fun toWorkoutIntensity(value: String): WorkoutIntensity = WorkoutIntensity.valueOf(value)
}