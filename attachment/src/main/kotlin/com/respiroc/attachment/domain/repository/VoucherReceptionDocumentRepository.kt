package com.respiroc.attachment.domain.repository

import com.respiroc.attachment.domain.model.VoucherReceptionDocument
import com.respiroc.util.repository.CustomJpaRepository
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository

@Repository
interface VoucherReceptionDocumentRepository : CustomJpaRepository<VoucherReceptionDocument, Long>

fun VoucherReceptionDocumentRepository.saveWithTenantId(
    entityManager: EntityManager,
    attachmentId: Long,
    senderEmail: String?,
    tenantId: Long
): VoucherReceptionDocument {
    return entityManager.createNativeQuery(
        """
            INSERT INTO voucher_reception_documents (attachment_id, sender_email, tenant_id)
            VALUES (:attachmentId, :senderEmail, :tenantId)
            RETURNING *
        """, VoucherReceptionDocument::class.java
    )
        .setParameter("attachmentId", attachmentId)
        .setParameter("senderEmail", senderEmail)
        .setParameter("tenantId", tenantId)
        .singleResult as VoucherReceptionDocument
}