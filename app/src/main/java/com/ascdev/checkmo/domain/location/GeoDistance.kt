package com.ascdev.checkmo.domain.location

import com.ascdev.checkmo.domain.model.Coordinates
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object GeoDistance {

    private const val EARTH_RADIUS_METERS = 6_371_008.8

    /** Great-circle distance using the haversine formula; accurate to well under a meter at workplace scale. */
    fun meters(from: Coordinates, to: Coordinates): Double {
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val deltaLat = lat2 - lat1
        val deltaLon = Math.toRadians(to.longitude - from.longitude)
        val a = sin(deltaLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(deltaLon / 2).pow(2)
        return 2 * EARTH_RADIUS_METERS * asin(sqrt(a))
    }
}
