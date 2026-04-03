package com.ganesh.qrtracker.network.models

data class AlertResponse(
    val alert_id: String,
    val package_id: String,
    val package_description: String,
    val scanned_by_name: String,
    val location: String,
    val status: String,
    val created_at: String
)