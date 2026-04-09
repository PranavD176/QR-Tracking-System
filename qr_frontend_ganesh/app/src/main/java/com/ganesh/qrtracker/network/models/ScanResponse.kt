package com.ganesh.qrtracker.network.models

data class ScanResponse(
    val result: String,                 // "Valid" or "Misplaced"
    val package_description: String,
    val owner_name: String,
    val alert_sent: Boolean,
    val scanned_by: String?             // Only present if misplaced
)
