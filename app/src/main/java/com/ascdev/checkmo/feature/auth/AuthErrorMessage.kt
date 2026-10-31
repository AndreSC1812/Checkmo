package com.ascdev.checkmo.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ascdev.checkmo.R
import com.ascdev.checkmo.domain.model.AuthError
import com.ascdev.checkmo.domain.validation.CredentialsValidator

@Composable
fun AuthError.message(): String = when (this) {
    AuthError.EMPTY_FIELDS -> stringResource(R.string.auth_error_empty_fields)
    AuthError.INVALID_EMAIL -> stringResource(R.string.auth_error_invalid_email)
    AuthError.WEAK_PASSWORD -> stringResource(
        R.string.auth_error_weak_password,
        CredentialsValidator.MIN_PASSWORD_LENGTH,
    )
    AuthError.INVALID_CREDENTIALS -> stringResource(R.string.auth_error_invalid_credentials)
    AuthError.EMAIL_ALREADY_IN_USE -> stringResource(R.string.auth_error_email_in_use)
    AuthError.INVALID_INVITE_CODE -> stringResource(R.string.auth_error_invalid_invite_code)
    AuthError.PROFILE_NOT_FOUND -> stringResource(R.string.auth_error_profile_not_found)
    AuthError.NETWORK -> stringResource(R.string.auth_error_network)
    AuthError.UNKNOWN -> stringResource(R.string.auth_error_unknown)
}
