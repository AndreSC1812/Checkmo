package com.ascdev.checkmo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascdev.checkmo.domain.model.User
import com.ascdev.checkmo.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = authRepository.session
        .map { user -> if (user == null) MainUiState.SignedOut else MainUiState.SignedIn(user) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState.Loading)
}

sealed interface MainUiState {
    data object Loading : MainUiState
    data object SignedOut : MainUiState
    data class SignedIn(val user: User) : MainUiState
}
