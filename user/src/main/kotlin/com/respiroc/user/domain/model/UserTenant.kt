package com.respiroc.user.domain.model

import com.respiroc.tenant.domain.model.Tenant
import jakarta.persistence.*

@Entity
@Table(name = "user_tenants")
class UserTenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = -1

    @Column(name = "user_id", nullable = false)
    var userId: Long = -1

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false, insertable = false)
    lateinit var user: User

    @Column(name = "tenant_id", nullable = false, updatable = false)
    var tenantId: Long = -1

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, updatable = false, insertable = false)
    lateinit var tenant: Tenant

    @OneToMany(mappedBy = "userTenant", orphanRemoval = true, fetch = FetchType.LAZY)
    var roles: MutableSet<UserTenantRole> = HashSet()
}