package de.medando.bloodpressure.ui.reading.editor

import androidx.lifecycle.SavedStateHandle
import de.medando.bloodpressure.domain.model.Reading
import de.medando.bloodpressure.domain.repository.ReadingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
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

@OptIn(ExperimentalCoroutinesApi::class)
class ReadingEditorViewModelTest {

    private class FakeReadingRepository : ReadingRepository {
        val saved = mutableListOf<Reading>()
        override fun observeAll(): Flow<List<Reading>> = emptyFlow()
        override fun observeLatest(): Flow<Reading?> = emptyFlow()
        override suspend fun get(id: Long): Reading? = null
        override suspend fun delete(id: Long) = Unit
        override suspend fun save(reading: Reading): Long {
            saved += reading
            return saved.size.toLong()
        }
    }

    private val now = Instant.parse("2026-11-14T07:30:00Z")
    private val repository = FakeReadingRepository()
    private val savedStateHandle = SavedStateHandle()
    private lateinit var viewModel: ReadingEditorViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel = createViewModel()
    }

    private fun createViewModel() = ReadingEditorViewModel(savedStateHandle, repository, Clock.fixed(now, ZoneOffset.UTC))

    @After
    fun tearDown() = Dispatchers.resetMain()

    private val state get() = viewModel.uiState.value

    @Test
    fun `starts with default values`() {
        assertEquals(mapOf(ValueField.SYSTOLIC to "120", ValueField.DIASTOLIC to "80", ValueField.PULSE to "70"), state.values)
        assertTrue(state.canSave)
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
