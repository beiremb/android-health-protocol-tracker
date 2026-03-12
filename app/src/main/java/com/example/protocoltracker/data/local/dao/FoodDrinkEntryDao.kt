package com.example.protocoltracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.protocoltracker.data.local.entity.FoodDrinkEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDrinkEntryDao {

    @Query("""
        SELECT * FROM food_drink_entries
        ORDER BY entryDate DESC, timeSlot DESC, id DESC
    """)
    fun observeAll(): Flow<List<FoodDrinkEntry>>

    @Query("""
        SELECT * FROM food_drink_entries
        WHERE entryDate = :date
        ORDER BY timeSlot ASC, id ASC
    """)
    fun observeByDate(date: String): Flow<List<FoodDrinkEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: FoodDrinkEntry): Long

    @Update
    suspend fun update(entry: FoodDrinkEntry)

    @Delete
    suspend fun delete(entry: FoodDrinkEntry)

    @Query("DELETE FROM food_drink_entries")
    suspend fun deleteAll()
}