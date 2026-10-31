package com.ascdev.checkmo.domain.location

import com.ascdev.checkmo.domain.model.Coordinates
import com.ascdev.checkmo.domain.model.DeviceLocation
import com.ascdev.checkmo.domain.model.EntryLocation
import com.ascdev.checkmo.domain.model.LocationError
import com.ascdev.checkmo.domain.model.Workplace
import com.ascdev.checkmo.domain.model.WorkplaceCheck
import com.ascdev.checkmo.testing.FakeAuthRepository
import com.ascdev.checkmo.testing.FakeLocationProvider
import com.ascdev.checkmo.testing.FakeWorkplaceRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CheckWorkplaceUseCaseTest {

    private val authRepository = FakeAuthRepository()
    private val workplaceRepository = FakeWorkplaceRepository()
    private val locationProvider = FakeLocationProvider()
    private val checkWorkplace = CheckWorkplaceUseCase(authRepository, workplaceRepository, locationProvider)

    private val center = Coordinates(40.0, -3.0)

    @Before
    fun setUp() {
        authRepository.setSession(authRepository.testUser())
        workplaceRepository.workplace.value = Workplace(center, radiusMeters = 100)
    }

    /** About [meters] north of the center. */
    private fun at(meters: Double, accuracy: Float = 5f, isMock: Boolean = false) = DeviceLocation(
        coordinates = Coordinates(center.latitude + meters / 111_195.0, center.longitude),
        accuracyMeters = accuracy,
        isMock = isMock,
    )

    @Test
    fun `allowed anywhere when no workplace is configured`() = runTest {
        workplaceRepository.workplace.value = null
        locationProvider.error = LocationError.PERMISSION_DENIED

        assertEquals(WorkplaceCheck.NotConfigured, checkWorkplace())
    }

    @Test
    fun `inside the radius returns the location as evidence`() = runTest {
        locationProvider.location = at(meters = 60.0)

        val result = checkWorkplace()

        assertEquals(WorkplaceCheck.Inside(EntryLocation(at(60.0).coordinates, 5f)), result)
    }

    @Test
    fun `outside the radius reports distance from the center`() = runTest {
        locationProvider.location = at(meters = 400.0)

        val result = checkWorkplace() as WorkplaceCheck.Outside

        assertEquals(400.0, result.distanceMeters.toDouble(), 2.0)
    }

    @Test
    fun `accuracy gives tolerance at the edge of the radius`() = runTest {
        locationProvider.location = at(meters = 120.0, accuracy = 30f)

        assertTrue(checkWorkplace() is WorkplaceCheck.Inside)
    }

    @Test
    fun `accuracy tolerance is capped`() = runTest {
        locationProvider.location = at(meters = 300.0, accuracy = 500f)

        assertTrue(checkWorkplace() is WorkplaceCheck.Outside)
    }

    @Test
    fun `mock locations are rejected even inside the radius`() = runTest {
        locationProvider.location = at(meters = 10.0, isMock = true)

        assertEquals(WorkplaceCheck.MockLocation, checkWorkplace())
    }

    @Test
    fun `location errors are passed through`() = runTest {
        locationProvider.error = LocationError.PERMISSION_DENIED

        assertEquals(WorkplaceCheck.Failed(LocationError.PERMISSION_DENIED), checkWorkplace())
    }
}
