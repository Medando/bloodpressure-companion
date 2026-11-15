package de.medando.bloodpressure.ui.reading.editor

import androidx.lifecycle.SavedStateHandle
import de.medando.bloodpressure.domain.model.BodyPosition
import de.medando.bloodpressure.domain.model.MeasurementSite
import de.medando.bloodpressure.domain.model.Reading
import de.medando.bloodpressure.domain.repository.ReadingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalCoroutinesApi::class)
class ReadingEditorViewModelTest {

    private class FakeReadingRepository : ReadingRepository {
        val saved = mutableListOf<Reading>()
        var latest: Reading? = null
        override fun observeAll(): Flow<List<Reading>> = emptyFlow()
        override fun observeLatest(): Flow<Reading?> = flowOf(latest)
        override suspend fun get(id: Long): Reading? = null
        override suspend fun delete(id: Long) = Unit
        override suspend fun save(reading: Reading): Long {
            saved += reading
            return saved.size.toLong()
        }
    }

    private val now = Instant.parse("2026-11-14T07:30:00Z")
    private val latest = Reading(
        id = 7,
        measuredAt = Instant.parse("2026-11-13T21:00:00Z"),
        systolic = 142,
        diastolic = 91,
        pulse = 58,
        site = MeasurementSite.LEFT_WRIST,
        position = BodyPosition.STANDING,
        device = "Cuff",
        sleep = 7.hours,
        comment = "after coffee",
    )
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeReadingRepository()
    private val savedStateHandle = SavedStateHandle()
    private lateinit var viewModel: ReadingEditorViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = createViewModel()
    }

    /** Creates the ViewModel without running the pre-fill. */
    private fun newViewModel(handle: SavedStateHandle = savedStateHandle) =
        ReadingEditorViewModel(handle, repository, Clock.fixed(now, ZoneOffset.UTC))

    /** Creates the ViewModel and lets the pre-fill complete. */
    private fun createViewModel(handle: SavedStateHandle = savedStateHandle) =
        newViewModel(handle).also { dispatcher.scheduler.advanceUntilIdle() }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private val state get() = viewModel.uiState.value

    @Test
    fun `starts with default values`() {
        assertEquals(mapOf(ValueField.SYSTOLIC to "120", ValueField.DIASTOLIC to "80", ValueField.PULSE to "70"), state.values)
        assertTrue(state.canSave)
    }

    @Test
    fun `saving is blocked until the initial values are loaded`() {
        viewModel = newViewModel(SavedStateHandle())
        assertTrue(state.isLoading)
        assertFalse(state.canSave)

        dispatcher.scheduler.advanceUntilIdle()

        assertFalse(state.isLoading)
        assertTrue(state.canSave)
    }

    @Test
    fun `pre-fills values, site, position and device from the latest reading`() {
        repository.latest = latest
        viewModel = createViewModel(SavedStateHandle())

        assertEquals(mapOf(ValueField.SYSTOLIC to "142", ValueField.DIASTOLIC to "91", ValueField.PULSE to "58"), state.values)
        assertEquals(MeasurementSite.LEFT_WRIST, state.site)
        assertEquals(BodyPosition.STANDING, state.position)
        assertEquals("Cuff", state.device)
    }

    @Test
    fun `save copies context of the latest reading but not comment, sleep and time`() = runTest {
        repository.latest = latest
        viewModel = createViewModel(SavedStateHandle())
        viewModel.onValueChange(ValueField.PULSE, "61")

        viewModel.save()
        advanceUntilIdle()

        assertEquals(
            listOf(
                Reading(
                    measuredAt = now,
                    systolic = 142,
                    diastolic = 91,
                    pulse = 61,
                    site = MeasurementSite.LEFT_WRIST,
                    position = BodyPosition.STANDING,
                    device = "Cuff",
                ),
            ),
            repository.saved,
        )
    }

    @Test
    fun `a completed load is not repeated after restore`() {
        val handle = SavedStateHandle()
        repository.latest = latest
        viewModel = createViewModel(handle)
        viewModel.onValueChange(ValueField.SYSTOLIC, "130")
        repository.latest = latest.copy(systolic = 100, site = null, position = BodyPosition.LYING, device = "Other")

        val restored = createViewModel(handle).uiState.value

        assertFalse(restored.isLoading)
        assertEquals(mapOf(ValueField.SYSTOLIC to "130", ValueField.DIASTOLIC to "91", ValueField.PULSE to "58"), restored.values)
        assertEquals(MeasurementSite.LEFT_WRIST, restored.site)
        assertEquals(BodyPosition.STANDING, restored.position)
        assertEquals("Cuff", restored.device)
    }

    @Test
    fun `restore before the load finished loads again`() {
        repository.latest = latest
        val handle = SavedStateHandle(mapOf(ValueField.SYSTOLIC.name to "130"))

        viewModel = createViewModel(handle)

        assertFalse(state.isLoading)
        assertEquals(mapOf(ValueField.SYSTOLIC to "142", ValueField.DIASTOLIC to "91", ValueField.PULSE to "58"), state.values)
        assertEquals(MeasurementSite.LEFT_WRIST, state.site)
        assertEquals(BodyPosition.STANDING, state.position)
        assertEquals("Cuff", state.device)
    }

    @Test
    fun `input keeps at most three digits`() {
        viewModel.onValueChange(ValueField.SYSTOLIC, "1a3 5,7")

        assertEquals("135", state.values[ValueField.SYSTOLIC])
    }

    @Test
    fun `empty or out-of-range value is invalid and blocks saving`() {
        viewModel.onValueChange(ValueField.PULSE, "")
        assertFalse(state.isValid(ValueField.PULSE))
        assertFalse(state.canSave)

        viewModel.onValueChange(ValueField.PULSE, "301")
        assertFalse(state.isValid(ValueField.PULSE))

        viewModel.onValueChange(ValueField.PULSE, "300")
        assertTrue(state.canSave)
    }

    @Test
    fun `step changes the value within the range`() {
        viewModel.onStep(ValueField.DIASTOLIC, 1)
        assertEquals("81", state.values[ValueField.DIASTOLIC])

        viewModel.onValueChange(ValueField.DIASTOLIC, "300")
        viewModel.onStep(ValueField.DIASTOLIC, 1)
        assertEquals("300", state.values[ValueField.DIASTOLIC])

        viewModel.onValueChange(ValueField.DIASTOLIC, "0")
        viewModel.onStep(ValueField.DIASTOLIC, -1)
        assertEquals("0", state.values[ValueField.DIASTOLIC])
    }

    @Test
    fun `step on an empty field starts from the default`() {
        viewModel.onValueChange(ValueField.SYSTOLIC, "")
        viewModel.onStep(ValueField.SYSTOLIC, -1)

        assertEquals("119", state.values[ValueField.SYSTOLIC])
    }

    @Test
    fun `save stores the reading with the current time`() = runTest {
        viewModel.onValueChange(ValueField.SYSTOLIC, "135")
        viewModel.onValueChange(ValueField.DIASTOLIC, "85")
        viewModel.onValueChange(ValueField.PULSE, "64")

        viewModel.save()
        assertTrue(state.isSaving)
        advanceUntilIdle()

        assertEquals(listOf(Reading(measuredAt = now, systolic = 135, diastolic = 85, pulse = 64)), repository.saved)
        assertTrue(state.isSaved)
        assertFalse(state.isSaving)
    }

    @Test
    fun `a second save after completion does not store a duplicate`() = runTest {
        viewModel.save()
        advanceUntilIdle()
        assertFalse(state.canSave)

        viewModel.save()
        advanceUntilIdle()

        assertEquals(1, repository.saved.size)
    }

    @Test
    fun `typed values are restored from saved state`() {
        viewModel.onValueChange(ValueField.SYSTOLIC, "135")
        viewModel.onStep(ValueField.PULSE, -1)
        viewModel.onValueChange(ValueField.DIASTOLIC, "")

        val restored = createViewModel().uiState.value

        assertEquals(mapOf(ValueField.SYSTOLIC to "135", ValueField.DIASTOLIC to "", ValueField.PULSE to "69"), restored.values)
    }

    @Test
    fun `save does nothing while invalid or already saving`() = runTest {
        viewModel.onValueChange(ValueField.SYSTOLIC, "")
        viewModel.save()
        advanceUntilIdle()
        assertTrue(repository.saved.isEmpty())

        viewModel.onValueChange(ValueField.SYSTOLIC, "120")
        viewModel.save()
        viewModel.save()
        advanceUntilIdle()
        assertEquals(1, repository.saved.size)
    }
}
