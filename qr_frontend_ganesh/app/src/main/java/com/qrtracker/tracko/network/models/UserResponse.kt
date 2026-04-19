package com.qrtracker.tracko.network.models

data class UserResponse(
    val user_id: String,
    val email: String,
    val full_name: String,
    val role: String
)
