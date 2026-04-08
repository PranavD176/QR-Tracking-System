package com.ganesh.qrtracker.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// This is the local database table for alerts
@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey
    val alert_id: String,
    val package_id: String,
    val package_description: String,
    val scanned_by_name: String,
    val location: String,
    val status: String,
    val created_at: String
)