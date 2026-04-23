package com.qrtracker.tracko.network.models

data class CreatePackageRequest(
    val description: String,
    val receiver_id: String,
    val route_checkpoints: List<String>? = null
)
