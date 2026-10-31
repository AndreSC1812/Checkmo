package com.ascdev.checkmo.domain.model

/** Workplace where employees are allowed to clock in, defined as a circle. */
data class Workplace(
    val center: Coordinates,
    val radiusMeters: Int,
) {
    companion object {
        const val MIN_RADIUS_METERS = 50
        const val MAX_RADIUS_METERS = 1000
        const val DEFAULT_RADIUS_METERS = 150
    }
}
