package com.ascdev.checkmo.core.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration

class TimeFormatTest {

    @Test
    fun `clock pads hours minutes and seconds`() {
        assertEquals("00:00:00", TimeFormat.clock(Duration.ZERO))
        assertEquals("07:05:09", TimeFormat.clock(Duration.ofSeconds(7 * 3600 + 5 * 60 + 9)))
    }

    @Test
    fun `clock does not wrap after 24 hours`() {
        assertEquals("40:00:00", TimeFormat.clock(Duration.ofHours(40)))
    }

    @Test
    fun `hours minutes hides hours under one hour`() {
        assertEquals("45 min", TimeFormat.hoursMinutes(Duration.ofMinutes(45)))
        assertEquals("7 h 05 min", TimeFormat.hoursMinutes(Duration.ofMinutes(425)))
    }

    @Test
    fun `negative durations are shown as zero`() {
        assertEquals("00:00:00", TimeFormat.clock(Duration.ofSeconds(-5)))
        assertEquals("0 min", TimeFormat.hoursMinutes(Duration.ofMinutes(-5)))
    }
}
