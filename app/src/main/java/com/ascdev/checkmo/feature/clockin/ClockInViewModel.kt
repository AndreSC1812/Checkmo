package com.ascdev.checkmo.feature.clockin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascdev.checkmo.core.biometric.VerificationResult
import com.ascdev.checkmo.core.time.Ticker
import com.ascdev.checkmo.domain.location.CheckWorkplaceUseCase
import com.ascdev.checkmo.domain.model.EntryLocation
import com.ascdev.checkmo.domain.model.LocationError
import com.ascdev.checkmo.domain.model.TimeEntryError
import com.ascdev.checkmo.domain.model.TimeEntryException
import com.ascdev.checkmo.domain.model.TimeEntryType
import com.ascdev.checkmo.domain.model.WorkplaceCheck
import com.ascdev.checkmo.domain.workday.ObserveWorkdayUseCase
import com.ascdev.checkmo.domain.workday.RegisterTimeEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Clocking flow: check the workplace location (asking for permission if needed), then verify
 * the user with biometrics, then record the entry together with where it happened.
 */
@HiltViewModel
class ClockInViewModel @Inject constructor(
    observeWorkday: ObserveWorkdayUseCase,
    private val registerTimeEntry: RegisterTimeEntryUseCase,
    private val checkWorkplace: CheckWorkplaceUseCase,
    ticker: Ticker,
) : ViewModel() {

    private val actionState = MutableStateFlow(ActionState())

    // Location that passed the workplace check, waiting for the user to be verified.
    private var verifiedLocation: EntryLocation? = null

    // Ticks only run while the screen is visible thanks to WhileSubscribed.
    val uiState: StateFlow<ClockInUiState> = combine(
        observeWorkday(ticker.ticks()).filterNotNull(),
        actionState,
    ) { workday, action ->
        ClockInUiState.Ready(
            workday = workday,
            isCheckingLocation = action.isCheckingLocation,
            isRegistering = action.isRegistering,
            pendingAction = action.pendingAction,
            message = action.message,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ClockInUiState.Loading)

    fun onAction(type: TimeEntryType) {
        val state = actionState.value
        if (state.isCheckingLocation || state.isRegistering || state.pendingAction != null) return
        actionState.update { it.copy(isCheckingLocation = true, message = null) }
        viewModelScope.launch {
            val check = checkWorkplace()
            actionState.update { it.copy(isCheckingLocation = false) }
            when (check) {
                WorkplaceCheck.NotConfigured -> requestVerification(type, location = null)
                is WorkplaceCheck.Inside -> requestVerification(type, check.location)
                is WorkplaceCheck.Outside -> showMessage(ClockInMessage.OutsideWorkplace(check.distanceMeters))
                WorkplaceCheck.MockLocation -> showMessage(ClockInMessage.MockLocation)
                is WorkplaceCheck.Failed -> when (check.error) {
                    LocationError.PERMISSION_DENIED -> setPendingAction(ClockInPendingAction.RequestLocationPermission(type))
                    LocationError.UNAVAILABLE -> showMessage(ClockInMessage.LocationUnavailable)
                }
            }
        }
    }

    fun onLocationPermissionResult(type: TimeEntryType, granted: Boolean) {
        setPendingAction(null)
        if (granted) onAction(type) else showMessage(ClockInMessage.LocationPermissionDenied)
    }

    fun onVerificationResult(type: TimeEntryType, result: VerificationResult) {
        setPendingAction(null)
        val location = verifiedLocation.also { verifiedLocation = null }
        when (result) {
            VerificationResult.Success -> register(type, location)
            VerificationResult.Cancelled -> Unit
            VerificationResult.Unavailable -> showMessage(ClockInMessage.VerificationUnavailable)
            is VerificationResult.Error -> showMessage(ClockInMessage.VerificationFailed(result.message))
        }
    }

    fun onMessageShown() {
        actionState.update { it.copy(message = null) }
    }

    private fun requestVerification(type: TimeEntryType, location: EntryLocation?) {
        verifiedLocation = location
        setPendingAction(ClockInPendingAction.VerifyUser(type))
    }

    private fun register(type: TimeEntryType, location: EntryLocation?) {
        actionState.update { it.copy(isRegistering = true, message = null) }
        viewModelScope.launch {
            val message = registerTimeEntry(type, location).exceptionOrNull()?.let { e ->
                ClockInMessage.EntryFailed((e as? TimeEntryException)?.error ?: TimeEntryError.UNKNOWN)
            }
            actionState.update { it.copy(isRegistering = false, message = message) }
        }
    }

    private fun setPendingAction(action: ClockInPendingAction?) {
        actionState.update { it.copy(pendingAction = action) }
    }

    private fun showMessage(message: ClockInMessage) {
        actionState.update { it.copy(message = message) }
    }

    private data class ActionState(
        val isCheckingLocation: Boolean = false,
        val isRegistering: Boolean = false,
        val pendingAction: ClockInPendingAction? = null,
        val message: ClockInMessage? = null,
    )
}
