package com.ixtlan.aurora.model

data class UserRegistrationRequest(
    val username: String,
    val password: String,
    val email: String,
    val firstName: String,
    val lastName: String
)