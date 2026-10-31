package com.ascdev.checkmo.domain.location

import com.ascdev.checkmo.domain.model.DeviceLocation

interface LocationProvider {

    /**
     * Fresh, high accuracy location fix.
     *
     * @throws com.ascdev.checkmo.domain.model.LocationException when permission is missing or
     * no fix is available.
     */
    suspend fun currentLocation(): DeviceLocation
}
