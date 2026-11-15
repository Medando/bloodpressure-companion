package de.medando.bloodpressure.ui.settings

import de.medando.bloodpressure.domain.model.Settings
import de.medando.bloodpressure.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private class FakeSettingsRepository(initial: Settings) : SettingsRepository {
        override val settings = MutableStateFlow(initial)
        override suspend fun update(transform: (Settings) -> Settings) = settings.update(transform)
    }

    private val repository = FakeSettingsRepository(Settings(firstDayOfWeek = DayOfWeek.SUNDAY))
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = SettingsViewModel(repository)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private val state get() = viewModel.uiState.value

    @Test
    fun `exposes the stored settings`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        assertTrue(state.isLoaded)
        assertEquals(DayOfWeek.SUNDAY, state.settings.firstDayOfWeek)
    }

    @Test
    fun `changing the first day of week stores it`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.onFirstDayOfWeekChange(DayOfWeek.SATURDAY)
        assertEquals(DayOfWeek.SATURDAY, repository.settings.value.firstDayOfWeek)
        assertEquals(DayOfWeek.SATURDAY, state.settings.firstDayOfWeek)

        viewModel.onFirstDayOfWeekChange(null)
        assertNull(repository.settings.value.firstDayOfWeek)
        assertNull(state.settings.firstDayOfWeek)
    }
}
