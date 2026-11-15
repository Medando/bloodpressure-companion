package de.medando.bloodpressure.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.medando.bloodpressure.domain.model.Settings
import de.medando.bloodpressure.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

data class SettingsUiState(
    val settings: Settings = Settings(),
    /** False until the stored settings arrive, so defaults don't flash before the user's choices. */
    val isLoaded: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = repository.settings
        .map { SettingsUiState(settings = it, isLoaded = true) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    /** `null` follows the device locale. */
    fun onFirstDayOfWeekChange(day: DayOfWeek?) {
        viewModelScope.launch { repository.update { it.copy(firstDayOfWeek = day) } }
    }
}
