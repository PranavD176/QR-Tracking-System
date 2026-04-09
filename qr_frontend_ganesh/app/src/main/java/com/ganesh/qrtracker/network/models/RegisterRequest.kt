package com.ganesh.qrtracker.network.models

data class RegisterRequest(
    val full_name: String,
    val email: String,
    val password: String,
    val role: String
)
