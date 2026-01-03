package com.footprint.data.repository

import com.footprint.data.local.FootprintDao
import com.footprint.data.local.FootprintEntity
import com.footprint.data.local.TravelGoalDao
import com.footprint.data.local.TravelGoalEntity
import com.footprint.data.model.FootprintEntry
import com.footprint.data.model.Mood
import com.footprint.data.model.TravelGoal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset

class FootprintRepository(
    private val footprintDao: FootprintDao,
    private val travelGoalDao: TravelGoalDao,
    private val trackPointDao: com.footprint.data.local.TrackPointDao,
    private val preferenceManager: com.footprint.utils.PreferenceManager
) {

    private val ioScope = CoroutineScope(Dispatchers.IO)

    fun observeEntries(): Flow<List<FootprintEntry>> =
        footprintDao.observeEntries().map { list -> list.map { it.toModel() } }

    fun observeGoals(): Flow<List<TravelGoal>> =
        travelGoalDao.observeGoals().map { list -> list.map { it.toModel() } }

    // --- Tracking ---
    suspend fun saveTrackPoint(location: com.amap.api.location.AMapLocation) {
        val entity = com.footprint.data.local.TrackPointEntity(
            latitude = location.latitude,
            longitude = location.longitude,
            timestamp = location.time,
            speed = location.speed,
            accuracy = location.accuracy,
            altitude = location.altitude
        )
        trackPointDao.insert(entity)
    }

    fun getTrackPoints(startTime: Long, endTime: Long): Flow<List<com.footprint.data.local.TrackPointEntity>> {
        return trackPointDao.getPointsInRange(startTime, endTime)
    }

    suspend fun getTrackPointCount(year: Int, month: Int? = null): Int {
        val start = if (month == null) {
            LocalDate.of(year, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        } else {
            LocalDate.of(year, month, 1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        }
        
        val end = if (month == null) {
            LocalDate.of(year, 12, 31).atTime(23, 59, 59).toInstant(ZoneOffset.UTC).toEpochMilli()
        } else {
            val lastDay = LocalDate.of(year, month, 1).lengthOfMonth()
            LocalDate.of(year, month, lastDay).atTime(23, 59, 59).toInstant(ZoneOffset.UTC).toEpochMilli()
        }
        
        return trackPointDao.getCountInRange(start, end)
    }

    suspend fun prepareBackup(): com.footprint.data.model.BackupData {
        return com.footprint.data.model.BackupData(
            footprints = footprintDao.getAll(),
            goals = travelGoalDao.getAll(),
            trackPoints = trackPointDao.getAll()
        )
    }

    suspend fun restoreFromBackup(data: com.footprint.data.model.BackupData) {
        if (data.footprints.isNotEmpty()) footprintDao.upsertAll(data.footprints)
        if (data.goals.isNotEmpty()) travelGoalDao.upsertAll(data.goals)
        if (data.trackPoints.isNotEmpty()) trackPointDao.insertAll(data.trackPoints)
    }
    // ----------------

    suspend fun saveEntry(entry: FootprintEntry) {
        footprintDao.upsert(entry.toEntity())
    }

    suspend fun deleteEntry(id: Long) = footprintDao.deleteById(id)

    suspend fun saveGoal(goal: TravelGoal) {
        travelGoalDao.upsert(goal.toEntity())
    }

    suspend fun deleteGoal(id: Long) = travelGoalDao.deleteById(id)

    suspend fun updateGoalCompletion(goal: TravelGoal, completed: Boolean) {
        travelGoalDao.upsert(goal.copy(isCompleted = completed).toEntity())
    }

    fun ensureSeedData() {
        ioScope.launch {
            if (!preferenceManager.hasSeededV5) {
                if (footprintDao.count() == 0) {
                    SeedData.entries.forEach { footprintDao.upsert(it) }
                }
                if (travelGoalDao.count() == 0) {
                    SeedData.goals.forEach { travelGoalDao.upsert(it) }
                }
                preferenceManager.hasSeededV5 = true
            }
        }
    }

    private fun FootprintEntity.toModel() = FootprintEntry(
        id = id,
        title = title,
        location = location,
        detail = detail,
        mood = mood,
        tags = tags,
        distanceKm = distanceKm,
        photos = photos,
        energyLevel = energyLevel,
        happenedOn = happenedOn,
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        weather = weather,
        temperature = temperature,
        transportType = try { com.footprint.data.model.TransportType.valueOf(transportType) } catch (e: Exception) { com.footprint.data.model.TransportType.UNKNOWN },
        carbonSavedKg = carbonSaved,
        icon = icon
    )

    private fun FootprintEntry.toEntity() = FootprintEntity(
        id = id,
        title = title,
        location = location,
        detail = detail,
        mood = mood,
        tags = tags,
        distanceKm = distanceKm,
        photos = photos,
        energyLevel = energyLevel,
        happenedOn = happenedOn,
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        weather = weather,
        temperature = temperature,
        transportType = transportType.name,
        carbonSaved = carbonSavedKg,
        icon = icon
    )

    private fun TravelGoalEntity.toModel() = TravelGoal(
        id = id,
        title = title,
        targetLocation = targetLocation,
        targetDate = targetDate,
        notes = notes,
        isCompleted = isCompleted,
        progress = progress,
        icon = icon
    )

    private fun TravelGoal.toEntity() = TravelGoalEntity(
        id = id,
        title = title,
        targetLocation = targetLocation,
        targetDate = targetDate,
        notes = notes,
        isCompleted = isCompleted,
        progress = progress,
        icon = icon
    )
}

private object SeedData {
    val entries = listOf(
        FootprintEntity(
            title = "川西彩林穿越",
            location = "阿坝州 四姑娘山",
            detail = "第一次完成4000+米徒步，夜宿牛棚看到了绝美的星空。",
            mood = Mood.EXCITED,
            tags = listOf("徒步", "高海拔", "摄影"),
            distanceKm = 18.4,
            photos = emptyList(),
            energyLevel = 8,
            happenedOn = LocalDate.now().minusDays(12),
            latitude = 31.1,
            longitude = 102.9
        ),
        FootprintEntity(
            title = "魔都城市夜跑",
            location = "上海 黄浦江",
            detail = "和朋友们一起完成半程马拉松，收集沿途的建筑灯光。",
            mood = Mood.CURIOUS,
            tags = listOf("夜跑", "朋友"),
            distanceKm = 21.0,
            photos = emptyList(),
            energyLevel = 7,
            happenedOn = LocalDate.now().minusDays(25),
            latitude = 31.23,
            longitude = 121.47
        ),
        FootprintEntity(
            title = "厦门海岸线骑行",
            location = "厦门 环岛路",
            detail = "记录海风、咖啡香和随拍的胶片照片。",
            mood = Mood.RELAXED,
            tags = listOf("骑行", "海边"),
            distanceKm = 32.5,
            photos = emptyList(),
            energyLevel = 6,
            happenedOn = LocalDate.now().minusDays(37),
            latitude = 24.47,
            longitude = 118.1
        )
    )

    val goals = listOf(
        TravelGoalEntity(
            title = "川藏线摩旅",
            targetLocation = "拉萨",
            targetDate = LocalDate.now().plusMonths(6),
            notes = "计划用14天记录沿线人文与风景，拍摄年系列纪录。",
            isCompleted = false,
            progress = 30
        ),
        TravelGoalEntity(
            title = "极地观星计划",
            targetLocation = "漠河",
            targetDate = LocalDate.now().plusMonths(2),
            notes = "希望捕捉极光并完成一篇深入的观星笔记。",
            isCompleted = false,
            progress = 10
        )
    )
}
