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
class User : Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long = -1

    @Size(max = 128)
    @Column(name = "email", nullable = false)
    lateinit var email: String

    @Size(max = 255)
    @Column(name = "password_hash", nullable = false)
    lateinit var passwordHash: String

    @ColumnDefault("true")
    @Column(name = "is_enabled")
    var isEnabled: Boolean = true

    @ColumnDefault("false")
    @Column(name = "is_locked")
    var isLocked: Boolean = false

    @Column(name = "last_login_at")
    var lastLoginAt: Instant? = null

    @Column(name = "last_tenant_id")
    var lastTenantId: Long? = null

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: Instant

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    lateinit var updatedAt: Instant

    @ManyToMany(targetEntity = Role::class, fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var roles: List<Role> = ArrayList()

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var userTenants: MutableSet<UserTenant> = HashSet()
}
