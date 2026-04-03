package com.ganesh.qrtracker.network.models

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String // "admin" or "staff"
)