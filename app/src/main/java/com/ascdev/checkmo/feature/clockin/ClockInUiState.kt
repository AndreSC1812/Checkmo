package com.ascdev.checkmo.feature.clockin

import com.ascdev.checkmo.domain.model.TimeEntryError
import com.ascdev.checkmo.domain.model.TimeEntryType
import com.ascdev.checkmo.domain.model.Workday

sealed interface ClockInUiState {

    data object Loading : ClockInUiState

    data class Ready(
        val workday: Workday,
        val isCheckingLocation: Boolean = false,
        val isRegistering: Boolean = false,
        val pendingAction: ClockInPendingAction? = null,
        val message: ClockInMessage? = null,
    ) : ClockInUiState {
        val actionsEnabled: Boolean get() = !isCheckingLocation && !isRegistering && pendingAction == null
    }
}

/** Steps that need the Activity, so the screen performs them and reports back. */
sealed interface ClockInPendingAction {
    val type: TimeEntryType

    data class RequestLocationPermission(override val type: TimeEntryType) : ClockInPendingAction
    data class VerifyUser(override val type: TimeEntryType) : ClockInPendingAction
}

sealed interface ClockInMessage {
    data class EntryFailed(val error: TimeEntryError) : ClockInMessage
    data object VerificationUnavailable : ClockInMessage
    data class VerificationFailed(val reason: String) : ClockInMessage
    data class OutsideWorkplace(val distanceMeters: Int) : ClockInMessage
    data object MockLocation : ClockInMessage
    data object LocationPermissionDenied : ClockInMessage
    data object LocationUnavailable : ClockInMessage
}
