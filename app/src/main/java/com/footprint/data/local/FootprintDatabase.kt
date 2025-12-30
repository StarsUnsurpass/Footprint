package com.footprint.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        FootprintEntity::class, 
        TravelGoalEntity::class, 
        BadgeEntity::class, 
        PrivacyFenceEntity::class,
        TrackPointEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FootprintDatabase : RoomDatabase() {
    abstract fun footprintDao(): FootprintDao
    abstract fun travelGoalDao(): TravelGoalDao
    abstract fun premiumDao(): PremiumDao
    abstract fun trackPointDao(): TrackPointDao

    companion object {
        @Volatile
        private var instance: FootprintDatabase? = null

        fun getInstance(context: Context): FootprintDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context).also { instance = it }
            }

        private fun build(context: Context): FootprintDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                FootprintDatabase::class.java,
                "footprint-db"
            ).fallbackToDestructiveMigration()
                .build()
    }
}
