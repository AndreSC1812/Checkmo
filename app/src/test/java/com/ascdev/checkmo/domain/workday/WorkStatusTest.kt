package com.ascdev.checkmo.domain.workday

import com.ascdev.checkmo.domain.model.TimeEntryType
import com.ascdev.checkmo.domain.model.WorkStatus
import com.ascdev.checkmo.testing.entry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkStatusTest {

    @Test
    fun `allowed entries follow the workday sequence`() {
        assertEquals(listOf(TimeEntryType.CLOCK_IN), WorkStatus.OFF_DUTY.allowedEntries)
        assertEquals(listOf(TimeEntryType.BREAK_START, TimeEntryType.CLOCK_OUT), WorkStatus.WORKING.allowedEntries)
        assertEquals(listOf(TimeEntryType.BREAK_END), WorkStatus.ON_BREAK.allowedEntries)
    }

    @Test
    fun `cannot clock out while on break`() {
        assertFalse(WorkStatus.ON_BREAK.canRegister(TimeEntryType.CLOCK_OUT))
        assertTrue(WorkStatus.ON_BREAK.canRegister(TimeEntryType.BREAK_END))
    }

    @Test
    fun `status comes from latest entry`() {
        assertEquals(WorkStatus.OFF_DUTY, WorkStatus.fromLatest(null))
        assertEquals(WorkStatus.WORKING, WorkStatus.fromLatest(entry(TimeEntryType.BREAK_END, "10:00")))
        assertEquals(WorkStatus.ON_BREAK, WorkStatus.fromLatest(entry(TimeEntryType.BREAK_START, "10:00")))
        assertEquals(WorkStatus.OFF_DUTY, WorkStatus.fromLatest(entry(TimeEntryType.CLOCK_OUT, "10:00")))
    }
}
