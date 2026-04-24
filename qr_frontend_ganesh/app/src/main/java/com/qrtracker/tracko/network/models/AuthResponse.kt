package com.qrtracker.tracko.network.models

data class AuthResponse(
    val success: Boolean,
    val data: AuthData?,
    val error: String?
)

data class AuthData(
    val token: String,
    val token_type: String,
    val expires_in: Int,
    val user_id: String,
    val email: String? = null,
    val full_name: String? = null,
    val role: String? = null,
    val contact_no: String? = null
)
