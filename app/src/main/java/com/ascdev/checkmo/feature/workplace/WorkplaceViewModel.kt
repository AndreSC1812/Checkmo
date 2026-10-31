package com.ascdev.checkmo.feature.workplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascdev.checkmo.domain.location.LocationProvider
import com.ascdev.checkmo.domain.model.LocationError
import com.ascdev.checkmo.domain.model.LocationException
import com.ascdev.checkmo.domain.model.Workplace
import com.ascdev.checkmo.domain.repository.AuthRepository
import com.ascdev.checkmo.domain.repository.WorkplaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class WorkplaceViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val workplaceRepository: WorkplaceRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkplaceUiState())
    val uiState: StateFlow<WorkplaceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val companyId = authRepository.session.first()?.company?.id
            val workplace = companyId?.let { workplaceRepository.observeWorkplace(it).first() }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    center = workplace?.center,
                    radiusMeters = workplace?.radiusMeters ?: Workplace.DEFAULT_RADIUS_METERS,
                )
            }
        }
    }

    fun useCurrentLocation() {
        if (_uiState.value.isLocating) return
        _uiState.update { it.copy(isLocating = true, message = null) }
        viewModelScope.launch {
            try {
                val location = locationProvider.currentLocation()
                _uiState.update {
                    it.copy(
                        isLocating = false,
                        center = location.coordinates,
                        accuracyMeters = location.accuracyMeters,
                        hasChanges = true,
                    )
                }
            } catch (e: LocationException) {
                _uiState.update { it.copy(isLocating = false, message = e.error.toMessage()) }
            }
        }
    }

    fun onLocationPermissionDenied() {
        _uiState.update { it.copy(message = WorkplaceMessage.LOCATION_PERMISSION_DENIED) }
    }

    fun onRadiusChange(radius: Float) {
        val snapped = ((radius / RADIUS_STEP).roundToInt() * RADIUS_STEP)
            .coerceIn(Workplace.MIN_RADIUS_METERS, Workplace.MAX_RADIUS_METERS)
        if (snapped == _uiState.value.radiusMeters) return
        _uiState.update { it.copy(radiusMeters = snapped, hasChanges = true) }
    }

    fun save() {
        val state = _uiState.value
        val center = state.center ?: return
        if (!state.canSave) return
        _uiState.update { it.copy(isSaving = true, message = null) }
        viewModelScope.launch {
            val companyId = authRepository.session.first()?.company?.id
            val result = if (companyId == null) {
                Result.failure(IllegalStateException("No session"))
            } else {
                workplaceRepository.saveWorkplace(companyId, Workplace(center, state.radiusMeters))
            }
            _uiState.update {
                it.copy(
                    isSaving = false,
                    hasChanges = result.isFailure,
                    message = if (result.isSuccess) WorkplaceMessage.SAVED else WorkplaceMessage.SAVE_FAILED,
                )
            }
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    private fun LocationError.toMessage() = when (this) {
        LocationError.PERMISSION_DENIED -> WorkplaceMessage.LOCATION_PERMISSION_DENIED
        LocationError.UNAVAILABLE -> WorkplaceMessage.LOCATION_UNAVAILABLE
    }

    companion object {
        const val RADIUS_STEP = 50
    }
}
