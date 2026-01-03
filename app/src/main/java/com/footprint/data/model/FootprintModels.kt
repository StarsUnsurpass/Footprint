package com.footprint.data.model

import java.time.LocalDate

data class FootprintEntry(
    val id: Long = 0,
    val title: String,
    val location: String,
    val detail: String,
    val mood: Mood,
    val tags: List<String>,
    val distanceKm: Double,
    val photos: List<String>,
    val energyLevel: Int,
    val happenedOn: LocalDate,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val weather: String? = null,
    val temperature: Double? = null,
    val transportType: TransportType = TransportType.UNKNOWN,
    val carbonSavedKg: Double = 0.0
)

enum class TransportType(val label: String) {
    WALK("步行"), BIKE("骑行"), CAR("自驾"), TRAIN("铁路"), PLANE("航空"), UNKNOWN("未知")
}

data class TravelGoal(
    val id: Long = 0,
    val title: String,
    val targetLocation: String,
    val targetDate: LocalDate,
    val notes: String,
    val isCompleted: Boolean = false,
    val progress: Int = 0
)

data class FootprintSummary(
    val yearly: Stats = Stats(),
    val monthly: Stats = Stats(),
    val streakDays: Int = 0,
    val daysActiveThisYear: Int = 0
)

data class Stats(
    val totalEntries: Int = 0,
    val totalDistance: Double = 0.0,
    val uniquePlaces: Int = 0,
    val dominantMood: Mood? = null,
    val energyAverage: Double = 0.0,
    val vitalityIndex: Int = 0,
    val topLocations: List<Pair<String, Int>> = emptyList()
)

data class PeriodStats(
    val totalEntries: Int = 0,
    val totalDistance: Double = 0.0,
    val dominantMood: Mood? = null,
    val uniquePlaces: Int = 0,
    val energyAverage: Double = 0.0
)