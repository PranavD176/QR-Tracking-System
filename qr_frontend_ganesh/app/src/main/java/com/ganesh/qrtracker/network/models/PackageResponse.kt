package com.ganesh.qrtracker.network.models

data class PackageResponse(
    val package_id: String,
    val description: String,
    val status: String,
    val owner_id: String?,
    val qr_payload: String?,
    val created_at: String
)
