package com.ascdev.checkmo.feature.clockin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascdev.checkmo.core.time.Ticker
import com.ascdev.checkmo.domain.model.TimeEntryError
import com.ascdev.checkmo.domain.model.TimeEntryException
import com.ascdev.checkmo.domain.model.TimeEntryType
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

@HiltViewModel
class ClockInViewModel @Inject constructor(
    observeWorkday: ObserveWorkdayUseCase,
    private val registerTimeEntry: RegisterTimeEntryUseCase,
    ticker: Ticker,
) : ViewModel() {

    private val actionState = MutableStateFlow(ActionState())

    // Ticks only run while the screen is visible thanks to WhileSubscribed.
    val uiState: StateFlow<ClockInUiState> = combine(
        observeWorkday(ticker.ticks()).filterNotNull(),
        actionState,
    ) { workday, action ->
        ClockInUiState.Ready(workday = workday, isRegistering = action.isRegistering, error = action.error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ClockInUiState.Loading)

    fun register(type: TimeEntryType) {
        if (actionState.value.isRegistering) return
        actionState.update { it.copy(isRegistering = true, error = null) }
        viewModelScope.launch {
            val error = registerTimeEntry(type).exceptionOrNull()?.let { e ->
                (e as? TimeEntryException)?.error ?: TimeEntryError.UNKNOWN
            }
            actionState.update { ActionState(isRegistering = false, error = error) }
        }
    }

    fun onErrorShown() {
        actionState.update { it.copy(error = null) }
    }

    private data class ActionState(
        val isRegistering: Boolean = false,
        val error: TimeEntryError? = null,
    )
}
