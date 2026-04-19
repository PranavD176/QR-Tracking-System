package com.qrtracker.tracko.network.models

data class CreatePackageRequest(
    val description: String,
    val destination_user_id: String? = null,
    val destination_address: String? = null
)
