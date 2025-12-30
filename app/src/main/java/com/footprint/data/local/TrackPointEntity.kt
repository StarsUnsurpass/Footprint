package com.footprint.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_points")
data class TrackPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val speed: Float,
    val accuracy: Float,
    val altitude: Double
)
