package com.ascdev.checkmo.domain.workday

import com.ascdev.checkmo.domain.model.SyncStatus
import com.ascdev.checkmo.domain.model.TimeEntryError
import com.ascdev.checkmo.domain.model.TimeEntryException
import com.ascdev.checkmo.domain.model.TimeEntryType
import com.ascdev.checkmo.testing.FakeAuthRepository
import com.ascdev.checkmo.testing.FakeTimeEntryRepository
import com.ascdev.checkmo.testing.TestZone
import com.ascdev.checkmo.testing.at
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock

class RegisterTimeEntryUseCaseTest {

    private val authRepository = FakeAuthRepository()
    private val timeEntryRepository = FakeTimeEntryRepository()
    private val clock = Clock.fixed(at("09:00"), TestZone)
    private val registerTimeEntry = RegisterTimeEntryUseCase(authRepository, timeEntryRepository, clock)

    @Before
    fun setUp() {
        authRepository.setSession(authRepository.testUser())
    }

    @Test
    fun `clock in is stored locally as pending`() = runTest {
        val entry = registerTimeEntry(TimeEntryType.CLOCK_IN).getOrThrow()

        assertEquals(TimeEntryType.CLOCK_IN, entry.type)
        assertEquals(SyncStatus.PENDING, entry.syncStatus)
        assertEquals(at("09:00"), entry.timestamp)
        assertEquals("c1", entry.companyId)
        assertEquals(listOf(entry), timeEntryRepository.entries.value)
    }

    @Test
    fun `invalid transition is rejected and nothing is stored`() = runTest {
        val result = registerTimeEntry(TimeEntryType.CLOCK_OUT)

        assertEquals(TimeEntryError.INVALID_TRANSITION, (result.exceptionOrNull() as TimeEntryException).error)
        assertTrue(timeEntryRepository.entries.value.isEmpty())
    }

    @Test
    fun `follows the sequence of a workday`() = runTest {
        listOf(
            TimeEntryType.CLOCK_IN,
            TimeEntryType.BREAK_START,
            TimeEntryType.BREAK_END,
            TimeEntryType.CLOCK_OUT,
        ).forEachIndexed { index, type ->
            // Entries need increasing timestamps for "latest" to be well defined.
            val stepClock = Clock.fixed(at("09:00").plusSeconds(index * 60L), TestZone)
            RegisterTimeEntryUseCase(authRepository, timeEntryRepository, stepClock)(type).getOrThrow()
        }

        assertEquals(4, timeEntryRepository.entries.value.size)
    }

    @Test
    fun `requires a signed in user`() = runTest {
        authRepository.setSession(null)

        val result = registerTimeEntry(TimeEntryType.CLOCK_IN)

        assertEquals(TimeEntryError.NOT_SIGNED_IN, (result.exceptionOrNull() as TimeEntryException).error)
    }
}
