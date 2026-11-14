package de.medando.bloodpressure.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import de.medando.bloodpressure.R
import kotlinx.serialization.Serializable

@Serializable data object ReadingsRoute : NavKey
@Serializable data object AnalysisRoute : NavKey
@Serializable data object ExportRoute : NavKey
@Serializable data object SettingsRoute : NavKey

/** Detail screens, shown on top of a tab. */
@Serializable data object ReadingEditorRoute : NavKey

/** Tabs of the navigation bar or rail. The first entry is the start destination. */
enum class TopLevelDestination(
    val route: NavKey,
    @param:StringRes val label: Int,
    @param:DrawableRes val icon: Int,
) {
    READINGS(ReadingsRoute, R.string.nav_readings, R.drawable.ic_list),
    ANALYSIS(AnalysisRoute, R.string.nav_analysis, R.drawable.ic_insights),
    EXPORT(ExportRoute, R.string.nav_export, R.drawable.ic_share),
    SETTINGS(SettingsRoute, R.string.nav_settings, R.drawable.ic_settings),
}
