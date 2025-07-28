package com.respiroc.user.domain.repository

import com.respiroc.user.domain.model.User
import com.respiroc.util.repository.CustomJpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : CustomJpaRepository<User, Long> {

    fun findByEmail(email: String): User?

    @Query("""
        SELECT user FROM User user
        LEFT JOIN FETCH user.userTenants userTenants
        LEFT JOIN FETCH user.roles roles
        LEFT JOIN FETCH userTenants.roles userTenantRoles
        LEFT JOIN FETCH userTenantRoles.tenantRole tenantRoles
        LEFT JOIN FETCH tenantRoles.tenantPermissions tenantPermissions
        WHERE user.id = :userId
        AND userTenants.tenantId= :tenantId
        """)
    fun findUserWithTenantRoles(@Param("userId") userId: Long, @Param("tenantId") tenantId: Long): User?

    @Query("""
        SELECT user FROM User user
        LEFT JOIN FETCH user.userTenants userTenants
        LEFT JOIN FETCH user.roles roles
        LEFT JOIN FETCH userTenants.roles userTenantRoles
        LEFT JOIN FETCH userTenantRoles.tenantRole tenantRoles
        LEFT JOIN FETCH tenantRoles.tenantPermissions tenantPermissions
        WHERE user.id IN :userIds
        AND userTenants.tenantId= :tenantId
        """)
    fun findUsersWithTenantRoles(@Param("userIds") userIds: List<Long>, @Param("tenantId") tenantId: Long): List<User>
}
