package com.ascdev.checkmo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.ascdev.checkmo.feature.auth.login.LoginRoute
import com.ascdev.checkmo.feature.auth.signup.SignUpRoute
import com.ascdev.checkmo.feature.home.HomeRoute

/**
 * Hosts the app's navigation. Signed-in and signed-out users get separate back stacks, so
 * signing in or out replaces the whole stack and Back never returns to the other flow.
 */
@Composable
fun CheckmoNavDisplay(isSignedIn: Boolean, modifier: Modifier = Modifier) {
    key(isSignedIn) {
        val startRoute: NavKey = if (isSignedIn) CheckmoRoute.Home else CheckmoRoute.Login
        val backStack = rememberNavBackStack(startRoute)

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
                entry<CheckmoRoute.Home> { HomeRoute() }
            },
        )
    }
}
