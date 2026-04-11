package com.qrtracker.tracko.network.models

data class ScanHistoryResponse(
    val scan_id: String,
    val scanner_name: String,
    val result: String,
    val location_description: String,
    val scanned_at: String
)
