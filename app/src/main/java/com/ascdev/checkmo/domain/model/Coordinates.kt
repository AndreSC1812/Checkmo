package com.ascdev.checkmo.domain.model

data class Coordinates(
    val latitude: Double,
    val longitude: Double,
)

/** A location fix reported by the device. */
data class DeviceLocation(
    val coordinates: Coordinates,
    val accuracyMeters: Float,
    /** True when the fix comes from a mock location app. */
    val isMock: Boolean,
)
