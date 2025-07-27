package com.respiroc.webapp.controller.web

import com.respiroc.ledger.application.AccountService
import com.respiroc.ledger.application.PostingService
import com.respiroc.webapp.controller.BaseController
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate

@Controller
@RequestMapping(value = ["/ledger"])
class LedgerWebController(
    private val postingService: PostingService,
    private val accountService: AccountService
) : BaseController() {

    @GetMapping(value = ["/general"])
    fun generalLedger(
        @RequestParam(name = "startDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        startDate: LocalDate?,
        @RequestParam(name = "endDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        endDate: LocalDate?,
        @RequestParam(name = "accountNumber", required = false)
        accountNumber: String?,
        model: Model
    ): String {
        val effectiveStartDate = startDate ?: LocalDate.now().withDayOfMonth(1)
        val effectiveEndDate = endDate ?: LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())

        val generalLedgerData = postingService.getGeneralLedger(effectiveStartDate, effectiveEndDate, accountNumber)

        model.addAttribute("generalLedgerData", generalLedgerData)
        model.addAttribute("startDate", effectiveStartDate)
        model.addAttribute("endDate", effectiveEndDate)
        model.addAttribute("selectedAccountNumber", accountNumber)

        addCommonAttributesForCurrentTenant(model, "General Ledger")
        val accounts = accountService.findAllAccounts().sortedBy { it.noAccountNumber }
        model.addAttribute("accounts", accounts)

        return "ledger/general"
    }

    @GetMapping(value = ["/chart-of-accounts"])
    fun chartOfAccounts(model: Model): String {
        addCommonAttributesForCurrentTenant(model, "Chart of Accounts")
        model.addAttribute("accounts", accountService.findAllAccounts())
        return "ledger/chart-of-accounts"
    }
}

@Controller
@RequestMapping("/htmx/ledger")
class LedgerHTMXController(
    private val postingService: PostingService
) : BaseController() {

    @GetMapping("/general")
    @HxRequest
    fun generalLedgerHTMX(
        @RequestParam(name = "startDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        startDate: LocalDate?,
        @RequestParam(name = "endDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        endDate: LocalDate?,
        @RequestParam(name = "accountNumber", required = false)
        accountNumber: String?,
        model: Model
    ): String {
        val effectiveStartDate = startDate ?: LocalDate.now().withDayOfMonth(1)
        val effectiveEndDate = endDate ?: LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())

        val generalLedgerData = postingService.getGeneralLedger(effectiveStartDate, effectiveEndDate, accountNumber)

        model.addAttribute("generalLedgerData", generalLedgerData)
        model.addAttribute("startDate", effectiveStartDate)
        model.addAttribute("endDate", effectiveEndDate)
        model.addAttribute("selectedAccountNumber", accountNumber)
        model.addAttribute(userAttributeName, springUser())
        model.addAttribute("companyCurrency", countryCode())

        return "ledger/general :: tableContent"
    }
}