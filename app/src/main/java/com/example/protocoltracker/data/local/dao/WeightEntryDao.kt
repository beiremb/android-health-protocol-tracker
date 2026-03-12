package com.example.protocoltracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.protocoltracker.data.local.entity.WeightEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightEntryDao {

    @Query("""
        SELECT * FROM weight_entries
        ORDER BY entryDate DESC, entryTime DESC, id DESC
    """)
    fun observeAll(): Flow<List<WeightEntry>>

    @Query("""
        SELECT * FROM weight_entries
        WHERE entryDate = :date
        ORDER BY entryTime DESC, id DESC
    """)
    fun observeByDate(date: String): Flow<List<WeightEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WeightEntry): Long

    @Update
    suspend fun update(entry: WeightEntry)

    @Delete
    suspend fun delete(entry: WeightEntry)

    @Query("DELETE FROM weight_entries")
    suspend fun deleteAll()
}