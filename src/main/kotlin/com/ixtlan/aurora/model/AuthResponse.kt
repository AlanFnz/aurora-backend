package com.ixtlan.aurora.model

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val error: String? = null,
    val message: String? = null
)