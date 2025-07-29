package com.respiroc.tenant.domain.repository

import com.respiroc.tenant.domain.model.TenantRole
import com.respiroc.util.repository.CustomJpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TenantRoleRepository : CustomJpaRepository<TenantRole, Long> {

    fun findByCode(code: String): TenantRole

    fun findByCodeIn(codes: Set<String>): List<TenantRole>
}
