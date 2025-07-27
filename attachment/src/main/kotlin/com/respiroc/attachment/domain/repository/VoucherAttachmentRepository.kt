package com.respiroc.attachment.domain.repository

import com.respiroc.attachment.domain.model.VoucherAttachment
import com.respiroc.util.repository.CustomJpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface VoucherAttachmentRepository : CustomJpaRepository<VoucherAttachment, Long> {

    @Query("SELECT va FROM VoucherAttachment va WHERE va.voucherId = :voucherId ORDER BY va.createdAt DESC")
    fun findByVoucherId(@Param("voucherId") voucherId: Long): List<VoucherAttachment>
}