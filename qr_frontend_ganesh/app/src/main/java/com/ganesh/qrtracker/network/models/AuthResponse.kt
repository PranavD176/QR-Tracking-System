package com.ganesh.qrtracker.network.models

data class AuthResponse(
    val token: String,
    val user_id: String,
    val role: String
)