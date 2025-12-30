package com.footprint

import android.app.Application
import com.amap.api.location.AMapLocationClient
import com.amap.api.maps.MapsInitializer
import com.footprint.data.local.FootprintDatabase
import com.footprint.data.repository.FootprintRepository

class FootprintApplication : Application() {
    lateinit var repository: FootprintRepository
        private set

    override fun onCreate() {
        super.onCreate()
        
        // --- 核心修复：全局最早期隐私确认 ---
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)
        AMapLocationClient.updatePrivacyShow(this, true, true)
        AMapLocationClient.updatePrivacyAgree(this, true)

        val database = FootprintDatabase.getInstance(this)
        repository = FootprintRepository(
            database.footprintDao(), 
            database.travelGoalDao(),
            database.trackPointDao()
        )
    }
}