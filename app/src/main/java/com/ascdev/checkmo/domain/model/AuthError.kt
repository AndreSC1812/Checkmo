package com.ascdev.checkmo.domain.model

enum class AuthError {
    EMPTY_FIELDS,
    INVALID_EMAIL,
    WEAK_PASSWORD,
    INVALID_CREDENTIALS,
    EMAIL_ALREADY_IN_USE,
    INVALID_INVITE_CODE,
    PROFILE_NOT_FOUND,
    NETWORK,
    UNKNOWN,
}

class AuthException(val error: AuthError, cause: Throwable? = null) : Exception(error.name, cause)

fun Throwable.toAuthError(): AuthError = (this as? AuthException)?.error ?: AuthError.UNKNOWN
