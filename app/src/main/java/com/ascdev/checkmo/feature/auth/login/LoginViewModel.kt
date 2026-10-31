package com.ascdev.checkmo.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascdev.checkmo.domain.model.AuthError
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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, error = null, passwordResetSent = false) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    // Navigation happens reactively once the session is saved, so success needs no extra state.
    fun signIn() {
        val state = _uiState.value
        if (state.isLoading) return
        val validationError = when {
            state.email.isBlank() || state.password.isEmpty() -> AuthError.EMPTY_FIELDS
            !CredentialsValidator.isValidEmail(state.email) -> AuthError.INVALID_EMAIL
            else -> null
        }
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null, passwordResetSent = false) }
        viewModelScope.launch {
            authRepository.signIn(state.email, state.password)
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.toAuthError()) } }
        }
    }

    fun sendPasswordReset() {
        val email = _uiState.value.email
        if (!CredentialsValidator.isValidEmail(email)) {
            _uiState.update { it.copy(error = AuthError.INVALID_EMAIL) }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            authRepository.sendPasswordReset(email)
                .onSuccess { _uiState.update { it.copy(isLoading = false, passwordResetSent = true) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.toAuthError()) } }
        }
    }
}
