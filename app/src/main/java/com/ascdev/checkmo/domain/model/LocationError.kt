package com.ascdev.checkmo.domain.model

enum class LocationError {
    PERMISSION_DENIED,

    /** Location is turned off or no fix could be obtained in time. */
    UNAVAILABLE,
}

class LocationException(val error: LocationError) : Exception(error.name)
