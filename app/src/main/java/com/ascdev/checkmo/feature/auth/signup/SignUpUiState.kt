package com.ascdev.checkmo.feature.auth.signup

import com.ascdev.checkmo.domain.model.AuthError
import com.ascdev.checkmo.domain.model.UserRole

data class SignUpUiState(
    val role: UserRole = UserRole.EMPLOYEE,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val inviteCode: String = "",
    val companyName: String = "",
    val isLoading: Boolean = false,
    val error: AuthError? = null,
)
