package com.ascdev.checkmo.feature.clockin

import com.ascdev.checkmo.core.biometric.VerificationResult
import com.ascdev.checkmo.core.time.Ticker
import com.ascdev.checkmo.domain.location.CheckWorkplaceUseCase
import com.ascdev.checkmo.domain.model.Coordinates
import com.ascdev.checkmo.domain.model.LocationError
import com.ascdev.checkmo.domain.model.TimeEntryError
import com.ascdev.checkmo.domain.model.TimeEntryType
import com.ascdev.checkmo.domain.model.WorkStatus
import com.ascdev.checkmo.domain.model.Workplace
import com.ascdev.checkmo.domain.workday.ObserveWorkdayUseCase
import com.ascdev.checkmo.domain.workday.RegisterTimeEntryUseCase
import com.ascdev.checkmo.testing.FakeAuthRepository
import com.ascdev.checkmo.testing.FakeLocationProvider
import com.ascdev.checkmo.testing.FakeTimeEntryRepository
import com.ascdev.checkmo.testing.FakeWorkplaceRepository
import com.ascdev.checkmo.testing.MainDispatcherRule
import com.ascdev.checkmo.testing.TestZone
import com.ascdev.checkmo.testing.at
import com.ascdev.checkmo.testing.entry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
class ClockInViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository()
    private val timeEntryRepository = FakeTimeEntryRepository()
    private val workplaceRepository = FakeWorkplaceRepository()
    private val locationProvider = FakeLocationProvider()
    private val now = MutableStateFlow(at("09:00"))
    private lateinit var viewModel: ClockInViewModel

    @Before
    fun setUp() {
        authRepository.setSession(authRepository.testUser())
        val clock = Clock.fixed(at("09:00"), TestZone)
        viewModel = ClockInViewModel(
            observeWorkday = ObserveWorkdayUseCase(authRepository, timeEntryRepository, clock),
            registerTimeEntry = RegisterTimeEntryUseCase(authRepository, timeEntryRepository, clock),
            checkWorkplace = CheckWorkplaceUseCase(authRepository, workplaceRepository, locationProvider),
            ticker = Ticker { now },
        )
    }

    private fun TestScope.collectUiState() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    private val readyState get() = viewModel.uiState.value as ClockInUiState.Ready

    @Test
    fun `starts off duty offering clock in`() = runTest {
        collectUiState()

        assertEquals(WorkStatus.OFF_DUTY, readyState.workday.status)
        assertEquals(listOf(TimeEntryType.CLOCK_IN), readyState.workday.status.allowedEntries)
    }

    @Test
    fun `action without workplace goes straight to verification`() = runTest {
        collectUiState()

        viewModel.onAction(TimeEntryType.CLOCK_IN)

        assertEquals(ClockInPendingAction.VerifyUser(TimeEntryType.CLOCK_IN), readyState.pendingAction)
        assertFalse(readyState.actionsEnabled)
    }

    @Test
    fun `verified clock in switches to working`() = runTest {
        collectUiState()

        viewModel.onAction(TimeEntryType.CLOCK_IN)
        viewModel.onVerificationResult(TimeEntryType.CLOCK_IN, VerificationResult.Success)

        assertEquals(WorkStatus.WORKING, readyState.workday.status)
        assertEquals(1, readyState.workday.entries.size)
        assertNull(readyState.pendingAction)
    }

    @Test
    fun `inside the workplace stores the location with the entry`() = runTest {
        workplaceRepository.workplace.value = Workplace(locationProvider.location.coordinates, radiusMeters = 100)
        collectUiState()

        viewModel.onAction(TimeEntryType.CLOCK_IN)
        viewModel.onVerificationResult(TimeEntryType.CLOCK_IN, VerificationResult.Success)

        assertEquals(locationProvider.location.coordinates, timeEntryRepository.entries.value.single().location?.coordinates)
    }

    @Test
    fun `outside the workplace blocks the entry before asking for biometrics`() = runTest {
        workplaceRepository.workplace.value = Workplace(Coordinates(41.0, 2.0), radiusMeters = 100)
        collectUiState()

        viewModel.onAction(TimeEntryType.CLOCK_IN)

        assertTrue(readyState.message is ClockInMessage.OutsideWorkplace)
        assertNull(readyState.pendingAction)
        assertTrue(timeEntryRepository.entries.value.isEmpty())
    }

    @Test
    fun `missing permission asks for it and retries when granted`() = runTest {
        workplaceRepository.workplace.value = Workplace(locationProvider.location.coordinates, radiusMeters = 100)
        locationProvider.error = LocationError.PERMISSION_DENIED
        collectUiState()

        viewModel.onAction(TimeEntryType.CLOCK_IN)
        assertEquals(ClockInPendingAction.RequestLocationPermission(TimeEntryType.CLOCK_IN), readyState.pendingAction)

        locationProvider.error = null
        viewModel.onLocationPermissionResult(TimeEntryType.CLOCK_IN, granted = true)
        assertEquals(ClockInPendingAction.VerifyUser(TimeEntryType.CLOCK_IN), readyState.pendingAction)
    }

    @Test
    fun `denied permission shows a message`() = runTest {
        workplaceRepository.workplace.value = Workplace(locationProvider.location.coordinates, radiusMeters = 100)
        locationProvider.error = LocationError.PERMISSION_DENIED
        collectUiState()

        viewModel.onAction(TimeEntryType.CLOCK_IN)
        viewModel.onLocationPermissionResult(TimeEntryType.CLOCK_IN, granted = false)

        assertEquals(ClockInMessage.LocationPermissionDenied, readyState.message)
        assertNull(readyState.pendingAction)
    }

    @Test
    fun `cancelled verification records nothing and shows no message`() = runTest {
        collectUiState()

        viewModel.onVerificationResult(TimeEntryType.CLOCK_IN, VerificationResult.Cancelled)

        assertTrue(timeEntryRepository.entries.value.isEmpty())
        assertNull(readyState.message)
    }

    @Test
    fun `unavailable verification records nothing and asks to set up a lock`() = runTest {
        collectUiState()

        viewModel.onVerificationResult(TimeEntryType.CLOCK_IN, VerificationResult.Unavailable)

        assertTrue(timeEntryRepository.entries.value.isEmpty())
        assertEquals(ClockInMessage.VerificationUnavailable, readyState.message)
    }

    @Test
    fun `verification error records nothing and shows the reason`() = runTest {
        collectUiState()

        viewModel.onVerificationResult(TimeEntryType.CLOCK_IN, VerificationResult.Error("Demasiados intentos"))

        assertTrue(timeEntryRepository.entries.value.isEmpty())
        assertEquals(ClockInMessage.VerificationFailed("Demasiados intentos"), readyState.message)
    }

    @Test
    fun `worked time advances with each tick`() = runTest {
        timeEntryRepository.entries.value = listOf(entry(TimeEntryType.CLOCK_IN, "08:00"))
        collectUiState()

        now.value = at("10:30")

        assertEquals(Duration.ofMinutes(150), readyState.workday.worked)
    }

    @Test
    fun `invalid entry shows error until it is consumed`() = runTest {
        collectUiState()

        viewModel.onAction(TimeEntryType.CLOCK_OUT)
        viewModel.onVerificationResult(TimeEntryType.CLOCK_OUT, VerificationResult.Success)
        assertEquals(ClockInMessage.EntryFailed(TimeEntryError.INVALID_TRANSITION), readyState.message)

        viewModel.onMessageShown()
        assertNull(readyState.message)
    }
}
