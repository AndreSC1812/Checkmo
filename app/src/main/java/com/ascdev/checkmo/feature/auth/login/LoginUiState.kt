package com.ascdev.checkmo.feature.auth.login

import com.ascdev.checkmo.domain.model.AuthError

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: AuthError? = null,
    val passwordResetSent: Boolean = false,
)
