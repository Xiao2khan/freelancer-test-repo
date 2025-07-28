package com.respiroc.user.application.payload

import com.respiroc.util.context.TenantRoleContext
import java.time.Instant

/**
 * Data Transfer Object for User information
 */
data class UserDTO(
    val id: Long,
    val email: String,
    val isEnabled: Boolean,
    val isLocked: Boolean,
    val isEnableCreateCompany: Boolean,
    val lastLoginAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val tenantRoles: List<TenantRoleContext> = emptyList()
)

/**
 * Data Transfer Object for creating a new user
 */
data class CreateUserDTO(
    val email: String,
    val password: String,
    val tenantRoleCode: String
)

/**
 * Data Transfer Object for updating a user
 */
data class UpdateUserDTO(
    val id: Long,
    val email: String,
    val isEnabled: Boolean = false,
    val isLocked: Boolean = false,
    val tenantRoleCode: String? = null,
    val password: String? = null
)
