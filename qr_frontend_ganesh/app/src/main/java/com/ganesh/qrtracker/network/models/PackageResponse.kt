package com.ganesh.qrtracker.network.models

data class PackageResponse(
    val id: String,
    val description: String,
    val status: String,
    val current_location: String,
    val created_at: String
)
