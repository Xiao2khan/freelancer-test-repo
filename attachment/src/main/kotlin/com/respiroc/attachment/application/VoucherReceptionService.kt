package com.respiroc.attachment.application

import com.respiroc.attachment.domain.model.Attachment
import com.respiroc.attachment.domain.model.VoucherReceptionDocument
import com.respiroc.attachment.domain.repository.AttachmentRepository
import com.respiroc.attachment.domain.repository.VoucherReceptionDocumentRepository
import com.respiroc.attachment.domain.repository.saveWithTenantId
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class VoucherReceptionService(
    private val voucherDocumentRepository: VoucherReceptionDocumentRepository,
    private val attachmentRepository: AttachmentRepository,
    private val attachmentService: AttachmentService,
    @PersistenceContext private val entityManager: EntityManager
) {

    fun saveDocument(
        fileData: ByteArray,
        filename: String,
        mimeType: String,
        senderEmail: String
    ): VoucherReceptionDocument {

        val (pdfBytes, pdfName, pdfMime) =
            attachmentService.convertToPdf(fileData, filename, mimeType)

        val attachment = Attachment().apply {
            this.fileData = pdfBytes
            this.filename = pdfName
            this.mimetype = pdfMime
        }
        val savedAttachment = attachmentRepository.save(attachment)

        val document = VoucherReceptionDocument().apply {
            this.attachment = savedAttachment
            this.senderEmail = senderEmail
        }
        return voucherDocumentRepository.save(document)
    }

    fun saveDocumentByTenantId(
        fileData: ByteArray,
        filename: String,
        mimeType: String,
        senderEmail: String,
        tenantId: Long
    ): VoucherReceptionDocument {

        val (pdfBytes, pdfName, pdfMime) =
            attachmentService.convertToPdf(fileData, filename, mimeType)

        val savedAttachment = attachmentRepository.saveWithTenantId(
            entityManager = entityManager,
            fileData = pdfBytes,
            filename = pdfName,
            mimetype = pdfMime,
            tenantId = tenantId
        )

        return voucherDocumentRepository.saveWithTenantId(
            entityManager = entityManager,
            attachmentId = savedAttachment.id!!,
            senderEmail = senderEmail,
            tenantId = tenantId
        )
    }
}