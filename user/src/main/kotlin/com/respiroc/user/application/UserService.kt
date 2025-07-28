package com.respiroc.user.application

import com.respiroc.tenant.application.TenantService
import com.respiroc.tenant.domain.model.Tenant
import com.respiroc.tenant.domain.model.TenantPermission
import com.respiroc.tenant.domain.model.TenantRole
import com.respiroc.user.application.payload.CreateUserDTO
import com.respiroc.user.application.payload.LoginPayload
import com.respiroc.user.application.payload.UpdateUserDTO
import com.respiroc.user.application.payload.UserDTO
import com.respiroc.user.domain.model.*
import com.respiroc.user.domain.repository.UserRepository
import com.respiroc.user.domain.repository.UserTenantRepository
import com.respiroc.user.domain.repository.UserTenantRoleRepository
import com.respiroc.util.constant.TenantRoleCode
import com.respiroc.util.context.*
import com.respiroc.util.currency.CurrencyService
import com.respiroc.util.exception.UnauthorizedException
import com.respiroc.util.exception.AuthenticationException
import com.respiroc.util.exception.ResourceAlreadyExistsException
import com.respiroc.util.payload.CreateCompanyPayload
import org.springframework.security.authentication.AccountStatusUserDetailsChecker
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import kotlin.jvm.optionals.getOrNull
import kotlin.collections.listOf

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val tenantService: TenantService,
    private val userTenantRoleRepository: UserTenantRoleRepository,
    private val userTenantRepository: UserTenantRepository,
    private val currencyService: CurrencyService
) {

    private val passwordEncoder = BCryptPasswordEncoder()

    /**
     * Create a new user with email, password, and role
     * Only users with the Owner role can create new users
     */
    fun createUser(createUserDTO: CreateUserDTO, tenantId: Long, hasOwnerRole: Boolean): UserDTO {

        if (!hasOwnerRole) {
            throw UnauthorizedException("Only users with the Owner role can create new users")
        }

        // Check if user with the same email already exists
        val existingUser = userRepository.findByEmail(createUserDTO.email)
        if (existingUser != null) {
            throw IllegalArgumentException("User with email ${createUserDTO.email} already exists")
        }

        // Create the new user
        val newUser = User()
        newUser.email = createUserDTO.email
        newUser.passwordHash = passwordEncoder.encode(createUserDTO.password)
        newUser.lastTenantId = tenantId
        newUser.isEnableCreateCompany = false
        val savedUser = userRepository.save(newUser)

        // Assign the role to the user
        val tenantRole = tenantService.findTenantRoleByCode(TenantRoleCode.valueOf(createUserDTO.tenantRoleCode))
        val tenant = tenantService.findTenantById(tenantId)

        // Create a UserContext for the new user
        val userContext = UserContext(
            id = savedUser.id,
            email = savedUser.email,
            password = savedUser.passwordHash,
            isEnabled = savedUser.isEnabled,
            isLocked = savedUser.isLocked,
            isEnableCreateCompany = savedUser.isEnableCreateCompany,
            currentTenant = null,
            tenants = emptyList(),
            roles = emptyList()
        )

        addUserTenantRole(tenant, tenantRole, userContext)

        // Return the user DTO
        return getUserById(savedUser.id, tenantId)
    }

    fun listUserManagement(currentUserId: Long, tenantId: Long, hasOwnerRole: Boolean): List<UserDTO> {
        if (hasOwnerRole) {
            return getAllUsersByTenantId(tenantId)
        } else {
            return listOf(getUserById(currentUserId, tenantId))
        }
    }

    /**
     * Get all users for a tenant
     */
    fun getAllUsersByTenantId(tenantId: Long): List<UserDTO> {
        // Get all UserTenant entities for the specified tenant
        val userTenants = userTenantRepository.findAllByTenantId(tenantId)

        // Extract all user IDs
        val userIds = userTenants.map { it.userId }

        // No users found for this tenant
        if (userIds.isEmpty()) {
            return emptyList()
        }

        val users = userRepository.findAllById(userIds)
            .associateBy { it.id }

        // Fetch all tenant roles for all users in a single query
        val userTenantRolesMap = findTenantRolesForUsers(userIds, tenantId)

        // Map UserTenant entities to UserDTO objects
        return userTenants.mapNotNull { userTenant ->
            val user = users[userTenant.userId] ?: return@mapNotNull null

            UserDTO(
                id = user.id,
                email = user.email,
                isEnabled = user.isEnabled,
                isLocked = user.isLocked,
                isEnableCreateCompany = user.isEnableCreateCompany,
                lastLoginAt = user.lastLoginAt,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt,
                tenantRoles = userTenantRolesMap[user.id] ?: emptyList()
            )
        }
    }

    /**
     * Get a user by ID
     */
    fun getUserById(userId: Long, tenantId: Long): UserDTO {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("User with ID $userId not found")
        }

        return UserDTO(
            id = user.id,
            email = user.email,
            isEnabled = user.isEnabled,
            isLocked = user.isLocked,
            isEnableCreateCompany = user.isEnableCreateCompany,
            lastLoginAt = user.lastLoginAt,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
            tenantRoles = findTenantRoles(user.id, tenantId)
        )
    }

    /**
     * Update a user
     * Only users with the Owner role can update users
     */
    fun updateUser(updateUserDTO: UpdateUserDTO, currentUser: UserContext, tenantId: Long, hasOwnerRole: Boolean): UserDTO {

        if (!hasOwnerRole) {
            throw UnauthorizedException("Only users with the Owner role can update users")
        }

        // Get the user to update
        val user = userRepository.findById(updateUserDTO.id).orElseThrow {
            IllegalArgumentException("User with ID ${updateUserDTO.id} not found")
        }

        // Update the user
        user.apply {
            email = updateUserDTO.email
            isEnabled = updateUserDTO.isEnabled
            isLocked = updateUserDTO.isLocked

            // Update password if provided
            updateUserDTO.password?.takeUnless { it.isBlank() }?.let {
                passwordHash = passwordEncoder.encode(it)
            }
        }

        val savedUser = userRepository.save(user)

        // Update the user's role if provided
        if (updateUserDTO.tenantRoleCode != null) {
            // Get the current tenant roles for the user
            val currentTenantRoles = findTenantRoles(user.id, tenantId)
            val currentRoleCode = currentTenantRoles.firstOrNull()?.code

            // Only update if the role is actually changing
            if (updateUserDTO.tenantRoleCode != currentRoleCode) {
                // Get the tenant role
                val tenantRole = tenantService.findTenantRoleByCode(TenantRoleCode.valueOf(updateUserDTO.tenantRoleCode))
                val tenant = tenantService.findTenantById(tenantId)

                // Get the user tenant
                val userTenant = getOrCreateUserTenant(user.id, tenantId)

                // Remove existing tenant roles for this user in this tenant
                userTenant.roles.toList().forEach { role ->
                    userTenantRoleRepository.delete(role)
                }

                // Create a UserContext for the user
                val userContext = UserContext(
                    id = user.id,
                    email = user.email,
                    password = user.passwordHash,
                    isEnabled = user.isEnabled,
                    isLocked = user.isLocked,
                    isEnableCreateCompany = user.isEnableCreateCompany,
                    currentTenant = null,
                    tenants = emptyList(),
                    roles = emptyList()
                )

                // Add the new role
                addUserTenantRole(tenant, tenantRole, userContext)
            }
        }

        // Return the updated user DTO
        return getUserById(savedUser.id, tenantId)
    }

    fun signupByEmailPassword(email: String, password: String): LoginPayload {
        val existUser = userRepository.findByEmail(email)
        if (existUser != null) throw ResourceAlreadyExistsException("User already exists")

        val newUser = User()
        newUser.email = email
        newUser.passwordHash = passwordEncoder.encode(password)
        return signup(newUser)
    }

    fun loginByEmailPassword(
        email: String,
        password: String
    ): LoginPayload {
        val user = userRepository.findByEmail(email) ?: throw AuthenticationException("Email not found")
        if (!passwordEncoder.matches(password, user.passwordHash)) throw AuthenticationException("Login incorrect")

        return login(user)
    }

    fun selectTenant(user: UserContext, tenatId: Long) {
        val userDb = userRepository.findById(user.id).get()
        userDb.lastTenantId = tenatId
        userRepository.save(userDb)
    }

    fun findByIdAndTenantId(id: Long, tenantId: Long?): UserContext? {
        return userRepository.findById(id).getOrNull()?.toUserContext(tenantId)
    }

    fun findTenantRoles(userId: Long, tenantId: Long): List<TenantRoleContext> {
        // Get the user with tenant roles
        val user = userRepository.findUserWithTenantRoles(userId, tenantId) ?: return emptyList()

        // Find the UserTenant with the specified tenantId, or return empty list if not found
        val userTenant = user.userTenants.singleOrNull { it.tenantId == tenantId } ?: return emptyList()

        // Map the roles to TenantRoleContext objects
        return userTenant.roles.map {
            it.tenantRole.toTenantRoleContext()
        }
    }

    /**
     * Find tenant roles for multiple users in a single query
     */
    fun findTenantRolesForUsers(userIds: List<Long>, tenantId: Long): Map<Long, List<TenantRoleContext>> {
        if (userIds.isEmpty()) {
            return emptyMap()
        }

        // Get all users with their tenant roles in a single query
        val users = userRepository.findUsersWithTenantRoles(userIds, tenantId)

        // Create a map of user ID to tenant roles
        return users.associate { user ->
            // Find the UserTenant with the specified tenantId
            val userTenant = user.userTenants.singleOrNull { it.tenantId == tenantId }

            // Map the roles to TenantRoleContext objects
            val tenantRoles = userTenant?.roles?.map {
                it.tenantRole.toTenantRoleContext()
            } ?: emptyList()

            user.id to tenantRoles
        }
    }

    fun createTenantForUser(payload: CreateCompanyPayload, user: UserContext): Tenant {
        // TODO: check for exist user tenant company
        val tenant = tenantService.createNewTenant(payload)
        val tenantRole = tenantService.findTenantRoleByCode(TenantRoleCode.OWNER)
        addUserTenantRole(tenant, tenantRole, user)
        return tenant
    }

    fun addUserTenantRole(
        tenant: Tenant,
        role: TenantRole,
        user: UserContext
    ) {
        val userTenant = getOrCreateUserTenant(user.id, tenant.id)
        val userTenantRoleId = UserTenantRoleId(userTenant.id, role.id)
        val userTenantRole = UserTenantRole(userTenantRoleId, userTenant, role)
        userTenantRoleRepository.save(userTenantRole)
    }

    fun getOrCreateUserTenant(userId: Long, tenantId: Long): UserTenant {
        return userTenantRepository.findUserTenantByUserIdAndTenantId(userId, tenantId)
            ?: run {
                val userTenant = UserTenant()
                userTenant.tenantId = tenantId
                userTenant.userId = userId
                userTenantRepository.save(userTenant)
            }
    }

    // ---------------------------------
    // Private Helper
    // ---------------------------------

    private fun signup(user: User): LoginPayload {
        val savedUser: User = userRepository.saveAndFlush(user)
        return login(savedUser)
    }

    private fun login(user: User): LoginPayload {
        val springUser = SpringUser(user.toUserContext(user.lastTenantId))
        AccountStatusUserDetailsChecker().check(springUser)

        user.lastLoginAt = Instant.now()
        userRepository.save(user)

        return LoginPayload(id = user.id, tenantId = user.lastTenantId)
    }

    private fun User.toUserContext(tenantId: Long?): UserContext {
        return UserContext(
            id = this.id,
            email = this.email,
            password = this.passwordHash,
            isEnabled = this.isEnabled,
            isLocked = this.isLocked,
            isEnableCreateCompany = this.isEnableCreateCompany,
            currentTenant = this.toCurrentTenant(tenantId),
            tenants = this.getTenantsInfo(),
            roles = this.roles.map { it -> it.toRoleContext() }.toList()
        )
    }

    private fun User.toCurrentTenant(tenantId: Long?): UserTenantContext? {
        return tenantId
            ?.let { id -> userTenants.singleOrNull { it.tenantId == id }?.tenant }
            ?.let { tenant ->
                UserTenantContext(
                    id = tenantId,
                    companyName = tenant.getCompanyName(),
                    countryCode = currencyService.getCompanyCurrency(tenant.getCompanyCountryCode()),
                    roles = findTenantRoles(this.id, tenantId),
                    tenantSlug = tenant.slug
                )
            }
    }

    private fun User.getTenantsInfo(): List<TenantInfo> {
        return this.userTenants.map {
            val tenant = it.tenant
            TenantInfo(
                tenant.id,
                tenant.getCompanyName(),
                currencyService.getCompanyCurrency(tenant.getCompanyCountryCode())
            )
        }.sortedBy { it.id }
    }

    private fun TenantRole.toTenantRoleContext(): TenantRoleContext {
        return TenantRoleContext(
            name = this.name,
            code = this.code,
            description = this.description,
            permissions = this.tenantPermissions.map { it.toTenantPermissionContext() }.toList()
        )
    }

    private fun TenantPermission.toTenantPermissionContext(): TenantPermissionContext {
        return TenantPermissionContext(
            name = this.name,
            code = this.code,
            description = this.description
        )
    }

    private fun Role.toRoleContext(): RoleContext {
        return RoleContext(
            name = this.name,
            code = this.code,
            description = this.description,
            permissions = this.permissions.map { it.toPermissionContext() }.toList()
        )
    }

    private fun Permission.toPermissionContext(): PermissionContext {
        return PermissionContext(
            name = this.name,
            code = this.code,
            description = this.description
        )
    }
}
