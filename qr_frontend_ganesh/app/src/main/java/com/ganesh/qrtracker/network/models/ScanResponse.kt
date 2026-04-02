package com.ganesh.qrtracker.network.models

data class ScanResponse(
    val success: Boolean,
    val message: String,
    val status: String // "valid" or "misplaced"
)