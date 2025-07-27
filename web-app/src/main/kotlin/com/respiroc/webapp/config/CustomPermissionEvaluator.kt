package com.respiroc.webapp.config

import com.respiroc.util.constant.PermissionAction
import com.respiroc.util.context.SpringUser
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.io.Serializable

@Component
class CustomPermissionEvaluator : PermissionEvaluator {
    override fun hasPermission(authentication: Authentication, targetDomainObject: Any?, permission: Any?): Boolean {
        val springUser = authentication.principal as SpringUser
        val currentUserPermissions = springUser.authorities.map { it.authority }.toSet()

        val requiredPermissions = permission.toString()
            .split(",", "|")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        return requiredPermissions.any { requiredPermission ->
            currentUserPermissions.any { userPermission ->
                isPermissionGranted(userPermission, requiredPermission)
            }
        }
    }

    override fun hasPermission(authentication: Authentication, targetId: Serializable?, targetType: String?, permission: Any?): Boolean {
        return hasPermission(authentication, null, permission)
    }


    fun isPermissionGranted(userPermission: String, requiredPermission: String): Boolean {
        val (userEntity, userActionStr) = userPermission.split(":")
        val (reqEntity, reqActionStr) = requiredPermission.split(":")

        val userAction = PermissionAction.fromCode(userActionStr) ?: return false
        val reqAction = PermissionAction.fromCode(reqActionStr) ?: return false

        val entityMatch = userEntity == "all" || userEntity == reqEntity
        val actionMatch = userAction.includes(reqAction)

        return entityMatch && actionMatch
    }
}
