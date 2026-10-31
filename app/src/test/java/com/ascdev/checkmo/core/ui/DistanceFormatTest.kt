package com.ascdev.checkmo.core.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class DistanceFormatTest {

    @Test
    fun `meters below one kilometer`() {
        assertEquals("350 m", DistanceFormat.meters(350))
    }

    @Test
    fun `kilometers use a spanish decimal comma`() {
        assertEquals("1,5 km", DistanceFormat.meters(1_530))
        assertEquals("12 km", DistanceFormat.meters(12_000))
    }
}
