package com.ixtlan.aurora.model

data class LoginResponse(
    val token: String,
    val user: UserResponse
)