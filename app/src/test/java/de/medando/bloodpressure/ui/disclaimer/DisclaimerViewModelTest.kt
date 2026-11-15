package de.medando.bloodpressure.ui.disclaimer

import de.medando.bloodpressure.domain.model.Settings
import de.medando.bloodpressure.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DisclaimerViewModelTest {

    /** Emits nothing until [stored] is set, like a store that is still being read. */
    private class FakeSettingsRepository : SettingsRepository {
        val stored = MutableStateFlow<Settings?>(null)
        override val settings: Flow<Settings> = stored.filterNotNull()
        override suspend fun update(transform: (Settings) -> Settings) {
            stored.update { transform(it ?: Settings()) }
        }
    }

    private val repository = FakeSettingsRepository()
    private lateinit var viewModel: DisclaimerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel = DisclaimerViewModel(repository)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private val state get() = viewModel.state.value

    // stateIn only collects the repository while someone observes the state.
    private fun TestScope.observe() {
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()
    }

    @Test
    fun `is loading until the settings are read`() = runTest {
        observe()

        assertEquals(DisclaimerState.LOADING, state)
    }

    @Test
    fun `is required when not yet accepted`() = runTest {
        repository.stored.value = Settings()
        observe()

        assertEquals(DisclaimerState.REQUIRED, state)
    }

    @Test
    fun `is accepted when acceptance is stored`() = runTest {
        repository.stored.value = Settings(disclaimerAccepted = true)
        observe()

        assertEquals(DisclaimerState.ACCEPTED, state)
    }

    @Test
    fun `accept stores acceptance and keeps other settings`() = runTest {
        repository.stored.value = Settings(name = "Ada")
        observe()

        viewModel.accept()
        advanceUntilIdle()

        assertEquals(Settings(name = "Ada", disclaimerAccepted = true), repository.stored.value)
        assertEquals(DisclaimerState.ACCEPTED, state)
    }

    @Test
    fun `accepting twice is harmless`() = runTest {
        repository.stored.value = Settings()
        observe()

        viewModel.accept()
        viewModel.accept()
        advanceUntilIdle()

        assertTrue(repository.stored.value!!.disclaimerAccepted)
        assertEquals(DisclaimerState.ACCEPTED, state)
    }
}
