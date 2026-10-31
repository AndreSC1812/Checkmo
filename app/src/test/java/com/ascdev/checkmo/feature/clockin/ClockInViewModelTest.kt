package com.ascdev.checkmo.feature.clockin

import com.ascdev.checkmo.core.time.Ticker
import com.ascdev.checkmo.domain.model.TimeEntryError
import com.ascdev.checkmo.domain.model.TimeEntryType
import com.ascdev.checkmo.domain.model.WorkStatus
import com.ascdev.checkmo.domain.workday.ObserveWorkdayUseCase
import com.ascdev.checkmo.domain.workday.RegisterTimeEntryUseCase
import com.ascdev.checkmo.testing.FakeAuthRepository
import com.ascdev.checkmo.testing.FakeTimeEntryRepository
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
import org.junit.Assert.assertNull
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
    private val now = MutableStateFlow(at("09:00"))
    private lateinit var viewModel: ClockInViewModel

    @Before
    fun setUp() {
        authRepository.setSession(authRepository.testUser())
        val clock = Clock.fixed(at("09:00"), TestZone)
        viewModel = ClockInViewModel(
            observeWorkday = ObserveWorkdayUseCase(authRepository, timeEntryRepository, clock),
            registerTimeEntry = RegisterTimeEntryUseCase(authRepository, timeEntryRepository, clock),
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
    fun `clocking in switches to working`() = runTest {
        collectUiState()

        viewModel.register(TimeEntryType.CLOCK_IN)

        assertEquals(WorkStatus.WORKING, readyState.workday.status)
        assertEquals(1, readyState.workday.entries.size)
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

        viewModel.register(TimeEntryType.CLOCK_OUT)
        assertEquals(TimeEntryError.INVALID_TRANSITION, readyState.error)

        viewModel.onErrorShown()
        assertNull(readyState.error)
    }
}
