package com.qrtracker.tracko.network.models

data class RegisterRequest(
    val full_name: String,
    val email: String,
    val password: String,
    val contact_no: String? = null
)
