package com.ascdev.checkmo.feature.clockin

import com.ascdev.checkmo.domain.model.TimeEntryError
import com.ascdev.checkmo.domain.model.Workday

sealed interface ClockInUiState {

    data object Loading : ClockInUiState

    data class Ready(
        val workday: Workday,
        val isRegistering: Boolean = false,
        val error: TimeEntryError? = null,
    ) : ClockInUiState
}
