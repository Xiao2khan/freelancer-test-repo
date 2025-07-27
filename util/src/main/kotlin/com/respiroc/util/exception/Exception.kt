package com.respiroc.util.exception

open class BaseException(override val message: String) : RuntimeException(message)

class ResourceNotFoundException(message: String) : BaseException(message)
class ResourceAlreadyExistsException(message: String): BaseException(message)
class MissingTenantContextException(message: String = "Current tenant is not set in context") : BaseException(message)
class AccountNotFoundException(message: String) : BaseException(message)
class InvalidVatCodeException(message: String) : BaseException(message)
class PostingsNotBalancedException(message: String) : BaseException(message)
class InvalidPostingsException(message: String) : BaseException(message)
class AuthenticationException(message: String) : BaseException(message)