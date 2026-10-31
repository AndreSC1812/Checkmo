package com.ascdev.checkmo.domain.model

/** Where a time entry was recorded, kept as evidence. */
data class EntryLocation(
    val coordinates: Coordinates,
    val accuracyMeters: Float,
)

sealed interface WorkplaceCheck {

    /** The company has not set a workplace, so entries are allowed anywhere. */
    data object NotConfigured : WorkplaceCheck

    data class Inside(val location: EntryLocation) : WorkplaceCheck

    /** [distanceMeters] is measured from the workplace center. */
    data class Outside(val distanceMeters: Int) : WorkplaceCheck

    data object MockLocation : WorkplaceCheck

    data class Failed(val error: LocationError) : WorkplaceCheck
}
