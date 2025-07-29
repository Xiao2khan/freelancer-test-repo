package com.respiroc.webapp.controller.request

import com.respiroc.user.application.payload.CreateUserPayload
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class CreateUserRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String,

    @field:NotEmpty(message = "Roles is required")
    val tenantRoleCodes: Set<String>
)

fun CreateUserRequest.toPayload(): CreateUserPayload {
    return CreateUserPayload(
        email = this.email,
        password = this.password,
        tenantRoleCodes = this.tenantRoleCodes,
    )
}

