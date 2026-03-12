package com.example.protocoltracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.protocoltracker.data.local.entity.WaistEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WaistEntryDao {

    @Query("""
        SELECT * FROM waist_entries
        ORDER BY entryDate DESC, id DESC
    """)
    fun observeAll(): Flow<List<WaistEntry>>

    @Query("""
        SELECT * FROM waist_entries
        WHERE entryDate = :date
        ORDER BY id DESC
    """)
    fun observeByDate(date: String): Flow<List<WaistEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WaistEntry): Long

    @Update
    suspend fun update(entry: WaistEntry)

    @Delete
    suspend fun delete(entry: WaistEntry)

    @Query("DELETE FROM waist_entries")
    suspend fun deleteAll()
}