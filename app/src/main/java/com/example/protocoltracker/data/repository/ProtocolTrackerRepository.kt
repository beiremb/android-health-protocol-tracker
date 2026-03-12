package com.example.protocoltracker.data.repository

import com.example.protocoltracker.data.local.AppDatabase
import com.example.protocoltracker.data.local.entity.DailyStepsEntry
import com.example.protocoltracker.data.local.entity.FoodDrinkEntry
import com.example.protocoltracker.data.local.entity.MilestoneEntry
import com.example.protocoltracker.data.local.entity.WaistEntry
import com.example.protocoltracker.data.local.entity.WeightEntry
import com.example.protocoltracker.data.local.entity.WorkoutEntry

class ProtocolTrackerRepository private constructor(
    private val database: AppDatabase
) {

    fun observeFoodDrinkEntries() = database.foodDrinkEntryDao().observeAll()
    fun observeFoodDrinkEntriesByDate(date: String) = database.foodDrinkEntryDao().observeByDate(date)

    suspend fun insertFoodDrinkEntry(entry: FoodDrinkEntry) =
        database.foodDrinkEntryDao().insert(entry)

    suspend fun updateFoodDrinkEntry(entry: FoodDrinkEntry) =
        database.foodDrinkEntryDao().update(entry)

    suspend fun deleteFoodDrinkEntry(entry: FoodDrinkEntry) =
        database.foodDrinkEntryDao().delete(entry)

    fun observeWorkoutEntries() = database.workoutEntryDao().observeAll()
    fun observeWorkoutEntriesByDate(date: String) = database.workoutEntryDao().observeByDate(date)

    suspend fun insertWorkoutEntry(entry: WorkoutEntry) =
        database.workoutEntryDao().insert(entry)

    suspend fun updateWorkoutEntry(entry: WorkoutEntry) =
        database.workoutEntryDao().update(entry)

    suspend fun deleteWorkoutEntry(entry: WorkoutEntry) =
        database.workoutEntryDao().delete(entry)

    fun observeWeightEntries() = database.weightEntryDao().observeAll()
    fun observeWeightEntriesByDate(date: String) = database.weightEntryDao().observeByDate(date)

    suspend fun insertWeightEntry(entry: WeightEntry) =
        database.weightEntryDao().insert(entry)

    suspend fun updateWeightEntry(entry: WeightEntry) =
        database.weightEntryDao().update(entry)

    suspend fun deleteWeightEntry(entry: WeightEntry) =
        database.weightEntryDao().delete(entry)

    fun observeWaistEntries() = database.waistEntryDao().observeAll()
    fun observeWaistEntriesByDate(date: String) = database.waistEntryDao().observeByDate(date)

    suspend fun insertWaistEntry(entry: WaistEntry) =
        database.waistEntryDao().insert(entry)

    suspend fun updateWaistEntry(entry: WaistEntry) =
        database.waistEntryDao().update(entry)

    suspend fun deleteWaistEntry(entry: WaistEntry) =
        database.waistEntryDao().delete(entry)

    fun observeDailySteps() = database.dailyStepsDao().observeAll()
    fun observeDailyStepsByDate(date: String) = database.dailyStepsDao().observeByDate(date)

    suspend fun upsertDailySteps(entry: DailyStepsEntry) =
        database.dailyStepsDao().upsert(entry)

    suspend fun deleteDailySteps(entry: DailyStepsEntry) =
        database.dailyStepsDao().delete(entry)

    fun observeMilestones() = database.milestoneDao().observeAll()

    suspend fun insertMilestone(entry: MilestoneEntry) =
        database.milestoneDao().insert(entry)

    suspend fun updateMilestone(entry: MilestoneEntry) =
        database.milestoneDao().update(entry)

    suspend fun deleteMilestone(entry: MilestoneEntry) =
        database.milestoneDao().delete(entry)

    companion object {
        @Volatile
        private var INSTANCE: ProtocolTrackerRepository? = null

        fun getInstance(database: AppDatabase): ProtocolTrackerRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = ProtocolTrackerRepository(database)
                INSTANCE = instance
                instance
            }
        }
    }
}