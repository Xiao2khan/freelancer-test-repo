package com.respiroc.webapp.controller.web

import com.respiroc.companylookup.api.CompanyLookupInternalApi
import com.respiroc.user.application.UserService
import com.respiroc.util.exception.UnauthorizedException
import com.respiroc.util.payload.CreateCompanyPayload
import com.respiroc.webapp.controller.BaseController
import com.respiroc.webapp.controller.request.CreateCompanyRequest
import com.respiroc.webapp.service.JwtService
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping(value = ["/tenant"])
class TenantWebController : BaseController() {

    @PreAuthorize("hasRole('REGISTERED_USER')")
    @GetMapping("/create")
    fun createCompany(model: Model): String {

        model.addAttribute("createCompanyRequest", CreateCompanyRequest("", "", "NO"))
        try {
            addCommonAttributesForCurrentTenant(model, "Create Company")
            return "tenant/create"
        } catch (_: Exception) {
            addCommonAttributes(model, "Create Company")
            return "tenant/create"
        }
    }
}

@Controller
@RequestMapping("/htmx/tenant")
class TenantHTMXController(
    private val userService: UserService,
    private val companyLookupApi: CompanyLookupInternalApi,
    private val jwt: JwtService
) : BaseController() {

    @PostMapping("/create")
    @HxRequest
    fun createCompanyHTMX(
        @Valid @ModelAttribute createCompanyRequest: CreateCompanyRequest,
        @CookieValue("token", required = true) token: String,
        bindingResult: BindingResult,
        model: Model,
        response: HttpServletResponse
    ): String {
        if (bindingResult.hasErrors()) {
            model.addAttribute(
                errorMessageAttributeName,
                "Please fill in all required fields correctly."
            )
            return "fragments/error-message"
        }

        val user = user()
        val companyInfo =
            companyLookupApi.getInfo(createCompanyRequest.organizationNumber, createCompanyRequest.countryCode)
        val companyAddress = companyInfo.address
        val payload = CreateCompanyPayload(
            name = companyInfo.name,
            organizationNumber = companyInfo.registrationNumber!!,
            countryCode = companyInfo.countryCode,
            addressCountryCode = companyAddress?.countryCode,
            postalCode = companyAddress?.postalCode,
            city = companyAddress?.city,
            addressPart1 = companyAddress?.address,
            addressPart2 = null,
            administrativeDivisionCode = null
        )

        val tenant = userService.createTenantForUser(payload, user())

        userService.selectTenant(user, tenant.id)
        val token = jwt.generateToken(subject = user.id.toString(), tenantId = tenant.id)
        setJwtCookie(token, response)
        return "redirect:htmx:/"
    }
}
