package com.respiroc.ledger.domain.model

import com.respiroc.tenant.domain.model.Tenant
import jakarta.persistence.*
import jakarta.persistence.Table
import org.hibernate.annotations.*
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "vouchers")
class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long = -1

    @Column(name = "number", nullable = false)
    var number: Short = 0

    @Column(name = "date", nullable = false)
    lateinit var date: LocalDate

    @Column(name = "description", length = Integer.MAX_VALUE)
    var description: String? = null

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    var tenantId: Long? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "tenant_id", nullable = false, updatable = false, insertable = false)
    lateinit var tenant: Tenant

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: Instant

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: Instant

    @OneToMany(mappedBy = "voucher")
    var postings: MutableSet<Posting> = mutableSetOf()

    fun getDisplayNumber(): String {
        return "${number}-${date.year}"
    }
}