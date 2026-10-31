package com.ascdev.checkmo.navigation

import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.ascdev.checkmo.domain.model.User
import com.ascdev.checkmo.feature.auth.login.LoginRoute
import com.ascdev.checkmo.feature.auth.signup.SignUpRoute
import com.ascdev.checkmo.feature.clockin.ClockInRoute
import com.ascdev.checkmo.feature.corrections.CorrectionsRoute
import com.ascdev.checkmo.feature.history.HistoryRoute
import com.ascdev.checkmo.feature.profile.ProfileRoute
import com.ascdev.checkmo.feature.reports.ReportsRoute
import com.ascdev.checkmo.feature.team.TeamRoute
import com.ascdev.checkmo.feature.workplace.WorkplaceRoute

/**
 * Root navigation. Signed-out users get the auth flow; signed-in users get the bottom-bar shell
 * for their role. Changing user or role recreates the stacks, so Back never crosses flows.
 */
@Composable
fun CheckmoNavDisplay(user: User?, modifier: Modifier = Modifier) {
    key(user?.id, user?.role) {
        if (user == null) {
            AuthNavDisplay(modifier = modifier.systemBarsPadding())
        } else {
            MainShell(
                destinations = topLevelDestinationsFor(user.role),
                modifier = modifier,
                entryProvider = { navigator ->
                    entryProvider {
                        entry<CheckmoRoute.ClockIn> { ClockInRoute() }
                        entry<CheckmoRoute.History> { HistoryRoute() }
                        entry<CheckmoRoute.Team> { TeamRoute() }
                        entry<CheckmoRoute.Corrections> { CorrectionsRoute() }
                        entry<CheckmoRoute.Reports> { ReportsRoute() }
                        entry<CheckmoRoute.Profile> {
                            ProfileRoute(onOpenWorkplace = { navigator.navigate(CheckmoRoute.Workplace) })
                        }
                        entry<CheckmoRoute.Workplace> { WorkplaceRoute(onNavigateBack = navigator.goBack) }
                    }
                },
            )
        }
    }
}

@Composable
private fun AuthNavDisplay(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(CheckmoRoute.Login)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<CheckmoRoute.Login> {
                LoginRoute(onCreateAccount = { backStack.add(CheckmoRoute.SignUp) })
            }
            entry<CheckmoRoute.SignUp> {
                SignUpRoute(onBackToLogin = { backStack.removeLastOrNull() })
            }
        },
    )
}
