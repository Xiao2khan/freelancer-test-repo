package com.respiroc.user.application.payload

import com.respiroc.util.context.TenantRoleContext
import java.time.Instant

data class UserDTO(
    val id: Long,
    val email: String,
    val isEnabled: Boolean,
    val isLocked: Boolean,
    val lastLoginAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val tenantRoles: List<TenantRoleContext> = emptyList(),
    val tenantRoleCodes : List<String> = emptyList()
)


