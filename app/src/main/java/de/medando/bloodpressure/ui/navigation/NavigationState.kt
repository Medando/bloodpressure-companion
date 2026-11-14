package de.medando.bloodpressure.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator

/**
 * One back stack per tab, so each tab keeps its screens and their state while another is shown.
 * Back from a tab's root returns to the start tab; back from the start tab's root leaves the app.
 */
@Stable
class NavigationState internal constructor(
    selected: MutableState<TopLevelDestination>,
    internal val backStacks: Map<TopLevelDestination, NavBackStack<NavKey>>,
) {
    var topLevel: TopLevelDestination by selected
        private set

    private val start = TopLevelDestination.entries.first()
    private val currentStack get() = backStacks.getValue(topLevel)

    /** `true` on a tab's root screen; detail screens hide the navigation bar. */
    val isAtTopLevel: Boolean get() = currentStack.size == 1

    internal val stacksInUse: List<TopLevelDestination>
        get() = if (topLevel == start) listOf(start) else listOf(start, topLevel)

    /** Switches tab; selecting the current tab again returns to its root. */
    fun select(destination: TopLevelDestination) {
        if (destination == topLevel) {
            while (currentStack.size > 1) currentStack.removeLastOrNull()
        } else {
            topLevel = destination
        }
    }

    /** Opens [route] on top of the current tab; a repeated request (double tap) is ignored. */
    fun navigate(route: NavKey) {
        if (currentStack.lastOrNull() != route) currentStack.add(route)
    }

    fun back() {
        if (currentStack.size > 1) currentStack.removeLastOrNull() else topLevel = start
    }

    /** Leaves [from] only while it is still the current screen, so a repeated request is ignored. */
    fun back(from: NavKey) {
        if (currentStack.lastOrNull() == from) back()
    }
}

@Composable
fun rememberNavigationState(): NavigationState {
    val selected = rememberSaveable { mutableStateOf(TopLevelDestination.entries.first()) }
    val backStacks = TopLevelDestination.entries.associateWith { rememberNavBackStack(it.route) }
    return remember(selected, backStacks) { NavigationState(selected, backStacks) }
}

/** Entries to display: the start tab's stack, then the selected tab's stack. */
@Composable
fun NavigationState.toEntries(entryProvider: (NavKey) -> NavEntry<NavKey>): List<NavEntry<NavKey>> {
    val decorated = backStacks.mapValues { (_, stack) ->
        rememberDecoratedNavEntries(
            backStack = stack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider,
        )
    }
    return stacksInUse.flatMap { decorated.getValue(it) }
}
