package com.respiroc.webapp.controller.web

import com.respiroc.user.application.UserService
import com.respiroc.user.application.payload.CreateUserDTO
import com.respiroc.user.application.payload.UpdateUserDTO
import com.respiroc.util.constant.TenantRoleCode
import com.respiroc.util.exception.UnauthorizedException
import com.respiroc.webapp.config.annotation.REQUIRE_PERMISSION_ALL_WRITE
import com.respiroc.webapp.controller.BaseController
import com.respiroc.webapp.controller.request.CreateUserRequest
import com.respiroc.webapp.controller.request.UpdateUserRequest
import com.respiroc.webapp.controller.response.Callout
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import jakarta.validation.Valid
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
        try {
            val tenantId = tenantId()
            val currentUser = user()
            val hasOwnerRole = hasOwnerRole()
            val users = userService.listUserManagement(currentUser.id, tenantId, hasOwnerRole)

            addCommonAttributesForCurrentTenant(model, "User Management")
            model.addAttribute("users", users)
            model.addAttribute("canCreateUser", hasOwnerRole)
            model.addAttribute("tenantRoles", TenantRoleCode.values())

            return "user/list"
        } catch (e: Exception) {
            addCommonAttributesForCurrentTenant(model, "User Management")
            model.addAttribute(errorMessageAttributeName, "Error loading users: ${e.message}")
            return "user/list"
        }
    }

    /**
     * Display the user edit form
     */
    @GetMapping("/{id}/edit")
    fun editUserForm(@PathVariable id: Long, model: Model): String {
        try {
            // Check if the current user has the Owner role
            val hasOwnerRole = hasOwnerRole()

            if (!hasOwnerRole) {
                throw UnauthorizedException("Only users with the Owner role can edit users")
            }

            val tenantId = tenantId()
            val userDto = userService.getUserById(id, tenantId)

            val updateUserRequest = UpdateUserRequest(
                id = userDto.id,
                email = userDto.email,
                isEnabled = userDto.isEnabled,
                isLocked = userDto.isLocked,
                tenantRoleCode = userDto.tenantRoles.firstOrNull()?.code,
                hasTenantOwnerRole = userDto.tenantRoles.any { it.code == TenantRoleCode.OWNER.code }
            )

            addCommonAttributesForCurrentTenant(model, "Edit User")
            model.addAttribute("updateUserRequest", updateUserRequest)
            model.addAttribute("tenantRoles", TenantRoleCode.values())

            return "user/edit"
        } catch (e: UnauthorizedException) {
            addCommonAttributesForCurrentTenant(model, "Unauthorized")
            model.addAttribute(calloutAttributeName, e.message)
            return "fragments/callout-message"
        } catch (e: Exception) {
            addCommonAttributesForCurrentTenant(model, "Error")
            model.addAttribute(calloutAttributeName, "Error: ${e.message}")
            return "fragments/callout-message"
        }
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
    @GetMapping("/{id}/edit-modal")
    @HxRequest
    fun loadEditUserModal(@PathVariable id: Long, model: Model): String {
        try {
            // Check if the current user has the Owner role
            val hasOwnerRole = hasOwnerRole()

            if (!hasOwnerRole) {
                throw UnauthorizedException("Only users with the Owner role can edit users")
            }

            val tenantId = tenantId()
            val userDto = userService.getUserById(id, tenantId)

            val updateUserRequest = UpdateUserRequest(
                id = userDto.id,
                email = userDto.email,
                isEnabled = userDto.isEnabled,
                isLocked = userDto.isLocked,
                tenantRoleCode = userDto.tenantRoles.firstOrNull()?.code,
                hasTenantOwnerRole = userDto.tenantRoles.any { it.code == TenantRoleCode.OWNER.code }
            )
            addCommonAttributesForCurrentTenant(model, "User Management")
            model.addAttribute("updateUserRequest", updateUserRequest)
            model.addAttribute("tenantRoles", TenantRoleCode.values())

            return "user/dialog :: editUserDialog"
        } catch (e: Exception) {
            addCommonAttributesForCurrentTenant(model, "User Management")
            model.addAttribute(
                calloutAttributeName,
                Callout.Error("Error: ${e.message}")
            )
            return "fragments/callout-message"
        }
    }

    /**
     * Handle user creation form submission
     */
    @REQUIRE_PERMISSION_ALL_WRITE
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
            return "fragments/callout-message"
        }
        try {
            val tenantId = tenantId()
            val currentUser = user()
            val hasOwnerRole = hasOwnerRole()

            val createUserDTO = CreateUserDTO(
                email = createUserRequest.email,
                password = createUserRequest.password,
                tenantRoleCode = createUserRequest.tenantRoleCode
            )

            userService.createUser(createUserDTO, tenantId, hasOwnerRole)

            // Refresh the user list
            val users = userService.listUserManagement(currentUser.id, tenantId, hasOwnerRole)
            addCommonAttributesForCurrentTenant(model, "User Management")
            model.addAttribute("users", users)
            model.addAttribute("canCreateUser", hasOwnerRole)

            // Return the updated user table
            return "user/list :: userTable"

        } catch (e: Exception) {
            model.addAttribute(
                calloutAttributeName,
                Callout.Error("Failed to create user: ${e.message}")
            )
            return "fragments/callout-message"
        }
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
            return "fragments/callout-message"
        }

        try {
            val tenantId = tenantId()
            val hasOwnerRole = hasOwnerRole()
            val currentUser = user()
            val updateUserDTO = UpdateUserDTO(
                id = updateUserRequest.id,
                email = updateUserRequest.email,
                isEnabled = updateUserRequest.isEnabled,
                isLocked = updateUserRequest.isLocked,
                tenantRoleCode = updateUserRequest.tenantRoleCode,
                password = updateUserRequest.password
            )

            userService.updateUser(updateUserDTO, user(), tenantId, hasOwnerRole)
            // Refresh the user list
            val users = userService.listUserManagement(currentUser.id, tenantId, hasOwnerRole)
            addCommonAttributesForCurrentTenant(model, "User Management")
            model.addAttribute("users", users)
            model.addAttribute("canCreateUser", hasOwnerRole)

            // Return the updated user table
            return "user/list :: userTable"

        } catch (e: Exception) {
            addCommonAttributesForCurrentTenant(model, "User Management")
            model.addAttribute(
                calloutAttributeName,
                Callout.Error("Failed to update user: ${e.message}")
            )
            return "fragments/callout-message"
        }
    }

    /**
     * Refresh the user list
     */
    @GetMapping("/list")
    @HxRequest
    fun refreshUserList(model: Model): String {
        try {
            val tenantId = tenantId()
            val currentUser = user()
            val hasOwnerRole = hasOwnerRole()
            val users = userService.listUserManagement(currentUser.id, tenantId, hasOwnerRole)
            addCommonAttributesForCurrentTenant(model, "User Management")
            model.addAttribute("users", users)
            model.addAttribute("canCreateUser", hasOwnerRole)

            return "user/list :: userTable"
        } catch (e: Exception) {
            addCommonAttributesForCurrentTenant(model, "User Management")
            model.addAttribute(
                calloutAttributeName,
                Callout.Error("Error loading users: ${e.message}")
            )
            return "fragments/callout-message"
        }
    }
}
