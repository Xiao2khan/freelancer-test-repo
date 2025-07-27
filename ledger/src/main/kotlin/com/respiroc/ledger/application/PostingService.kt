package com.respiroc.ledger.application

import com.respiroc.ledger.application.payload.*
import com.respiroc.ledger.domain.model.AccountType
import com.respiroc.ledger.domain.repository.PostingRepository
import com.respiroc.util.context.ContextAwareApi
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Service
@Transactional
class PostingService(
    private val postingRepository: PostingRepository,
    private val accountService: AccountService
) : ContextAwareApi {

    @Transactional(readOnly = true)
    fun getTrialBalance(startDate: LocalDate, endDate: LocalDate): TrialBalanceDTO {
        val accountNumbers = postingRepository.findDistinctAccountNumbers()
        val accounts = accountService.findAllAccounts().associateBy { it.noAccountNumber }

        val trialBalanceEntries = accountNumbers.mapNotNull { accountNumber ->
            val account = accounts[accountNumber]
            if (account != null) {
                val openingBalance = postingRepository.getAccountBalanceBeforeDate(accountNumber, startDate)
                val movement = postingRepository.getAccountMovementInPeriod(accountNumber, startDate, endDate)
                val closingBalance = openingBalance + movement

                // Only include accounts that have activity or balance
                if (openingBalance.compareTo(BigDecimal.ZERO) != 0 ||
                    movement.compareTo(BigDecimal.ZERO) != 0 ||
                    closingBalance.compareTo(BigDecimal.ZERO) != 0
                ) {
                    TrialBalanceEntry(
                        accountNumber = accountNumber,
                        accountName = account.accountName,
                        openingBalance = openingBalance,
                        difference = movement,
                        closingBalance = closingBalance
                    )
                } else null
            } else null
        }

        val totalOpeningBalance = trialBalanceEntries.sumOf { it.openingBalance }
        val totalDifference = trialBalanceEntries.sumOf { it.difference }
        val totalClosingBalance = trialBalanceEntries.sumOf { it.closingBalance }

        return TrialBalanceDTO(
            entries = trialBalanceEntries,
            totalOpeningBalance = totalOpeningBalance,
            totalDifference = totalDifference,
            totalClosingBalance = totalClosingBalance
        )
    }

    @Transactional(readOnly = true)
    fun getPostingsForProfitLoss(startDate: LocalDate, endDate: LocalDate): Map<AccountType, ProfitLossDTO> {
        val accounts = accountService.findAllAccounts().associateBy { it.noAccountNumber }

        val profitLossPostings = postingRepository.findProfitLossPostings(startDate, endDate)

        val groupedByType = profitLossPostings.groupBy { row ->
            when (row[0] as String) {
                "ASSET" -> AccountType.ASSET
                "REVENUE" -> AccountType.REVENUE
                else -> AccountType.EXPENSE
            }
        }
        return groupedByType.mapValues { (_, rows) ->
            val entries = rows.mapNotNull { postingData ->
                val accountNumber = postingData[1] as String
                val amount = postingData[2] as BigDecimal
                val account = accounts[accountNumber]
                if (account != null) {
                    ProfitLossEntry(
                        accountNumber = accountNumber,
                        accountName = account.accountName,
                        amount = amount
                    )
                } else null
            }
            ProfitLossDTO(
                entries = entries,
                totalBalance = entries.sumOf { it.amount }
            )
        }
    }

    @Transactional(readOnly = true)
    fun getPostingsForBalanceSheet(startDate: LocalDate, endDate: LocalDate): Map<AccountType, BalanceSheetDTO> {
        val accounts = accountService.findAllAccounts().associateBy { it.noAccountNumber }

        val balanceSheetPostings = postingRepository.findBalanceSheetPostings(startDate, endDate)

        val groupedByType = balanceSheetPostings.groupBy { row ->
            when (row[0] as String) {
                "ASSET" -> AccountType.ASSET
                "EQUITY" -> AccountType.EQUITY
                else -> AccountType.LIABILITY
            }
        }
        return groupedByType.mapValues { (_, rows) ->
            val entries = rows.mapNotNull { postingData ->
                val accountNumber = postingData[1] as String
                val amount = postingData[2] as BigDecimal
                val account = accounts[accountNumber]
                if (account != null) {
                    BalanceSheetEntry(
                        accountNumber = accountNumber,
                        accountName = account.accountName,
                        amount = amount
                    )
                } else null
            }
            BalanceSheetDTO(
                entries = entries,
                totalBalance = entries.sumOf { it.amount }
            )
        }
    }

    @Transactional(readOnly = true)
    fun getGeneralLedger(
        startDate: LocalDate,
        endDate: LocalDate,
        accountNumber: String?
    ): GeneralLedgerPayload {
        val accounts = accountService.findAllAccounts().associateBy { it.noAccountNumber }

        val summaryData = postingRepository.getGeneralLedgerSummary(accountNumber, startDate, endDate)

        val accountEntries = summaryData.mapNotNull { row ->
            val accNumber = row[0] as String
            val openingBalance = row[1] as BigDecimal
            val periodMovement = row[2] as BigDecimal
            val transactionCount = row[3] as Long

            val account = accounts[accNumber]
            if (account != null) {
                val closingBalance = openingBalance + periodMovement

                // Only get detailed postings if needed for display
                val postings = if (transactionCount > 0) {
                    postingRepository.findPostingsByAccountAndDateRange(accNumber, startDate, endDate)
                        .map { posting ->
                            GeneralLedgerPostingEntry(
                                id = posting.id,
                                voucherId = posting.voucherId,
                                voucherNumber = posting.voucher?.getDisplayNumber(),
                                date = posting.postingDate,
                                description = posting.description,
                                vatCode = posting.vatCode,
                                currency = posting.currency,
                                amount = posting.amount,
                                originalCurrency = posting.originalCurrency,
                                originalAmount = posting.originalAmount
                            )
                        }
                } else {
                    emptyList()
                }

                GeneralLedgerAccountEntry(
                    accountNumber = accNumber,
                    accountName = account.accountName,
                    openingBalance = openingBalance,
                    postings = postings,
                    closingBalance = closingBalance
                )
            } else null
        }

        val totalAmount = accountEntries.sumOf { it.closingBalance }

        return GeneralLedgerPayload(
            accounts = accountEntries,
            totalAmount = totalAmount
        )
    }
}