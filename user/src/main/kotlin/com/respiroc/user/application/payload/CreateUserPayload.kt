package com.respiroc.user.application.payload

/**
 * Data Transfer Object for creating a new user
 */
data class CreateUserPayload(
    val email: String,
    val password: String,
    val tenantRoleCodes: Set<String>
)
