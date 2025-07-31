package com.respiroc.webapp.service

import com.respiroc.ledger.application.payload.CreatePostingPayload
import com.respiroc.ledger.application.VatService
import com.respiroc.ledger.application.VoucherService
import com.respiroc.ledger.application.payload.UpdateVoucherPayload
import com.respiroc.ledger.domain.model.Posting
import com.respiroc.util.currency.CurrencyService
import com.respiroc.webapp.controller.request.CreateVoucherRequest
import com.respiroc.webapp.controller.request.PostingLine
import com.respiroc.webapp.controller.response.Callout
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class VoucherWebService(
    private val voucherService: VoucherService,
    private val vatService: VatService,
    private val currencyService: CurrencyService
) {

    companion object {
        private const val VAT_ACCOUNT_NUMBER = "2710"
    }

    fun convertPostingsToUILines(postings: List<Posting>): List<PostingLine> {
        return postings
            .groupBy { it.rowNumber }
            .toSortedMap() // Ensure proper ordering by row number
            .map { (rowNumber, rowPostings) ->
                convertRowPostingsToUILine(rowPostings, rowNumber)
            }
    }

    private fun convertRowPostingsToUILine(rowPostings: List<Posting>, rowNumber: Int): PostingLine {
        val basePostings = rowPostings.filter { it.accountNumber != VAT_ACCOUNT_NUMBER }
        val vatPostings = rowPostings.filter { it.accountNumber == VAT_ACCOUNT_NUMBER && it.vatCode == null }

        val debitPosting = basePostings.find { it.amount > BigDecimal.ZERO }
        val creditPosting = basePostings.find { it.amount < BigDecimal.ZERO }

        val hasOriginalCurrency = rowPostings.any { it.originalAmount != null && it.originalCurrency != null }

        val (displayAmount, displayCurrency) = if (hasOriginalCurrency) {
            // Use original amounts and currency
            val (debitOriginalTotal, creditOriginalTotal) = calculateOriginalTotalAmounts(basePostings, vatPostings)
            val originalAmount = maxOf(
                debitOriginalTotal?.abs() ?: BigDecimal.ZERO,
                creditOriginalTotal?.abs() ?: BigDecimal.ZERO
            )
            val originalCurrency = rowPostings.first { it.originalCurrency != null }.originalCurrency!!
            Pair(originalAmount, originalCurrency)
        } else {
            // Use amounts and company currency
            val (debitTotalAmount, creditTotalAmount) = calculateTotalAmounts(basePostings, vatPostings)
            val convertedAmount = maxOf(
                debitTotalAmount?.abs() ?: BigDecimal.ZERO,
                creditTotalAmount?.abs() ?: BigDecimal.ZERO
            )
            val companyCurrency = rowPostings.first().currency
            Pair(convertedAmount, companyCurrency)
        }

        val samplePosting = rowPostings.first()

        return PostingLine(
            debitAccount = debitPosting?.accountNumber ?: "",
            creditAccount = creditPosting?.accountNumber ?: "",
            amount = if (displayAmount > BigDecimal.ZERO) displayAmount else null,
            currency = displayCurrency,
            postingDate = samplePosting.postingDate,
            description = samplePosting.description,
            debitVatCode = debitPosting?.vatCode,
            creditVatCode = creditPosting?.vatCode,
            rowNumber = rowNumber
        )
    }

    private fun calculateTotalAmounts(
        basePostings: List<Posting>,
        vatPostings: List<Posting>
    ): Pair<BigDecimal?, BigDecimal?> {
        val debitBase = basePostings.find { it.amount > BigDecimal.ZERO }
        val creditBase = basePostings.find { it.amount < BigDecimal.ZERO }

        val debitVat = vatPostings.find { it.amount > BigDecimal.ZERO }
        val creditVat = vatPostings.find { it.amount < BigDecimal.ZERO }

        val debitTotal = if (debitBase != null) {
            debitBase.amount + (debitVat?.amount ?: BigDecimal.ZERO)
        } else null

        val creditTotal = if (creditBase != null) {
            creditBase.amount + (creditVat?.amount ?: BigDecimal.ZERO)
        } else null

        return Pair(debitTotal, creditTotal)
    }

    private fun calculateOriginalTotalAmounts(
        basePostings: List<Posting>,
        vatPostings: List<Posting>
    ): Pair<BigDecimal?, BigDecimal?> {
        val debitBase = basePostings.find { it.amount > BigDecimal.ZERO }
        val creditBase = basePostings.find { it.amount < BigDecimal.ZERO }

        val debitVat = vatPostings.find { it.amount > BigDecimal.ZERO }
        val creditVat = vatPostings.find { it.amount < BigDecimal.ZERO }

        val debitTotal = if (debitBase != null) {
            val baseOriginal = debitBase.originalAmount ?: debitBase.amount
            val vatOriginal = debitVat?.originalAmount ?: debitVat?.amount ?: BigDecimal.ZERO
            baseOriginal + vatOriginal
        } else null

        val creditTotal = if (creditBase != null) {
            val baseOriginal = creditBase.originalAmount ?: creditBase.amount
            val vatOriginal = creditVat?.originalAmount ?: creditVat?.amount ?: BigDecimal.ZERO
            baseOriginal + vatOriginal
        } else null

        return Pair(debitTotal, creditTotal)
    }

    fun updateVoucherWithPostings(
        voucherId: Long,
        request: CreateVoucherRequest,
        companyCurrencyCode: String
    ) {
        val validPostingLines = request.getValidPostingLines()
        val postingCommands = convertToPostingCommands(validPostingLines, companyCurrencyCode)

        voucherService.updateVoucherWithPostings(UpdateVoucherPayload(
            id = voucherId,
            date = request.voucherDate,
            description = request.voucherDescription,
            postings = postingCommands
        ))
    }

    private fun convertToPostingCommands(
        postingLines: List<PostingLine>,
        companyCurrency: String
    ): List<CreatePostingPayload> {
        return postingLines.flatMapIndexed { index, line ->
            val originalAmount = line.amount!!
            val originalCurrency = line.currency
            // Always use the array index to ensure consistent row numbering after deletions
            val baseRowNumber = index

            val hasDebit = line.debitAccount.isNotBlank()
            val hasCredit = line.creditAccount.isNotBlank()

            when {
                hasDebit && hasCredit -> {
                    // Both debit and credit filled - create two postings with same row number
                    val debitLine = line.copy(
                        creditAccount = "", 
                        creditVatCode = null,
                        rowNumber = baseRowNumber
                    )
                    val creditLine = line.copy(
                        debitAccount = "", 
                        debitVatCode = null,
                        rowNumber = baseRowNumber
                    )

                    val debitCommands =
                        createCommandsForLine(debitLine, originalAmount, originalCurrency, companyCurrency, baseRowNumber)
                    val creditCommands =
                        createCommandsForLine(creditLine, originalAmount, originalCurrency, companyCurrency, baseRowNumber)

                    debitCommands + creditCommands
                }

                hasDebit || hasCredit -> {
                    // Only one side filled
                    createCommandsForLine(line, originalAmount, originalCurrency, companyCurrency, baseRowNumber)
                }

                else -> {
                    emptyList()
                }
            }
        }
    }

    private fun createCommandsForLine(
        line: PostingLine,
        originalAmount: BigDecimal,
        originalCurrency: String,
        companyCurrency: String,
        rowNumber: Int
    ): List<CreatePostingPayload> {
        val vatCode = line.getVatCode()

        return if (vatCode != null) {
            // Create two postings: one for base amount and one for VAT
            createVatPostings(line, originalAmount, originalCurrency, companyCurrency, vatCode, rowNumber)
        } else {
            // Create single posting without VAT
            listOf(createSinglePosting(line, originalAmount, originalCurrency, companyCurrency, null, rowNumber))
        }
    }

    private fun createVatPostings(
        line: PostingLine,
        originalAmount: BigDecimal,
        originalCurrency: String,
        companyCurrency: String,
        vatCode: String,
        rowNumber: Int
    ): List<CreatePostingPayload> {
        val vatCodeEntity = vatService.findVatCodeByCode(vatCode)
            ?: throw IllegalArgumentException("Invalid VAT code: $vatCode")

        val totalSignedAmount = calculateConvertedSignedAmount(line, originalCurrency, companyCurrency)
        val totalAbsAmount = totalSignedAmount.abs()

        val baseAmount = vatService.calculateBaseAmountFromVatInclusive(totalAbsAmount, vatCodeEntity)
        val vatAmount = vatService.calculateVatAmount(baseAmount, vatCodeEntity)

        val signedBaseAmount = if (totalSignedAmount < BigDecimal.ZERO) baseAmount.negate() else baseAmount
        val signedVatAmount = if (totalSignedAmount < BigDecimal.ZERO) vatAmount.negate() else vatAmount

        // Calculate original amounts if currency conversion is needed
        val (originalBaseAmount, originalVatAmount) = if (originalCurrency != companyCurrency) {
            val originalAbsAmount = originalAmount.abs()
            val originalBase = vatService.calculateBaseAmountFromVatInclusive(originalAbsAmount, vatCodeEntity)
            val originalVat = vatService.calculateVatAmount(originalBase, vatCodeEntity)

            val signedOriginalBase = if (originalAmount < BigDecimal.ZERO) originalBase.negate() else originalBase
            val signedOriginalVat = if (originalAmount < BigDecimal.ZERO) originalVat.negate() else originalVat

            Pair(signedOriginalBase, signedOriginalVat)
        } else {
            Pair(null, null)
        }

        val basePosting = CreatePostingPayload(
            accountNumber = line.getAccountNumber(),
            amount = signedBaseAmount,
            currency = companyCurrency,
            postingDate = line.postingDate,
            description = line.description,
            originalAmount = originalBaseAmount,
            originalCurrency = if (originalCurrency != companyCurrency) originalCurrency else null,
            vatCode = vatCode,
            rowNumber = rowNumber
        )

        val vatPosting = CreatePostingPayload(
            accountNumber = VAT_ACCOUNT_NUMBER, // 2710 hard-coded
            amount = signedVatAmount,
            currency = companyCurrency,
            postingDate = line.postingDate,
            description = line.description,
            originalAmount = originalVatAmount,
            originalCurrency = if (originalCurrency != companyCurrency) originalCurrency else null,
            vatCode = null,
            rowNumber = rowNumber
        )

        return listOf(basePosting, vatPosting)
    }

    private fun createSinglePosting(
        line: PostingLine,
        originalAmount: BigDecimal,
        originalCurrency: String,
        companyCurrency: String,
        vatCode: String?,
        rowNumber: Int
    ): CreatePostingPayload {
        return CreatePostingPayload(
            accountNumber = line.getAccountNumber(),
            amount = calculateConvertedSignedAmount(line, originalCurrency, companyCurrency),
            currency = companyCurrency,
            postingDate = line.postingDate,
            description = line.description,
            originalAmount = if (originalCurrency != companyCurrency) originalAmount else null,
            originalCurrency = if (originalCurrency != companyCurrency) originalCurrency else null,
            vatCode = vatCode,
            rowNumber = rowNumber
        )
    }

    private fun calculateConvertedSignedAmount(
        line: PostingLine,
        originalCurrency: String,
        companyCurrency: String
    ): BigDecimal {
        val signedAmount = line.getSignedAmount()
        val convertedAmount = if (originalCurrency == companyCurrency) {
            signedAmount
        } else {
            currencyService.convertCurrency(signedAmount, originalCurrency, companyCurrency)
        }
        return convertedAmount.setScale(2, java.math.RoundingMode.HALF_UP)
    }
}
