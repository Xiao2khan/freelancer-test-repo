package com.respiroc.webapp.controller.request

import com.respiroc.user.application.payload.CreateUserPayload
import com.respiroc.user.application.payload.UpdateUserPayload
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank


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

    val tenantRoleCodes: Set<String> = emptySet(),

    val password: String? = null,
)

fun UpdateUserRequest.toPayload(): UpdateUserPayload {
    return UpdateUserPayload(
        id = this.id,
        email = this.email,
        password = this.password,
        isEnabled = this.isEnabled,
        isLocked = this.isLocked,
        tenantRoleCodes = this.tenantRoleCodes,
    )
}

