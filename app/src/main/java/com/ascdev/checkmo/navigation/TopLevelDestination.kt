package com.ascdev.checkmo.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.ascdev.checkmo.R
import com.ascdev.checkmo.domain.model.UserRole

enum class TopLevelDestination(
    val route: CheckmoRoute,
    @param:DrawableRes val iconRes: Int,
    @param:StringRes val labelRes: Int,
) {
    CLOCK_IN(CheckmoRoute.ClockIn, R.drawable.ic_schedule, R.string.nav_clock_in),
    HISTORY(CheckmoRoute.History, R.drawable.ic_history, R.string.nav_history),
    TEAM(CheckmoRoute.Team, R.drawable.ic_group, R.string.nav_team),
    CORRECTIONS(CheckmoRoute.Corrections, R.drawable.ic_edit, R.string.nav_corrections),
    REPORTS(CheckmoRoute.Reports, R.drawable.ic_description, R.string.nav_reports),
    PROFILE(CheckmoRoute.Profile, R.drawable.ic_person, R.string.nav_profile),
}

/** Bottom bar tabs for each role. The first one is the start destination. */
fun topLevelDestinationsFor(role: UserRole): List<TopLevelDestination> = when (role) {
    UserRole.EMPLOYEE -> listOf(
        TopLevelDestination.CLOCK_IN,
        TopLevelDestination.HISTORY,
        TopLevelDestination.PROFILE,
    )
    UserRole.COMPANY -> listOf(
        TopLevelDestination.TEAM,
        TopLevelDestination.CORRECTIONS,
        TopLevelDestination.REPORTS,
        TopLevelDestination.PROFILE,
    )
}
