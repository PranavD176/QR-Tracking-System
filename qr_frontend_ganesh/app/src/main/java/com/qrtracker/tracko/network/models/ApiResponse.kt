package com.qrtracker.tracko.network.models

// Standard response envelope — every API response uses this structure
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: String?
)
