package com.qrtracker.tracko.network.models

data class ScanResponse(
    val result: String,                 // "valid" or "misplaced"
    val package_description: String,
    val sender_name: String?,
    val alert_sent: Boolean,
    val scanned_by: String?,            // Only present if misplaced
    val status: String? = null          // Package status after scan
)
