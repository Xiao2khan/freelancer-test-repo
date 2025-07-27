package com.respiroc.webapp.config.annotation

import org.springframework.security.access.prepost.PreAuthorize

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasPermission(null, 'all:write')")
annotation class REQUIRE_PERMISSION_ALL_WRITE
