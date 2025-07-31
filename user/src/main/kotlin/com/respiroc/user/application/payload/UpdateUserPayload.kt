package com.respiroc.user.application.payload

data class UpdateUserPayload(
    val id: Long,
    val email: String,
    val isEnabled: Boolean = false,
    val isLocked: Boolean = false,
    val tenantRoleCodes: Set<String> = emptySet(),
    val password: String? = null
)


