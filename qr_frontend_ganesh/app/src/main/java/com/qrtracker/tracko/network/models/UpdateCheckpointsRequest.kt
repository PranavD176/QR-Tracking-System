package com.qrtracker.tracko.network.models

data class UpdateCheckpointsRequest(
    val route_checkpoints: List<String>
)
