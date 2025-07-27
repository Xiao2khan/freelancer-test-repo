package com.respiroc.attachment.domain.repository

import com.respiroc.attachment.domain.model.Attachment
import com.respiroc.util.repository.CustomJpaRepository
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository

@Repository
interface AttachmentRepository : CustomJpaRepository<Attachment, Long>

fun AttachmentRepository.saveWithTenantId(
    entityManager: EntityManager,
    fileData: ByteArray,
    filename: String,
    mimetype: String,
    tenantId: Long
): Attachment {
    return entityManager.createNativeQuery(
        """
            INSERT INTO attachments (file_data, filename, mimetype, tenant_id)
            VALUES (:fileData, :filename, :mimetype, :tenantId)
            RETURNING *
        """, Attachment::class.java
    )
        .setParameter("fileData", fileData)
        .setParameter("filename", filename)
        .setParameter("mimetype", mimetype)
        .setParameter("tenantId", tenantId)
        .singleResult as Attachment
}
