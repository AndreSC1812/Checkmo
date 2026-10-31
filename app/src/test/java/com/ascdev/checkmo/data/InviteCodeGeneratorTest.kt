package com.ascdev.checkmo.data

import com.ascdev.checkmo.data.auth.InviteCodeGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class InviteCodeGeneratorTest {

    @Test
    fun `generates codes of fixed length without ambiguous characters`() {
        val random = Random(seed = 42)
        repeat(500) {
            val code = InviteCodeGenerator.generate(random)
            assertEquals(InviteCodeGenerator.CODE_LENGTH, code.length)
            assertTrue(code, code.none { it in "01OIL" })
        }
    }

    @Test
    fun `normalize trims and uppercases`() {
        assertEquals("K7P2QX", InviteCodeGenerator.normalize("  k7p2qx "))
    }
}
