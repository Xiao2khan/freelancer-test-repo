package com.respiroc.webapp.controller

import com.respiroc.util.constant.TenantRoleCode
import com.respiroc.util.context.SpringUser
import com.respiroc.util.context.TenantInfo
import com.respiroc.util.context.UserContext
import com.respiroc.util.context.UserTenantContext
import com.respiroc.util.exception.MissingTenantContextException
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.ui.Model

open class BaseController {

    val titleAttributeName: String = "title"
    val errorMessageAttributeName: String = "error"
    val userAttributeName: String = "user"
    val tenantsAttributeName: String = "tenants"
    val currentTenantAttributeName: String = "currentTenant"
    val calloutAttributeName: String = "callout"
    val hasTenantOwnerAttributeName: String = "hasTenantOwnerRole"
    val isEnableCreateCompanyAttributeName: String = "isEnableCreateCompany"

    companion object {
        private const val JWT_TOKEN_PERIOD = 24 * 60 * 60
    }

    fun springUser(): SpringUser {
        return SecurityContextHolder.getContext().authentication.principal as SpringUser
    }

    fun user(): UserContext {
        return springUser().ctx
    }

    fun tenantId(): Long {
        return user().currentTenant?.id ?: throw MissingTenantContextException()
    }

    fun currentTenant(): UserTenantContext {
        return user().currentTenant ?: throw MissingTenantContextException()
    }

    fun tenants(): List<TenantInfo> {
        return user().tenants
    }

    fun countryCode(): String {
        return user().currentTenant?.countryCode ?: throw IllegalStateException("No current tenant is set for the user")
    }

    fun hasOwnerRole(): Boolean {
        return user().currentTenant?.roles?.any {
            it.code == TenantRoleCode.OWNER.code
        } ?: false
    }


    fun addCommonAttributesForCurrentTenant(
        model: Model,
        title: String,
    ) {
        val springUser = springUser()
        val currentTenant = currentTenant()
        val tenants = tenants()
        val hasTenantOwnerRole = hasOwnerRole()
        model.addAttribute(userAttributeName, springUser)
        model.addAttribute(currentTenantAttributeName, currentTenant)
        model.addAttribute(tenantsAttributeName, tenants)
        model.addAttribute(titleAttributeName, "${currentTenant.companyName} - $title")
        model.addAttribute(hasTenantOwnerAttributeName, hasTenantOwnerRole)
        model.addAttribute(isEnableCreateCompanyAttributeName, user().isEnableCreateCompany)
    }

    fun addCommonAttributes(
        model: Model,
        title: String,
    ) {
        val springUser = springUser()
        val tenants = tenants()

        model.addAttribute(userAttributeName, springUser)
        model.addAttribute(tenantsAttributeName, tenants)
        model.addAttribute(titleAttributeName, title)
    }

    fun isUserLoggedIn(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication != null &&
                authentication.isAuthenticated &&
                authentication !is AnonymousAuthenticationToken
    }

    fun setJwtCookie(token: String, response: HttpServletResponse) {
        val jwtCookie = Cookie("token", token)
        jwtCookie.isHttpOnly = true
        jwtCookie.secure = false // Set to true in production with HTTPS
        jwtCookie.path = "/"
        jwtCookie.maxAge = JWT_TOKEN_PERIOD
        response.addCookie(jwtCookie)
    }
}
