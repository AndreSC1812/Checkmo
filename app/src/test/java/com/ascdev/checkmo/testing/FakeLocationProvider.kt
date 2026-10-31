package com.ascdev.checkmo.testing

import com.ascdev.checkmo.domain.location.LocationProvider
import com.ascdev.checkmo.domain.model.Coordinates
import com.ascdev.checkmo.domain.model.DeviceLocation
import com.ascdev.checkmo.domain.model.LocationError
import com.ascdev.checkmo.domain.model.LocationException

class FakeLocationProvider : LocationProvider {

    var location: DeviceLocation = DeviceLocation(Coordinates(40.41678, -3.70379), accuracyMeters = 10f, isMock = false)
    var error: LocationError? = null

    override suspend fun currentLocation(): DeviceLocation {
        error?.let { throw LocationException(it) }
        return location
    }
}
