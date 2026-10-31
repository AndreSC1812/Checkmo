package com.ascdev.checkmo.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascdev.checkmo.domain.model.User
import com.ascdev.checkmo.domain.model.UserRole
import com.ascdev.checkmo.domain.model.Workplace
import com.ascdev.checkmo.domain.repository.AuthRepository
import com.ascdev.checkmo.domain.repository.WorkplaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User,
    /** Only loaded for company accounts. */
    val workplace: Workplace? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    workplaceRepository: WorkplaceRepository,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ProfileUiState?> = authRepository.session
        .flatMapLatest { user ->
            when {
                user == null -> flowOf(null)
                user.role == UserRole.COMPANY -> workplaceRepository.observeWorkplace(user.company.id)
                    .map { ProfileUiState(user, it) }
                else -> flowOf(ProfileUiState(user))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}
