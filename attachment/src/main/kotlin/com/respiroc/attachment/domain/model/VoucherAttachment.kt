package com.respiroc.attachment.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.TenantId
import java.time.Instant

@Entity
@Table(name = "voucher_attachments")
class VoucherAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = -1

    @Column(name = "voucher_id", nullable = false)
    var voucherId: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attachment_id", nullable = false)
    lateinit var attachment: Attachment

    @Column(name = "attachment_id", nullable = false, insertable = false, updatable = false)
    var attachmentId: Long = -1

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    var tenantId: Long? = null

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: Instant
} 