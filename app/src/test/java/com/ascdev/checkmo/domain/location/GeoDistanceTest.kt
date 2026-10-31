package com.ascdev.checkmo.domain.location

import com.ascdev.checkmo.domain.model.Coordinates
import org.junit.Assert.assertEquals
import org.junit.Test

class GeoDistanceTest {

    private val puertaDelSol = Coordinates(40.41678, -3.70379)
    private val atocha = Coordinates(40.40692, -3.69117)

    @Test
    fun `same point is zero meters away`() {
        assertEquals(0.0, GeoDistance.meters(puertaDelSol, puertaDelSol), 0.001)
    }

    @Test
    fun `puerta del sol to atocha is about one and a half kilometers`() {
        assertEquals(1_530.0, GeoDistance.meters(puertaDelSol, atocha), 20.0)
    }

    @Test
    fun `distance is symmetric`() {
        assertEquals(GeoDistance.meters(puertaDelSol, atocha), GeoDistance.meters(atocha, puertaDelSol), 0.001)
    }

    @Test
    fun `one thousandth of a degree of latitude is about 111 meters`() {
        assertEquals(111.2, GeoDistance.meters(Coordinates(40.0, -3.0), Coordinates(40.001, -3.0)), 0.5)
    }
}
