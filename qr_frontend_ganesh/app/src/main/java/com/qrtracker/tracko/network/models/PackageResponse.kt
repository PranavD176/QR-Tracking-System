package com.qrtracker.tracko.network.models

data class PackageResponse(
    val package_id: String,
    val description: String,
    val status: String,
    val owner_id: String?,
    val destination_user_id: String? = null,
    val destination_address: String? = null,
    val qr_payload: String?,
    val created_at: String? = null
)

