package com.qrtracker.tracko.network.models

data class PackageResponse(
    val package_id: String,
    val description: String,
    val status: String,
    val sender_id: String?,
    val sender_name: String? = null,
    val receiver_id: String? = null,
    val receiver_name: String? = null,
    val current_holder_id: String? = null,
    val current_holder_name: String? = null,
    val route_checkpoints: List<String>? = null,
    val qr_payload: String?,
    val created_at: String? = null
)
