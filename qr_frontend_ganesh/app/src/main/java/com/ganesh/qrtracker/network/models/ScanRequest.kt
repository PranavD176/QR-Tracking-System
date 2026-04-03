package com.ganesh.qrtracker.network.models

data class ScanRequest(
    val package_id: String,
    val location_description: String
)