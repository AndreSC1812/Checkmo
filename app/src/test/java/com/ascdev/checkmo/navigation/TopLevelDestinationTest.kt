package com.ascdev.checkmo.navigation

import com.ascdev.checkmo.domain.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Test

class TopLevelDestinationTest {

    @Test
    fun `employee starts on clock in and sees history and profile`() {
        assertEquals(
            listOf(TopLevelDestination.CLOCK_IN, TopLevelDestination.HISTORY, TopLevelDestination.PROFILE),
            topLevelDestinationsFor(UserRole.EMPLOYEE),
        )
    }

    @Test
    fun `company starts on team and never sees employee tabs`() {
        val destinations = topLevelDestinationsFor(UserRole.COMPANY)

        assertEquals(TopLevelDestination.TEAM, destinations.first())
        assertEquals(
            emptyList<TopLevelDestination>(),
            destinations.filter { it == TopLevelDestination.CLOCK_IN || it == TopLevelDestination.HISTORY },
        )
    }

    @Test
    fun `every role can reach profile`() {
        UserRole.entries.forEach { role ->
            assertEquals(TopLevelDestination.PROFILE, topLevelDestinationsFor(role).last())
        }
    }
}
