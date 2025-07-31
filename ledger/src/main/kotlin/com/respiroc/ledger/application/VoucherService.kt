package com.respiroc.ledger.application

import com.respiroc.attachment.domain.repository.AttachmentRepository
import com.respiroc.ledger.application.payload.*
import com.respiroc.ledger.domain.model.Posting
import com.respiroc.ledger.domain.model.Voucher
import com.respiroc.ledger.domain.repository.PostingRepository
import com.respiroc.ledger.domain.repository.VoucherRepository
import com.respiroc.util.context.ContextAwareApi
import com.respiroc.util.exception.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Service
@Transactional
class VoucherService(
    private val voucherRepository: VoucherRepository,
    private val postingRepository: PostingRepository,
    private val accountService: AccountService,
    private val vatService: VatService,
    private val attachmentRepository: AttachmentRepository
) : ContextAwareApi {

    fun createVoucher(payload: CreateVoucherPayload): VoucherPayload {
        val voucherNumber = generateNextVoucherNumber(payload.date)

        if (payload.postings.isNotEmpty()) {
            validatePostingCommands(payload.postings)
            validateBalance(payload.postings)
        }

        val voucher = createVoucherEntity(payload, voucherNumber)
        val savedVoucher = voucherRepository.save(voucher)

        if (payload.postings.isNotEmpty()) {
            saveNonZeroPostings(payload.postings, savedVoucher.id)
        }

        return VoucherPayload(
            id = savedVoucher.id,
            number = savedVoucher.getDisplayNumber(),
            date = savedVoucher.date,
            description = savedVoucher.description
        )
    }

    fun findOrCreateEmptyVoucher(): VoucherPayload {
        val existingEmptyVoucher = voucherRepository.findFirstEmptyVoucher()
        if (existingEmptyVoucher != null) {
            return VoucherPayload(
                id = existingEmptyVoucher.id,
                number = existingEmptyVoucher.getDisplayNumber(),
                date = existingEmptyVoucher.date,
                description = existingEmptyVoucher.description
            )
        }

        val emptyVoucherPayload = CreateVoucherPayload(
            date = LocalDate.now(),
            description = null,
            postings = emptyList()
        )
        return createVoucher(emptyVoucherPayload)
    }


    @Transactional(readOnly = true)
    fun findVoucherSummariesByDateRange(startDate: LocalDate, endDate: LocalDate): List<VoucherSummaryPayload> {
        val vouchers = voucherRepository.findVoucherSummariesDateRange(startDate, endDate)
        return vouchers
            .filter { it.postings.isNotEmpty() }
            .map { voucher -> toVoucherSummary(voucher) }
    }

    fun updateVoucherWithPostings(payload: UpdateVoucherPayload): VoucherPayload {
        val voucher = voucherRepository.findByIdWithPostings(payload.id)
            ?: throw ResourceNotFoundException("Voucher not found")

        if (payload.postings.isNotEmpty()) {
            validatePostingCommands(payload.postings)
            validateBalance(payload.postings)
        }

        if (voucher.postings.isNotEmpty()) {
            postingRepository.deleteAll(voucher.postings)
        }

        if (payload.postings.isNotEmpty()) {
            saveNonZeroPostings(payload.postings, payload.id)
        }

        voucher.date = payload.date
        voucher.description = payload.description
        val savedVoucher = voucherRepository.save(voucher)

        return VoucherPayload(
            id = voucher.id,
            number = voucher.getDisplayNumber(),
            date = voucher.date,
            description = savedVoucher.description
        )
    }

    @Transactional(readOnly = true)
    fun findVoucherById(id: Long): Voucher? {
        return voucherRepository.findByIdWithPostings(id)
    }

    // -------------------------------
    // Private Helper Methods
    // -------------------------------

    private fun generateNextVoucherNumber(date: LocalDate): Short {
        val maxNumber = voucherRepository.findMaxVoucherNumberForYear(date.year)
        return (maxNumber + 1).toShort()
    }

    private fun validatePostingCommands(postings: List<CreatePostingPayload>) {
        // Ensure there are at least 2 non-zero postings for valid double-entry bookkeeping
        val nonZeroPostingsCount = postings.count { it.amount.compareTo(BigDecimal.ZERO) != 0 }
        if (nonZeroPostingsCount < 2) {
            throw InvalidPostingsException("There must be at least two postings and the total of debits must be equal to credits.")
        }

        postings.forEach { postingData ->
            validateAccount(postingData.accountNumber)
            validateVatCode(postingData.vatCode)
        }
    }

    private fun validateAccount(accountNumber: String) {
        if (accountService.findAccountByNumber(accountNumber) == null) {
            throw AccountNotFoundException("No account found with account number = $accountNumber")
        }
    }

    private fun validateVatCode(vatCode: String?) {
        if (vatCode != null && !vatService.vatCodeExists(vatCode)) {
            throw InvalidVatCodeException("Invalid VAT code: $vatCode")
        }
    }

    private fun validateBalance(postings: List<CreatePostingPayload>) {
        val totalAmount = postings.sumOf { it.amount }
        if (totalAmount.compareTo(BigDecimal.ZERO) != 0) {
            throw PostingsNotBalancedException("Postings must balance: total amount is $totalAmount")
        }
    }

    private fun createVoucherEntity(payload: CreateVoucherPayload, voucherNumber: Short): Voucher {
        val voucher = Voucher()
        voucher.number = voucherNumber
        voucher.date = payload.date
        voucher.description = payload.description
        return voucher
    }

    private fun createPostingEntity(
        postingData: CreatePostingPayload,
        voucherId: Long
    ): Posting {
        val posting = Posting()
        posting.accountNumber = postingData.accountNumber
        posting.amount = postingData.amount
        posting.currency = postingData.currency
        posting.postingDate = postingData.postingDate
        posting.description = postingData.description
        posting.originalAmount = postingData.originalAmount
        posting.originalCurrency = postingData.originalCurrency
        posting.vatCode = postingData.vatCode
        posting.voucherId = voucherId
        posting.rowNumber = postingData.rowNumber

        return posting
    }

    private fun saveNonZeroPostings(
        postings: List<CreatePostingPayload>,
        voucherId: Long
    ) {
        val nonZeroPostings = postings
            .filter { it.amount.compareTo(BigDecimal.ZERO) != 0 }
            .map { postingData ->
                createPostingEntity(postingData, voucherId)
            }

        if (nonZeroPostings.isNotEmpty()) {
            postingRepository.saveAll(nonZeroPostings)
        }
    }

    fun deleteVoucher(voucherId: Long) {
        val voucher = voucherRepository.findById(voucherId)
            .orElseThrow { ResourceNotFoundException("Voucher not found") }

        if (!isLastCreatedVoucher(voucherId)) {
            throw BusinessException("Only the last created voucher can be deleted")
        }

        voucherRepository.delete(voucher)
        attachmentRepository.deleteOrphanedAttachments()
    }

    fun isLastCreatedVoucher(voucherId: Long): Boolean {
        val lastVoucher = voucherRepository.findFirstByOrderByCreatedAtDesc()
        return lastVoucher?.id == voucherId
    }

    private fun toVoucherSummary(voucher: Voucher) = VoucherSummaryPayload(
        id = voucher.id,
        number = voucher.getDisplayNumber(),
        postings = voucher.postings.map { posting ->
            PostingSummaryPayload(
                id = posting.id,
                date = posting.postingDate,
                description = posting.description,
                accountNumber = posting.accountNumber,
                accountName = accountService.findAccountByNumber(posting.accountNumber)?.accountName,
                vatCode = posting.vatCode,
                amount = posting.amount
            )
        }
    )
}
