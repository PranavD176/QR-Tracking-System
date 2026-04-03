package com.ganesh.qrtracker.network.models

data class LoginRequest(
    val email: String,
    val password: String
)