package com.ascdev.checkmo.feature.workplace

import com.ascdev.checkmo.domain.model.Coordinates
import com.ascdev.checkmo.domain.model.Workplace

data class WorkplaceUiState(
    val isLoading: Boolean = true,
    val center: Coordinates? = null,
    val accuracyMeters: Float? = null,
    val radiusMeters: Int = Workplace.DEFAULT_RADIUS_METERS,
    val hasChanges: Boolean = false,
    val isLocating: Boolean = false,
    val isSaving: Boolean = false,
    val message: WorkplaceMessage? = null,
) {
    val canSave: Boolean get() = center != null && hasChanges && !isSaving && !isLocating
}

enum class WorkplaceMessage {
    SAVED,
    SAVE_FAILED,
    LOCATION_PERMISSION_DENIED,
    LOCATION_UNAVAILABLE,
}
