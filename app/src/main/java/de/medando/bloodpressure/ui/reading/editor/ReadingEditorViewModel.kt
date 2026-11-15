package de.medando.bloodpressure.ui.reading.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.medando.bloodpressure.domain.model.BodyPosition
import de.medando.bloodpressure.domain.model.MeasurementSite
import de.medando.bloodpressure.domain.model.Reading
import de.medando.bloodpressure.domain.repository.ReadingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

enum class ValueField(val default: Int, val of: (Reading) -> Int) {
    SYSTOLIC(Reading.DEFAULT_SYSTOLIC, Reading::systolic),
    DIASTOLIC(Reading.DEFAULT_DIASTOLIC, Reading::diastolic),
    PULSE(Reading.DEFAULT_PULSE, Reading::pulse),
}

/** Field values are kept as typed text, so an empty or partial entry survives recomposition. */
data class ReadingEditorUiState(
    val values: Map<ValueField, String> = ValueField.entries.associateWith { it.default.toString() },
    val site: MeasurementSite? = null,
    val position: BodyPosition? = null,
    val device: String? = null,
    /** True until the initial values are loaded; the form is hidden meanwhile. */
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
) {
    fun isValid(field: ValueField): Boolean = values.getValue(field).toIntOrNull() in Reading.VALUE_RANGE
    /** Stays false after a successful save, so a second tap cannot store a duplicate. */
    val canSave: Boolean get() = !isLoading && !isSaving && !isSaved && ValueField.entries.all(::isValid)
}

@HiltViewModel
class ReadingEditorViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val readings: ReadingRepository,
    private val clock: Clock,
) : ViewModel() {

    // State is mirrored into the SavedStateHandle so it survives process death; `loaded` skips the
    // pre-fill after a restore.
    private val _uiState = MutableStateFlow(
        ReadingEditorUiState(
            values = ValueField.entries.associateWith { savedStateHandle[it.name] ?: it.default.toString() },
            site = savedStateHandle[KEY_SITE],
            position = savedStateHandle[KEY_POSITION],
            device = savedStateHandle[KEY_DEVICE],
            isLoading = KEY_LOADED !in savedStateHandle,
        ),
    )
    val uiState: StateFlow<ReadingEditorUiState> = _uiState.asStateFlow()

    init {
        if (_uiState.value.isLoading) viewModelScope.launch { prefill(readings.observeLatest().first()) }
    }

    /** Copies values, site, position and device of [latest]; comment and sleep are not carried over. */
    private fun prefill(latest: Reading?) {
        if (latest != null) {
            ValueField.entries.forEach { setValue(it, it.of(latest).toString()) }
            savedStateHandle[KEY_SITE] = latest.site
            savedStateHandle[KEY_POSITION] = latest.position
            savedStateHandle[KEY_DEVICE] = latest.device
            _uiState.update { it.copy(site = latest.site, position = latest.position, device = latest.device) }
        }
        savedStateHandle[KEY_LOADED] = true
        _uiState.update { it.copy(isLoading = false) }
    }

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
                    site = state.site,
                    position = state.position,
                    device = state.device,
                ),
            )
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    private companion object {
        const val MAX_DIGITS = 3
        const val KEY_SITE = "site"
        const val KEY_POSITION = "position"
        const val KEY_DEVICE = "device"
        const val KEY_LOADED = "loaded"
    }
}
