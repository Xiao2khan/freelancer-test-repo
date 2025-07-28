package com.respiroc.user.domain.model

import jakarta.persistence.*
import jakarta.validation.constraints.Size
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.io.Serializable
import java.time.Instant

@Entity
@Table(name = "users")
open class User : Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long = -1

    @Size(max = 128)
    @Column(name = "email", nullable = false)
    open lateinit var email: String

    @Size(max = 255)
    @Column(name = "password_hash", nullable = false)
    open lateinit var passwordHash: String

    @ColumnDefault("true")
    @Column(name = "is_enabled")
    open var isEnabled: Boolean = true

    @ColumnDefault("false")
    @Column(name = "is_locked")
    open var isLocked: Boolean = false

    @ColumnDefault("false")
    @Column(name = "is_enable_create_company")
    open var isEnableCreateCompany: Boolean = true

    @Column(name = "last_login_at")
    open var lastLoginAt: Instant? = null

    @Column(name = "last_tenant_id")
    open var lastTenantId: Long? = null

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    open lateinit var createdAt: Instant

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    open lateinit var updatedAt: Instant

    @ManyToMany(targetEntity = Role::class, fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    open var roles: List<Role> = ArrayList()

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    open var userTenants: MutableSet<UserTenant> = HashSet()
}
