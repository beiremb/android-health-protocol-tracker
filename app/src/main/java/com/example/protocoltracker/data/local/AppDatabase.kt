package com.example.protocoltracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.protocoltracker.data.local.converter.AppTypeConverters
import com.example.protocoltracker.data.local.dao.DailyStepsDao
import com.example.protocoltracker.data.local.dao.FoodDrinkEntryDao
import com.example.protocoltracker.data.local.dao.MilestoneDao
import com.example.protocoltracker.data.local.dao.WaistEntryDao
import com.example.protocoltracker.data.local.dao.WeightEntryDao
import com.example.protocoltracker.data.local.dao.WorkoutEntryDao
import com.example.protocoltracker.data.local.entity.DailyStepsEntry
import com.example.protocoltracker.data.local.entity.FoodDrinkEntry
import com.example.protocoltracker.data.local.entity.MilestoneEntry
import com.example.protocoltracker.data.local.entity.WaistEntry
import com.example.protocoltracker.data.local.entity.WeightEntry
import com.example.protocoltracker.data.local.entity.WorkoutEntry

@Database(
    entities = [
        FoodDrinkEntry::class,
        WorkoutEntry::class,
        WeightEntry::class,
        WaistEntry::class,
        DailyStepsEntry::class,
        MilestoneEntry::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun foodDrinkEntryDao(): FoodDrinkEntryDao
    abstract fun workoutEntryDao(): WorkoutEntryDao
    abstract fun weightEntryDao(): WeightEntryDao
    abstract fun waistEntryDao(): WaistEntryDao
    abstract fun dailyStepsDao(): DailyStepsDao
    abstract fun milestoneDao(): MilestoneDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "protocol_tracker.db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}