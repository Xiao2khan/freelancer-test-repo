package com.respiroc.webapp.controller.web

import com.respiroc.companylookup.api.CompanyLookupInternalApi
import com.respiroc.webapp.controller.BaseController
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/htmx/company")
class CompanyHTMXController(
    private val companyLookupApi: CompanyLookupInternalApi
) : BaseController() {

    @GetMapping("/search")
    @HxRequest
    fun searchCompaniesHTMX(
        @RequestParam name: String,
        @RequestParam(defaultValue = "NO") countryCode: String,
        model: Model
    ): String {
        val query = name.trim()

        if (query.length < 2) {
            return "fragments/company-search :: empty"
        }

        val searchResult = companyLookupApi.search(query, countryCode)
        model.addAttribute("companies", searchResult.companies.take(10))
        return "fragments/company-search :: results"
    }
}