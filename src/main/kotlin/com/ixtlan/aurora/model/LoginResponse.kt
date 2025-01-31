package com.ixtlan.aurora.model

data class LoginResponse(
    val token: String? = null,
    val error: String? = null,
    val message: String? = null
)