package com.ascdev.checkmo.feature.clockin

import com.ascdev.checkmo.domain.model.TimeEntryError
import com.ascdev.checkmo.domain.model.Workday

sealed interface ClockInUiState {

    data object Loading : ClockInUiState

    data class Ready(
        val workday: Workday,
        val isRegistering: Boolean = false,
        val message: ClockInMessage? = null,
    ) : ClockInUiState
}

sealed interface ClockInMessage {
    data class EntryFailed(val error: TimeEntryError) : ClockInMessage
    data object VerificationUnavailable : ClockInMessage
    data class VerificationFailed(val reason: String) : ClockInMessage
}
