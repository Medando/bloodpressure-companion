package de.medando.bloodpressure.ui.settings

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.medando.bloodpressure.R
import de.medando.bloodpressure.domain.model.Settings
import de.medando.bloodpressure.domain.model.systemFirstDayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.DayOfWeek

/** Tests the stateless screen. Strings come from resources and the platform, so the device language doesn't matter. */
@RunWith(AndroidJUnit4::class)
class SettingsContentTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val events = mutableListOf<DayOfWeek?>()

    private fun string(@StringRes id: Int, vararg args: Any): String = compose.activity.getString(id, *args)

    private val locale get() = compose.activity.resources.configuration.locales[0]
    private fun dayName(day: DayOfWeek) = day.displayName(locale)
    private val systemDefault get() =
        string(R.string.settings_first_day_of_week_system, dayName(systemFirstDayOfWeek(systemLocale(compose.activity))))

    private fun option(label: String) = compose.onNode(
        hasText(label) and hasAnyAncestor(isDialog()) and
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton),
    )

    private fun setContent(firstDayOfWeek: DayOfWeek?) =
        setContent(SettingsUiState(Settings(firstDayOfWeek = firstDayOfWeek), isLoaded = true))

    private fun setContent(uiState: SettingsUiState) {
        compose.setContent {
            MaterialTheme {
                SettingsContent(
                    uiState = uiState,
                    onFirstDayOfWeekChange = { events += it },
                )
            }
        }
    }

    @Test
    fun showsSystemDefaultWithResolvedDay() {
        setContent(firstDayOfWeek = null)

        compose.onNodeWithText(string(R.string.nav_settings)).assertExists()
        compose.onNodeWithText(string(R.string.settings_first_day_of_week)).assertExists()
        compose.onNodeWithText(systemDefault).assertExists()
    }

    @Test
    fun dialogMarksCurrentChoiceAndForwardsSelection() {
        setContent(firstDayOfWeek = DayOfWeek.SUNDAY)

        compose.onNodeWithText(dayName(DayOfWeek.SUNDAY)).performClick()
        option(dayName(DayOfWeek.SUNDAY)).assertIsSelected()
        option(dayName(DayOfWeek.MONDAY)).assertIsNotSelected()
        option(dayName(DayOfWeek.SATURDAY)).assertIsNotSelected()
        option(systemDefault).performClick()

        assertEquals(listOf<DayOfWeek?>(null), events)
        compose.onNode(isDialog()).assertDoesNotExist()
    }

    @Test
    fun showsNoPreferenceUntilLoaded() {
        setContent(SettingsUiState())

        compose.onNodeWithText(string(R.string.nav_settings)).assertExists()
        compose.onNodeWithText(string(R.string.settings_first_day_of_week)).assertDoesNotExist()
    }

    @Test
    fun storedDayOutsideTheOptionsIsListedAndSelected() {
        setContent(firstDayOfWeek = DayOfWeek.TUESDAY)

        compose.onNodeWithText(dayName(DayOfWeek.TUESDAY)).performClick()
        option(dayName(DayOfWeek.TUESDAY)).assertIsSelected()
        option(systemDefault).assertIsNotSelected()
    }

    @Test
    fun cancelClosesDialogWithoutChange() {
        setContent(firstDayOfWeek = null)

        compose.onNodeWithText(systemDefault).performClick()
        compose.onNodeWithText(string(R.string.action_cancel)).performClick()

        assertEquals(emptyList<DayOfWeek?>(), events)
        compose.onNode(isDialog()).assertDoesNotExist()
    }
}
