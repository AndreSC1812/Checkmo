package com.ascdev.checkmo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeEntryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: TimeEntryEntity)

    @Query(
        """
        SELECT * FROM time_entries
        WHERE user_id = :userId AND timestamp >= :fromMillis AND timestamp < :toMillis
        ORDER BY timestamp ASC
        """,
    )
    fun observeBetween(userId: String, fromMillis: Long, toMillis: Long): Flow<List<TimeEntryEntity>>

    @Query(
        """
        SELECT * FROM time_entries
        WHERE user_id = :userId AND timestamp < :beforeMillis
        ORDER BY timestamp DESC
        LIMIT 1
        """,
    )
    fun observeLatestBefore(userId: String, beforeMillis: Long): Flow<TimeEntryEntity?>

    @Query("SELECT * FROM time_entries WHERE user_id = :userId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(userId: String): TimeEntryEntity?
}
