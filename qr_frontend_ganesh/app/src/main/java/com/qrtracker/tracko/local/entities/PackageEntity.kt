package com.qrtracker.tracko.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// This is the local database table for packages
// Mirrors the PackageResponse from the API but stored offline
@Entity(tableName = "packages")
data class PackageEntity(
    @PrimaryKey
    val package_id: String,
    val description: String,
    val status: String,
    val owner_id: String,
    val qr_payload: String,
    val created_at: String
)
