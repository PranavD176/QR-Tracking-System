package com.ganesh.qrtracker.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ganesh.qrtracker.local.entities.AlertEntity
import com.ganesh.qrtracker.local.entities.FailedScanEntity
import com.ganesh.qrtracker.local.entities.PackageEntity

@Database(
    entities = [
        PackageEntity::class,
        AlertEntity::class,
        FailedScanEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Room generates the actual implementation of these DAOs automatically
    abstract fun packageDao(): PackageDao
    abstract fun alertDao(): AlertDao
    abstract fun failedScanDao(): FailedScanDao

    companion object {
        // Volatile ensures the instance is always up to date across all threads
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Singleton pattern — only one database instance exists at a time
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "qr_tracker_database"   // Name of the local DB file on device
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}