package com.respiroc.tenant.domain.model

import com.respiroc.company.domain.model.Company
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.Instant

@Entity
@Table(name = "tenants")
@EntityListeners(AuditingEntityListener::class)
class Tenant : Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long = -1

    @Column(name = "slug", nullable = false, unique = true)
    var slug: String? = null

    @Column(name = "company_id", nullable = false, updatable = false, insertable = false)
    var companyId: Long = -1

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "company_id", nullable = false)
    lateinit var company: Company

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: Instant

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: Instant

    fun getCompanyName(): String {
        return company.name
    }

    fun getCompanyCountryCode(): String {
        return company.countryCode
    }
}