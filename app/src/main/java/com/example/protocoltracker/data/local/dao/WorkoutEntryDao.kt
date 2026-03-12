package com.example.protocoltracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.protocoltracker.data.local.entity.WorkoutEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutEntryDao {

    @Query("""
        SELECT * FROM workout_entries
        ORDER BY entryDate DESC, id DESC
    """)
    fun observeAll(): Flow<List<WorkoutEntry>>

    @Query("""
        SELECT * FROM workout_entries
        WHERE entryDate = :date
        ORDER BY id ASC
    """)
    fun observeByDate(date: String): Flow<List<WorkoutEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WorkoutEntry): Long

    @Update
    suspend fun update(entry: WorkoutEntry)

    @Delete
    suspend fun delete(entry: WorkoutEntry)

    @Query("DELETE FROM workout_entries")
    suspend fun deleteAll()
}