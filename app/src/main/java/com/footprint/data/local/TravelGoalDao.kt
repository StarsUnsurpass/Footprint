package com.footprint.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TravelGoalDao {
    @Query("SELECT * FROM travel_goals ORDER BY target_date ASC")
    fun observeGoals(): Flow<List<TravelGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: TravelGoalEntity)

    @Update
    suspend fun update(goal: TravelGoalEntity)

    @Query("DELETE FROM travel_goals WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM travel_goals")
    suspend fun count(): Int

    @Query("SELECT * FROM travel_goals")
    suspend fun getAll(): List<TravelGoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(goals: List<TravelGoalEntity>)
}
