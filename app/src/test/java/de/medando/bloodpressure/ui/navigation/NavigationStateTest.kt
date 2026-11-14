package de.medando.bloodpressure.ui.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import de.medando.bloodpressure.ui.navigation.TopLevelDestination.ANALYSIS
import de.medando.bloodpressure.ui.navigation.TopLevelDestination.READINGS
import de.medando.bloodpressure.ui.navigation.TopLevelDestination.SETTINGS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationStateTest {

    private data object DetailRoute : NavKey

    private val state = NavigationState(
        selected = mutableStateOf(READINGS),
        backStacks = TopLevelDestination.entries.associateWith { NavBackStack(it.route) },
    )

    @Test
    fun `starts on the first tab at its root`() {
        assertEquals(READINGS, state.topLevel)
        assertTrue(state.isAtTopLevel)
        assertEquals(listOf(READINGS), state.stacksInUse)
    }

    @Test
    fun `another tab is shown on top of the start tab`() {
        state.select(SETTINGS)

        assertEquals(SETTINGS, state.topLevel)
        assertEquals(listOf(READINGS, SETTINGS), state.stacksInUse)
    }

    @Test
    fun `back from a tab root returns to the start tab`() {
        state.select(ANALYSIS)
        state.back()

        assertEquals(READINGS, state.topLevel)
    }

    @Test
    fun `back from a detail screen stays on the tab`() {
        state.select(ANALYSIS)
        state.navigate(DetailRoute)
        assertFalse(state.isAtTopLevel)

        state.back()

        assertEquals(ANALYSIS, state.topLevel)
        assertTrue(state.isAtTopLevel)
    }

    @Test
    fun `navigating to the current screen again does not stack it twice`() {
        state.navigate(DetailRoute)
        state.navigate(DetailRoute)

        assertEquals(listOf(ReadingsRoute, DetailRoute), state.backStacks.getValue(READINGS).toList())
    }

    @Test
    fun `leaving a screen twice only goes back once`() {
        state.select(ANALYSIS)
        state.navigate(DetailRoute)

        state.back(from = DetailRoute)
        state.back(from = DetailRoute)

        assertEquals(ANALYSIS, state.topLevel)
        assertTrue(state.isAtTopLevel)
    }

    @Test
    fun `a tab keeps its stack while another is selected`() {
        state.navigate(DetailRoute)
        state.select(SETTINGS)
        assertTrue(state.isAtTopLevel)

        state.select(READINGS)

        assertFalse(state.isAtTopLevel)
        assertEquals(listOf(ReadingsRoute, DetailRoute), state.backStacks.getValue(READINGS).toList())
    }

    @Test
    fun `selecting the current tab again returns to its root`() {
        state.navigate(DetailRoute)
        state.select(READINGS)

        assertTrue(state.isAtTopLevel)
    }
}
