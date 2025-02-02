package com.ixtlan.aurora.model

data class UserResponse(
    val id: Long,
    val username: String,
    val email: String?,
    val firstName: String,
    val lastName: String,
    val roles: Set<String>
)
