package com.ganesh.qrtracker.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "packages")
data class PackageEntity(
    @PrimaryKey val id: String,
    val description: String,
    val status: String,
    val currentLocation: String,
    val createdAt: String
)
