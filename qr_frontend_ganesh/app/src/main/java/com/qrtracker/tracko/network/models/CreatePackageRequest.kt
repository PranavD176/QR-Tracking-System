package com.qrtracker.tracko.network.models

import com.qrtracker.tracko.ui.packages.RouteCheckpoint

data class CreatePackageRequest(
    val description: String,
    val destination_user_id: String? = null,
    val destination_address: String? = null,
    val route_checkpoints: List<RouteCheckpoint>? = null
)
