package com.ascdev.checkmo.domain.workday

import com.ascdev.checkmo.domain.model.TimeEntryType.BREAK_END
import com.ascdev.checkmo.domain.model.TimeEntryType.BREAK_START
import com.ascdev.checkmo.domain.model.TimeEntryType.CLOCK_IN
import com.ascdev.checkmo.domain.model.TimeEntryType.CLOCK_OUT
import com.ascdev.checkmo.domain.model.TimeEntry
import com.ascdev.checkmo.domain.model.WorkStatus
import com.ascdev.checkmo.testing.TestDate
import com.ascdev.checkmo.testing.TestZone
import com.ascdev.checkmo.testing.at
import com.ascdev.checkmo.testing.entry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Duration

class WorkdayCalculatorTest {

    private fun calculate(
        entries: List<TimeEntry>,
        now: String,
        statusAtStartOfDay: WorkStatus = WorkStatus.OFF_DUTY,
    ) = WorkdayCalculator.calculate(TestDate, TestZone, entries, statusAtStartOfDay, at(now))

    @Test
    fun `no entries means off duty with nothing worked`() {
        val day = calculate(emptyList(), now = "12:00")

        assertEquals(WorkStatus.OFF_DUTY, day.status)
        assertEquals(Duration.ZERO, day.worked)
        assertNull(day.statusSince)
    }

    @Test
    fun `working counts time until now`() {
        val day = calculate(listOf(entry(CLOCK_IN, "09:00")), now = "11:30")

        assertEquals(WorkStatus.WORKING, day.status)
        assertEquals(Duration.ofMinutes(150), day.worked)
        assertEquals(at("09:00"), day.statusSince)
    }

    @Test
    fun `full day separates worked and break time`() {
        val day = calculate(
            listOf(
                entry(CLOCK_IN, "09:00"),
                entry(BREAK_START, "13:00"),
                entry(BREAK_END, "14:00"),
                entry(CLOCK_OUT, "18:00"),
            ),
            now = "20:00",
        )

        assertEquals(WorkStatus.OFF_DUTY, day.status)
        assertEquals(Duration.ofHours(8), day.worked)
        assertEquals(Duration.ofHours(1), day.onBreak)
    }

    @Test
    fun `on break counts break time until now`() {
        val day = calculate(listOf(entry(CLOCK_IN, "09:00"), entry(BREAK_START, "13:00")), now = "13:20")

        assertEquals(WorkStatus.ON_BREAK, day.status)
        assertEquals(Duration.ofHours(4), day.worked)
        assertEquals(Duration.ofMinutes(20), day.onBreak)
        assertEquals(at("13:00"), day.statusSince)
    }

    @Test
    fun `split shift adds both periods`() {
        val day = calculate(
            listOf(
                entry(CLOCK_IN, "08:00"),
                entry(CLOCK_OUT, "12:00"),
                entry(CLOCK_IN, "16:00"),
                entry(CLOCK_OUT, "19:30"),
            ),
            now = "22:00",
        )

        assertEquals(Duration.ofMinutes(450), day.worked)
    }

    @Test
    fun `shift started yesterday counts from midnight`() {
        val day = calculate(
            listOf(entry(CLOCK_OUT, "06:00")),
            now = "10:00",
            statusAtStartOfDay = WorkStatus.WORKING,
        )

        assertEquals(Duration.ofHours(6), day.worked)
        assertEquals(WorkStatus.OFF_DUTY, day.status)
    }

    @Test
    fun `past day counts until midnight`() {
        val day = WorkdayCalculator.calculate(
            date = TestDate,
            zone = TestZone,
            entries = listOf(entry(CLOCK_IN, "22:00")),
            statusAtStartOfDay = WorkStatus.OFF_DUTY,
            now = at("08:00", TestDate.plusDays(1)),
        )

        assertEquals(Duration.ofHours(2), day.worked)
    }

    @Test
    fun `entries that break the sequence are ignored`() {
        val day = calculate(
            listOf(
                entry(CLOCK_IN, "09:00"),
                entry(CLOCK_IN, "10:00"),
                entry(BREAK_END, "11:00"),
                entry(CLOCK_OUT, "12:00"),
            ),
            now = "15:00",
        )

        assertEquals(Duration.ofHours(3), day.worked)
        assertEquals(WorkStatus.OFF_DUTY, day.status)
    }

    @Test
    fun `entries from other days or the future are ignored`() {
        val day = calculate(
            listOf(
                entry(CLOCK_IN, "09:00", date = TestDate.minusDays(1)),
                entry(CLOCK_IN, "09:00"),
                entry(CLOCK_OUT, "17:00"),
            ),
            now = "12:00",
        )

        assertEquals(WorkStatus.WORKING, day.status)
        assertEquals(Duration.ofHours(3), day.worked)
        assertEquals(1, day.entries.size)
    }
}
