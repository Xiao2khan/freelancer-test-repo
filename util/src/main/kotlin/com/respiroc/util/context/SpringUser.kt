package com.respiroc.util.context

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import java.util.stream.Collectors

class SpringUser(val ctx: UserContext) : User(
    ctx.email,
    ctx.password,
    ctx.isEnabled,
    true,
    true,
    !ctx.isLocked,
    mapToGrantedAuthorities(ctx.roles, ctx.currentTenant?.roles)
) {
    companion object {

        private fun mapToGrantedAuthorities(
            roles: Collection<RoleContext>,
            tenantRoles: Collection<TenantRoleContext>?
        ): List<GrantedAuthority> {
            val authorities: MutableList<GrantedAuthority> = ArrayList()

            authorities.addAll(
                roles.stream()
                    .flatMap { role: RoleContext -> role.permissions.stream() }
                    .map { permission: PermissionContext -> SimpleGrantedAuthority(permission.code) }
                    .collect(Collectors.toList<GrantedAuthority>())
            )

            if (tenantRoles != null) {
                authorities.addAll(
                    tenantRoles.stream()
                        .flatMap { role: TenantRoleContext -> role.permissions.stream() }
                        .map { permission: TenantPermissionContext -> SimpleGrantedAuthority(permission.code) }
                        .collect(Collectors.toList<GrantedAuthority>())
                )
                authorities.addAll(
                    tenantRoles.stream()
                        .map { role: TenantRoleContext -> SimpleGrantedAuthority(("ROLE_TENANT_${role.code}")) }
                        .collect(Collectors.toList<GrantedAuthority>())
                )
            }

            return authorities
        }
    }
}
