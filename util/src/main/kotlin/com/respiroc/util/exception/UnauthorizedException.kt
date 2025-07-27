package com.respiroc.util.exception

/**
 * Exception thrown when a user attempts to perform an action they are not authorized to perform.
 */
class UnauthorizedException(message: String) : RuntimeException(message)
