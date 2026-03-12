package com.example.protocoltracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.protocoltracker.data.local.entity.DailyStepsEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStepsDao {

    @Query("""
        SELECT * FROM daily_steps
        ORDER BY entryDate DESC
    """)
    fun observeAll(): Flow<List<DailyStepsEntry>>

    @Query("""
        SELECT * FROM daily_steps
        WHERE entryDate = :date
        LIMIT 1
    """)
    fun observeByDate(date: String): Flow<DailyStepsEntry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: DailyStepsEntry)

    @Delete
    suspend fun delete(entry: DailyStepsEntry)

    @Query("DELETE FROM daily_steps")
    suspend fun deleteAll()
}