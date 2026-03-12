package com.example.protocoltracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.protocoltracker.data.local.entity.MilestoneEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao {

    @Query("""
        SELECT * FROM milestones
        ORDER BY sortOrder ASC
    """)
    fun observeAll(): Flow<List<MilestoneEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MilestoneEntry): Long

    @Update
    suspend fun update(entry: MilestoneEntry)

    @Delete
    suspend fun delete(entry: MilestoneEntry)

    @Query("DELETE FROM milestones")
    suspend fun deleteAll()
}