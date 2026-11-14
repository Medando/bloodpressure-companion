package de.medando.bloodpressure.ui.reading.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.medando.bloodpressure.domain.model.Reading
import de.medando.bloodpressure.domain.repository.ReadingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

enum class ValueField(val default: Int) {
    SYSTOLIC(Reading.DEFAULT_SYSTOLIC),
    DIASTOLIC(Reading.DEFAULT_DIASTOLIC),
    PULSE(Reading.DEFAULT_PULSE),
}

/** Field values are kept as typed text, so an empty or partial entry survives recomposition. */
data class ReadingEditorUiState(
    val values: Map<ValueField, String> = ValueField.entries.associateWith { it.default.toString() },
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
) {
    fun isValid(field: ValueField): Boolean = values.getValue(field).toIntOrNull() in Reading.VALUE_RANGE
    /** Stays false after a successful save, so a second tap cannot store a duplicate. */
    val canSave: Boolean get() = !isSaving && !isSaved && ValueField.entries.all(::isValid)
}

@HiltViewModel
class ReadingEditorViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val readings: ReadingRepository,
    private val clock: Clock,
) : ViewModel() {

    // Typed values are mirrored into the SavedStateHandle so they survive process death.
    private val _uiState = MutableStateFlow(
        ReadingEditorUiState(
            values = ValueField.entries.associateWith { savedStateHandle[it.name] ?: it.default.toString() },
        ),
    )
    val uiState: StateFlow<ReadingEditorUiState> = _uiState.asStateFlow()

    fun onValueChange(field: ValueField, text: String) {
        setValue(field, text.filter(Char::isDigit).take(MAX_DIGITS))
    }

    /** Moves the value by [delta], starting from the default when the field is empty. */
    fun onStep(field: ValueField, delta: Int) {
        val current = _uiState.value.values.getValue(field).toIntOrNull() ?: field.default
        setValue(field, (current + delta).coerceIn(Reading.VALUE_RANGE).toString())
    }

    private fun setValue(field: ValueField, text: String) {
        savedStateHandle[field.name] = text
        _uiState.update { it.copy(values = it.values + (field to text)) }
    }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            // A failing local insert is a bug, not a user-recoverable state: let it crash.
            readings.save(
                Reading(
                    measuredAt = clock.instant(),
                    systolic = state.values.getValue(ValueField.SYSTOLIC).toInt(),
                    diastolic = state.values.getValue(ValueField.DIASTOLIC).toInt(),
                    pulse = state.values.getValue(ValueField.PULSE).toInt(),
                ),
            )
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    private companion object {
        const val MAX_DIGITS = 3
    }
}
