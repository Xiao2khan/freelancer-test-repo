package com.respiroc.webapp.controller.web

import com.respiroc.user.application.UserService
import com.respiroc.util.constant.TenantRoleCode
import com.respiroc.util.exception.BaseException
import com.respiroc.webapp.controller.BaseController
import com.respiroc.webapp.controller.request.CreateUserRequest
import com.respiroc.webapp.controller.request.UpdateUserRequest
import com.respiroc.webapp.controller.request.toPayload
import com.respiroc.webapp.controller.response.Callout
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping(value = ["/user"])
class UserWebController(
    private val userService: UserService
) : BaseController() {

    /**
     * Display the user listing page
     */
    @GetMapping
    fun listUsers(model: Model): String {
            val tenantId = tenantId()
            val currentUser = user()
            val hasOwnerRole = hasOwnerRole()
            val users = userService.listUserManagement(currentUser.id, tenantId, hasOwnerRole)
            val rolesExcludingOwner = TenantRoleCode.entries.filter { it != TenantRoleCode.OWNER }

            addCommonAttributesForCurrentTenant(model, "User Management")
            model.addAttribute("users", users)
            model.addAttribute("tenantRoles", rolesExcludingOwner)
            return "user/list"
    }
}

@Controller
@RequestMapping("/htmx/user")
class UserHTMXController(
    private val userService: UserService
) : BaseController() {

    /**
     * Load the edit user modal
     */
    @PreAuthorize("hasRole('TENANT_OWNER')")
    @GetMapping("/{id}/edit-modal")
    @HxRequest
    fun loadEditUserModal(@PathVariable id: Long, model: Model): String {
        val tenantId = tenantId()
        val userDto = userService.getUserById(id, tenantId)
        val hasTenantOwnerRole = userDto.tenantRoles.any { it.code == TenantRoleCode.OWNER.code }
        val rolesExcludingOwner = TenantRoleCode.entries.filter { it != TenantRoleCode.OWNER }

        addCommonAttributesForCurrentTenant(model, "User Management")
        model.addAttribute("updateUserRequest", userDto)
        model.addAttribute("tenantRoles", rolesExcludingOwner)
        model.addAttribute("hasTenantOwnerRole", hasTenantOwnerRole)

        return "user/dialog :: editUserDialog"
    }

    /**
     * Handle user creation form submission
     */
    @PostMapping("/create")
    @HxRequest
    fun createUser(
        @Valid @ModelAttribute createUserRequest: CreateUserRequest,
        bindingResult: BindingResult,
        model: Model
    ): String {
        if (bindingResult.hasErrors()) {
            addCommonAttributesForCurrentTenant(model, "User Management")
            model.addAttribute(
                calloutAttributeName,
                Callout.Error("Please fill in all required fields correctly.")
            )
            return "fragments/r-callout"
        }
        val tenantId = tenantId()
        val currentUser = user()
        userService.createUser(createUserRequest.toPayload(), tenantId)

        // Refresh the user list
        val users = userService.listUserManagement(currentUser.id, tenantId, hasOwnerRole())
        addCommonAttributesForCurrentTenant(model, "User Management")
        model.addAttribute("users", users)

        // Return the updated user table
        return "user/list :: userTable"
    }

    /**
     * Handle user update form submission
     */
    @PostMapping("/{id}/update")
    @HxRequest
    fun updateUser(
        @PathVariable id: Long,
        @Valid @ModelAttribute updateUserRequest: UpdateUserRequest,
        bindingResult: BindingResult,
        model: Model
    ): String {
        if (bindingResult.hasErrors()) {
            model.addAttribute(
                calloutAttributeName,
                Callout.Error("Please fill in all required fields correctly.")
            )
            return "fragments/r-callout"
        }

        val tenantId = tenantId()
        val currentUser = user()
        userService.updateUser(updateUserRequest.toPayload(), tenantId)
        // Refresh the user list
        val users = userService.listUserManagement(currentUser.id, tenantId, hasOwnerRole())
        addCommonAttributesForCurrentTenant(model, "User Management")
        model.addAttribute("users", users)

        // Return the updated user table
        return "user/list :: userTable"
    }

    /**
     * Refresh the user list
     */
    @GetMapping("/list")
    @HxRequest
    fun refreshUserList(model: Model): String {
            val tenantId = tenantId()
            val currentUser = user()
            val hasOwnerRole = hasOwnerRole()
            val users = userService.listUserManagement(currentUser.id, tenantId, hasOwnerRole)
            addCommonAttributesForCurrentTenant(model, "User Management")
            model.addAttribute("users", users)
            return "user/list :: userTable"
    }
}
