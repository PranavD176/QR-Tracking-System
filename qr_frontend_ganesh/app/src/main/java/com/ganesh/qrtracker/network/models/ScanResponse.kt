package com.ganesh.qrtracker.network.models

import android.R

data class ScanResponse(
    val result: String,                 // "Valid" or "Misplaces"
    val package_description: R.string
    val owner_name: String,
    val alert_sent: Boolean,
    val scanned_by: String             // Only present if misplaced
)
