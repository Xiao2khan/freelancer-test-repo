package com.respiroc.tenant.application


import com.respiroc.company.application.CompanyService
import com.respiroc.util.payload.CreateCompanyPayload
import com.respiroc.company.domain.model.Company
import com.respiroc.tenant.domain.model.Tenant
import com.respiroc.tenant.domain.model.TenantRole
import com.respiroc.tenant.domain.repository.TenantRepository
import com.respiroc.tenant.domain.repository.TenantRoleRepository
import com.respiroc.util.constant.TenantRoleCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TenantService(
    private val companyService: CompanyService,
    private val tenantRepository: TenantRepository,
    private val tenantRoleRepository: TenantRoleRepository
) {

    fun createNewTenant(companyId: Long): Tenant {
        val tenant = Tenant()
        val company = Company()
        company.id = companyId
        tenant.company = company
        tenant.slug = generateSlug(company.name)
        return tenantRepository.saveAndFlush(tenant)
    }

    fun createNewTenant(payload: CreateCompanyPayload): Tenant {
        val company = companyService.getOrCreateCompany(payload)
        val tenant = Tenant()
        tenant.company = company
        tenant.slug = generateSlug(company.name)
        return tenantRepository.saveAndFlush(tenant)
    }

    fun findTenantBySlug(slug: String): Tenant? {
        return tenantRepository.findBySlug(slug)
    }

    /**
     * Find a tenant by ID
     */
    fun findTenantById(id: Long): Tenant {
        return tenantRepository.findById(id).orElseThrow {
            IllegalArgumentException("Tenant with ID $id not found")
        }
    }

    fun findTenantRoleByCode(role: TenantRoleCode): TenantRole {
        return tenantRoleRepository.findByCode(role.code)
    }

    fun findTenantRolesByCodes(tenantRoleCodes: Set<String>): List<TenantRole> {
        return tenantRoleRepository.findByCodeIn(tenantRoleCodes)
    }

    private fun generateSlug(name: String): String {
        val slug = name
            .lowercase()
            .replace(" ", "-")
            .replace(Regex("[^a-z0-9-]"), "")
            .replace(Regex("-+"), "-")
            .trim('-')
        var finalSlug = slug
        var counter = 1
        while (tenantRepository.findBySlug(finalSlug) != null) {
            finalSlug = "$slug-${counter++}"
        }
        return finalSlug
    }
}
