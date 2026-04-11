package com.qrtracker.tracko.network.models

data class AdminAlertResponse(
    val alert_id: String,
    val package_description: String,
    val owner_name: String,
    val scanned_by_name: String,
    val location: String,
    val created_at: String
)
