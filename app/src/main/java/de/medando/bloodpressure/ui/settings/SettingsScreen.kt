package de.medando.bloodpressure.ui.settings

import android.app.LocaleManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.medando.bloodpressure.R
import de.medando.bloodpressure.domain.model.systemFirstDayOfWeek
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

/** `null` is "System default". Locales start their week on one of these days (except the Maldives' Friday). */
private val FIRST_DAY_OPTIONS = listOf(null, DayOfWeek.MONDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

/** Stateful entry point: wires the ViewModel to [SettingsContent]. */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        uiState = uiState,
        onFirstDayOfWeekChange = viewModel::onFirstDayOfWeekChange,
    )
}

/** Add further preferences as rows (and section headers) to the column. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onFirstDayOfWeekChange: (DayOfWeek?) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Pinned: the bar stays in place and takes on a container colour while the rows scroll beneath it.
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_settings)) }, scrollBehavior = scrollBehavior)
        },
    ) { innerPadding ->
        if (!uiState.isLoaded) return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            FirstDayOfWeekPreference(uiState.settings.firstDayOfWeek, onFirstDayOfWeekChange)
        }
    }
}

@Composable
private fun FirstDayOfWeekPreference(day: DayOfWeek?, onChange: (DayOfWeek?) -> Unit) {
    // Read from the configuration, so a locale change updates the names.
    val locale = LocalConfiguration.current.locales[0]
    val systemFirstDay = systemFirstDayOfWeek(systemLocale(LocalContext.current))
    val title = stringResource(R.string.settings_first_day_of_week)
    var showDialog by rememberSaveable { mutableStateOf(false) }
    // A stored day outside the offered ones (e.g. from a backup) is listed too, so one option is always selected.
    val options = if (day in FIRST_DAY_OPTIONS) FIRST_DAY_OPTIONS else FIRST_DAY_OPTIONS + day

    PreferenceRow(title = title, value = firstDayLabel(day, systemFirstDay, locale), onClick = { showDialog = true })

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                Column(Modifier.selectableGroup().verticalScroll(rememberScrollState())) {
                    options.forEach { option ->
                        RadioOption(
                            label = firstDayLabel(option, systemFirstDay, locale),
                            selected = option == day,
                            onClick = {
                                showDialog = false
                                onChange(option)
                            },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@Composable
private fun PreferenceRow(title: String, value: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        modifier = Modifier.clickable(onClick = onClick),
        supportingContent = { Text(value) },
    )
}

@Composable
private fun RadioOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The row handles clicks and semantics.
        RadioButton(selected = selected, onClick = null)
        Text(label, modifier = Modifier.padding(start = 16.dp))
    }
}

@Composable
private fun firstDayLabel(day: DayOfWeek?, systemFirstDay: DayOfWeek, locale: Locale): String =
    if (day == null) {
        stringResource(R.string.settings_first_day_of_week_system, systemFirstDay.displayName(locale))
    } else {
        day.displayName(locale)
    }

/**
 * The device's locale with its regional preferences. With a per-app language the configuration locale is
 * the app's, which says nothing about the user's calendar.
 */
internal fun systemLocale(context: Context): Locale =
    context.getSystemService(LocaleManager::class.java).systemLocales[0]

/** Capitalised, as standalone weekday names are lowercase in many languages. */
internal fun DayOfWeek.displayName(locale: Locale): String =
    getDisplayName(TextStyle.FULL_STANDALONE, locale).replaceFirstChar { it.titlecase(locale) }
