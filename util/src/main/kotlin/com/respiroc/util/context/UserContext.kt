package com.respiroc.util.context

data class UserContext(
    val id: Long,
    val email: String,
    val password: String,
    val isEnabled: Boolean,
    val isLocked: Boolean,
    val currentTenant: UserTenantContext?,
    val tenants: List<TenantInfo>,
    val roles: List<RoleContext>,
)
