package com.footprint.data.model

import com.footprint.data.local.FootprintEntity
import com.footprint.data.local.TrackPointEntity
import com.footprint.data.local.TravelGoalEntity

data class BackupData(
    val version: Int = 1,
    val footprints: List<FootprintEntity> = emptyList(),
    val goals: List<TravelGoalEntity> = emptyList(),
    val trackPoints: List<TrackPointEntity> = emptyList(),
    val exportedAt: Long = System.currentTimeMillis()
)
