package com.respiroc.user.domain.repository

import com.respiroc.user.domain.model.UserTenant
import com.respiroc.util.repository.CustomJpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserTenantRepository : CustomJpaRepository<UserTenant, Long> {
    fun findUserTenantByUserIdAndTenantId(userId: Long, tenantId: Long): UserTenant?
    
    fun findAllByTenantId(tenantId: Long): List<UserTenant>
}
