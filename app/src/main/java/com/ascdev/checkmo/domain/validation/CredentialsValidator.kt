package com.ascdev.checkmo.domain.validation

object CredentialsValidator {

    // Firebase Auth rejects passwords shorter than 6 characters.
    const val MIN_PASSWORD_LENGTH = 6

    private val EmailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean = EmailRegex.matches(email.trim())

    fun isValidPassword(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH
}
