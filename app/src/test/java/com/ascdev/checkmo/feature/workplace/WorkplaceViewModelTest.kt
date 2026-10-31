package com.ascdev.checkmo.feature.workplace

import com.ascdev.checkmo.domain.model.Coordinates
import com.ascdev.checkmo.domain.model.LocationError
import com.ascdev.checkmo.domain.model.Workplace
import com.ascdev.checkmo.testing.FakeAuthRepository
import com.ascdev.checkmo.testing.FakeLocationProvider
import com.ascdev.checkmo.testing.FakeWorkplaceRepository
import com.ascdev.checkmo.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WorkplaceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository()
    private val workplaceRepository = FakeWorkplaceRepository()
    private val locationProvider = FakeLocationProvider()

    @Before
    fun setUp() {
        authRepository.setSession(authRepository.testUser())
    }

    private fun createViewModel() = WorkplaceViewModel(authRepository, workplaceRepository, locationProvider)

    @Test
    fun `loads the saved workplace`() {
        workplaceRepository.workplace.value = Workplace(Coordinates(1.0, 2.0), radiusMeters = 300)

        val state = createViewModel().uiState.value

        assertFalse(state.isLoading)
        assertEquals(Coordinates(1.0, 2.0), state.center)
        assertEquals(300, state.radiusMeters)
        assertFalse(state.canSave)
    }

    @Test
    fun `cannot save without a location`() {
        val viewModel = createViewModel()

        viewModel.onRadiusChange(200f)

        assertNull(viewModel.uiState.value.center)
        assertFalse(viewModel.uiState.value.canSave)
    }

    @Test
    fun `current location fills the center and enables saving`() {
        val viewModel = createViewModel()

        viewModel.useCurrentLocation()

        assertEquals(Coordinates(40.41678, -3.70379), viewModel.uiState.value.center)
        assertEquals(10f, viewModel.uiState.value.accuracyMeters)
        assertTrue(viewModel.uiState.value.canSave)
    }

    @Test
    fun `location errors are reported`() {
        locationProvider.error = LocationError.UNAVAILABLE
        val viewModel = createViewModel()

        viewModel.useCurrentLocation()

        assertEquals(WorkplaceMessage.LOCATION_UNAVAILABLE, viewModel.uiState.value.message)
        assertFalse(viewModel.uiState.value.isLocating)
    }

    @Test
    fun `radius snaps to steps and stays within limits`() {
        val viewModel = createViewModel()

        viewModel.onRadiusChange(173f)
        assertEquals(150, viewModel.uiState.value.radiusMeters)

        viewModel.onRadiusChange(5_000f)
        assertEquals(Workplace.MAX_RADIUS_METERS, viewModel.uiState.value.radiusMeters)
    }

    @Test
    fun `save stores the workplace for the user's company`() {
        val viewModel = createViewModel()
        viewModel.useCurrentLocation()
        viewModel.onRadiusChange(400f)

        viewModel.save()

        assertEquals("c1", workplaceRepository.savedCompanyId)
        assertEquals(Workplace(Coordinates(40.41678, -3.70379), 400), workplaceRepository.workplace.value)
        assertEquals(WorkplaceMessage.SAVED, viewModel.uiState.value.message)
        assertFalse(viewModel.uiState.value.canSave)
    }

    @Test
    fun `failed save keeps changes so the user can retry`() {
        workplaceRepository.failSaves = true
        val viewModel = createViewModel()
        viewModel.useCurrentLocation()

        viewModel.save()

        assertEquals(WorkplaceMessage.SAVE_FAILED, viewModel.uiState.value.message)
        assertTrue(viewModel.uiState.value.canSave)
    }
}
