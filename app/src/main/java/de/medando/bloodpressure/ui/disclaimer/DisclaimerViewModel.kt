package de.medando.bloodpressure.ui.disclaimer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.medando.bloodpressure.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DisclaimerState { LOADING, REQUIRED, ACCEPTED }

@HiltViewModel
class DisclaimerViewModel @Inject constructor(
    private val settings: SettingsRepository,
) : ViewModel() {

    val state: StateFlow<DisclaimerState> = settings.settings
        .map { if (it.disclaimerAccepted) DisclaimerState.ACCEPTED else DisclaimerState.REQUIRED }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DisclaimerState.LOADING)

    fun accept() {
        viewModelScope.launch { settings.update { it.copy(disclaimerAccepted = true) } }
    }
}
