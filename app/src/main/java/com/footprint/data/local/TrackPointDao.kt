package com.footprint.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackPointDao {
    @Insert
    suspend fun insert(point: TrackPointEntity)

    @Query("SELECT * FROM track_points WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp ASC")
    fun getPointsInRange(start: Long, end: Long): Flow<List<TrackPointEntity>>

    @Query("SELECT * FROM track_points WHERE timestamp >= :since ORDER BY timestamp ASC")
    fun getPointsSince(since: Long): Flow<List<TrackPointEntity>>
    
    @Query("DELETE FROM track_points WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}
