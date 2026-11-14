package de.medando.bloodpressure.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import de.medando.bloodpressure.R
import de.medando.bloodpressure.ui.navigation.AnalysisRoute
import de.medando.bloodpressure.ui.navigation.ExportRoute
import de.medando.bloodpressure.ui.navigation.ReadingsRoute
import de.medando.bloodpressure.ui.navigation.SettingsRoute
import de.medando.bloodpressure.ui.navigation.TopLevelDestination
import de.medando.bloodpressure.ui.navigation.rememberNavigationState
import de.medando.bloodpressure.ui.navigation.toEntries

@Composable
fun BloodPressureApp() {
    val navigation = rememberNavigationState()
    // Bar on phones, rail on larger windows; hidden on detail screens.
    val layoutType = if (navigation.isAtTopLevel) {
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfoV2())
    } else {
        NavigationSuiteType.None
    }

    NavigationSuiteScaffold(
        layoutType = layoutType,
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { destination ->
                item(
                    selected = destination == navigation.topLevel,
                    onClick = { navigation.select(destination) },
                    icon = { Icon(painterResource(destination.icon), contentDescription = null) },
                    label = { Text(stringResource(destination.label)) },
                )
            }
        },
    ) {
        NavDisplay(
            entries = navigation.toEntries(
                entryProvider<NavKey> {
                    entry<ReadingsRoute> { PlaceholderScreen(R.string.nav_readings) }
                    entry<AnalysisRoute> { PlaceholderScreen(R.string.nav_analysis) }
                    entry<ExportRoute> { PlaceholderScreen(R.string.nav_export) }
                    entry<SettingsRoute> { PlaceholderScreen(R.string.nav_settings) }
                },
            ),
            onBack = navigation::back,
        )
    }
}
