package com.ascdev.checkmo.domain.workday

import com.ascdev.checkmo.domain.model.TimeEntryType
import com.ascdev.checkmo.domain.model.WorkStatus
import com.ascdev.checkmo.testing.FakeAuthRepository
import com.ascdev.checkmo.testing.FakeTimeEntryRepository
import com.ascdev.checkmo.testing.TestDate
import com.ascdev.checkmo.testing.TestZone
import com.ascdev.checkmo.testing.at
import com.ascdev.checkmo.testing.entry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.Duration

class ObserveWorkdayUseCaseTest {

    private val authRepository = FakeAuthRepository()
    private val timeEntryRepository = FakeTimeEntryRepository()
    private val observeWorkday = ObserveWorkdayUseCase(
        authRepository,
        timeEntryRepository,
        Clock.fixed(at("12:00"), TestZone),
    )

    @Test
    fun `emits null when signed out`() = runTest {
        assertNull(observeWorkday(flowOf(at("12:00"))).first())
    }

    @Test
    fun `computes today from entries and the status carried over from yesterday`() = runTest {
        authRepository.setSession(authRepository.testUser())
        timeEntryRepository.entries.value = listOf(
            entry(TimeEntryType.CLOCK_IN, "22:00", date = TestDate.minusDays(1)),
            entry(TimeEntryType.CLOCK_OUT, "02:00"),
            entry(TimeEntryType.CLOCK_IN, "09:00"),
        )

        val workday = observeWorkday(flowOf(at("12:00"))).first()!!

        assertEquals(TestDate, workday.date)
        assertEquals(WorkStatus.WORKING, workday.status)
        assertEquals(Duration.ofHours(5), workday.worked)
        assertEquals(2, workday.entries.size)
    }

    @Test
    fun `entry newer than the last tick is reflected immediately`() = runTest {
        authRepository.setSession(authRepository.testUser())
        timeEntryRepository.entries.value = listOf(entry(TimeEntryType.CLOCK_IN, "12:00:01"))

        val workday = observeWorkday(flowOf(at("12:00"))).first()!!

        assertEquals(WorkStatus.WORKING, workday.status)
    }
}
