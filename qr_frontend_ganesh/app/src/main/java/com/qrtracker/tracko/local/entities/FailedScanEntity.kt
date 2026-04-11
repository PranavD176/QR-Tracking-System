package com.qrtracker.tracko.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// Stores scans that failed due to no internet connection
// These will be retried automatically when network is available
@Entity(tableName = "failed_scans")
data class FailedScanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val package_id: String,
    val location_description: String,
    val attempted_at: String    // Timestamp of when the scan was attempted
)
