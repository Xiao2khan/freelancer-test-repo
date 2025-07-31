package com.respiroc.supplier.domain.model

import com.respiroc.company.domain.model.Company
import com.respiroc.tenant.domain.model.Tenant
import com.respiroc.util.domain.person.PrivatePerson
import jakarta.persistence.*
import org.hibernate.annotations.TenantId

@Entity
@Table(name = "suppliers")
class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long = -1

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    var tenantId: Long? = null

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, updatable = false, insertable = false)
    lateinit var tenant: Tenant

    @Column(name = "company_id", nullable = true, updatable = false, insertable = false)
    var companyId: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = true)
    var company: Company? = null

    @Column(name = "private_person_id", nullable = true, updatable = false, insertable = false)
    var personId: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "private_person_id", nullable = true)
    var person: PrivatePerson? = null
}