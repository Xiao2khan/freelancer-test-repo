package com.respiroc.webapp.controller.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * Request class for creating a new user
 */
data class CreateUserRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String,

    @field:NotBlank(message = "Role is required")
    val tenantRoleCode: String
)

/**
 * Request class for updating a user
 */
data class UpdateUserRequest(
    val id: Long,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    val isEnabled: Boolean = false,

    val isLocked: Boolean = false,

    val tenantRoleCode: String? = null,

    val password: String? = null,

    val hasTenantOwnerRole: Boolean = false

)
