package com.ascdev.checkmo.feature.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascdev.checkmo.domain.model.AuthError
import com.ascdev.checkmo.domain.model.SignUpRequest
import com.ascdev.checkmo.domain.model.UserRole
import com.ascdev.checkmo.domain.model.toAuthError
import com.ascdev.checkmo.domain.repository.AuthRepository
import com.ascdev.checkmo.domain.validation.CredentialsValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onRoleChange(role: UserRole) = _uiState.update { it.copy(role = role, error = null) }

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name, error = null) }

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email, error = null) }

    fun onPasswordChange(password: String) = _uiState.update { it.copy(password = password, error = null) }

    fun onInviteCodeChange(code: String) = _uiState.update { it.copy(inviteCode = code.uppercase(), error = null) }

    fun onCompanyNameChange(name: String) = _uiState.update { it.copy(companyName = name, error = null) }

    fun signUp() {
        val state = _uiState.value
        if (state.isLoading) return
        val validationError = validate(state)
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            authRepository.signUp(state.toRequest())
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.toAuthError()) } }
        }
    }

    private fun validate(state: SignUpUiState): AuthError? {
        val roleField = when (state.role) {
            UserRole.EMPLOYEE -> state.inviteCode
            UserRole.COMPANY -> state.companyName
        }
        return when {
            listOf(state.name, state.email, state.password, roleField).any { it.isBlank() } -> AuthError.EMPTY_FIELDS
            !CredentialsValidator.isValidEmail(state.email) -> AuthError.INVALID_EMAIL
            !CredentialsValidator.isValidPassword(state.password) -> AuthError.WEAK_PASSWORD
            else -> null
        }
    }

    private fun SignUpUiState.toRequest(): SignUpRequest = when (role) {
        UserRole.EMPLOYEE -> SignUpRequest.Employee(name, email, password, inviteCode)
        UserRole.COMPANY -> SignUpRequest.Company(name, email, password, companyName)
    }
}
