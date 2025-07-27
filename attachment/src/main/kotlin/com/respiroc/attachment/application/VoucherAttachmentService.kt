package com.respiroc.attachment.application

import com.respiroc.attachment.domain.model.Attachment
import com.respiroc.attachment.domain.model.VoucherAttachment
import com.respiroc.attachment.domain.repository.AttachmentRepository
import com.respiroc.attachment.domain.repository.VoucherAttachmentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class VoucherAttachmentService(
    private val voucherAttachmentRepository: VoucherAttachmentRepository,
    private val attachmentRepository: AttachmentRepository,
    private val attachmentService: AttachmentService
) {

    fun saveAttachment(
        voucherId: Long,
        fileData: ByteArray,
        filename: String,
        mimeType: String
    ): VoucherAttachment {

        val (pdfBytes, pdfName, pdfMime) =
            attachmentService.convertToPdf(fileData, filename, mimeType)

        val attachment = Attachment().apply {
            this.fileData = pdfBytes
            this.filename = pdfName
            this.mimetype = pdfMime
        }
        val savedAttachment = attachmentRepository.save(attachment)

        val document = VoucherAttachment().apply {
            this.attachment = savedAttachment
            this.voucherId = voucherId
        }
        return voucherAttachmentRepository.save(document)
    }

    @Transactional(readOnly = true)
    fun findAttachmentsByVoucherId(voucherId: Long): List<VoucherAttachment> {
        return voucherAttachmentRepository.findByVoucherId(voucherId)
    }

    fun deleteAttachment(attachmentId: Long) {
        voucherAttachmentRepository.findById(attachmentId).ifPresent { vt ->
            voucherAttachmentRepository.deleteById(attachmentId)
            attachmentRepository.deleteById(vt.attachmentId)
        }
    }
} 