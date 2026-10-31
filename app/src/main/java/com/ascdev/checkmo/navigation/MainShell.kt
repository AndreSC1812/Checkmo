package com.ascdev.checkmo.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay

/** Navigation actions available to screens inside the shell; they act on the current tab. */
class ShellNavigator(
    val navigate: (NavKey) -> Unit,
    val goBack: () -> Unit,
)

/**
 * Bottom-bar shell for signed-in users. Each tab keeps its own back stack and decorators, so
 * switching tabs preserves screen state and ViewModels. The start tab always sits below the
 * current one, so Back from another tab's root returns to it.
 */
@Composable
fun MainShell(
    destinations: List<TopLevelDestination>,
    entryProvider: (ShellNavigator) -> (NavKey) -> NavEntry<NavKey>,
    modifier: Modifier = Modifier,
) {
    val startDestination = destinations.first()
    var currentDestination by rememberSaveable { mutableStateOf(startDestination) }

    val backStacks = destinations.associateWith { rememberNavBackStack(it.route) }
    val goBack: () -> Unit = {
        val currentBackStack = backStacks.getValue(currentDestination)
        if (currentBackStack.size > 1) {
            currentBackStack.removeLastOrNull()
        } else {
            currentDestination = startDestination
        }
    }
    val navigator = ShellNavigator(
        navigate = { route -> backStacks.getValue(currentDestination).add(route) },
        goBack = goBack,
    )
    val provider = entryProvider(navigator)
    val decoratedEntries = destinations.associateWith { destination ->
        rememberDecoratedNavEntries(
            backStack = backStacks.getValue(destination),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = provider,
        )
    }
    val entries = if (currentDestination == startDestination) {
        decoratedEntries.getValue(startDestination)
    } else {
        decoratedEntries.getValue(startDestination) + decoratedEntries.getValue(currentDestination)
    }

    Scaffold(
        modifier = modifier,
        // Screens draw their own top app bar, which already handles the status bar inset.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = destination == currentDestination,
                        onClick = {
                            if (destination == currentDestination) {
                                // Re-selecting a tab pops it back to its root.
                                backStacks.getValue(destination).retainAll(listOf(destination.route))
                            } else {
                                currentDestination = destination
                            }
                        },
                        icon = { Icon(painterResource(destination.iconRes), contentDescription = null) },
                        label = { Text(stringResource(destination.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavDisplay(
            entries = entries,
            modifier = Modifier.padding(innerPadding),
            onBack = goBack,
        )
    }
}
