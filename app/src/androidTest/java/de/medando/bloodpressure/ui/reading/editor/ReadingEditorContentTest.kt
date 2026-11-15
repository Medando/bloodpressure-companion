package de.medando.bloodpressure.ui.reading.editor

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.medando.bloodpressure.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests the stateless screen. Strings come from resources, so the device language doesn't matter. */
@RunWith(AndroidJUnit4::class)
class ReadingEditorContentTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val events = mutableListOf<String>()

    private fun string(@StringRes id: Int, vararg args: Any): String = compose.activity.getString(id, *args)

    private fun setContent(uiState: ReadingEditorUiState = ReadingEditorUiState()) {
        compose.setContent {
            MaterialTheme {
                ReadingEditorContent(
                    uiState = uiState,
                    onValueChange = { field, text -> events += "change $field $text" },
                    onStep = { field, delta -> events += "step $field $delta" },
                    onSave = { events += "save" },
                    onCancel = { events += "cancel" },
                )
            }
        }
    }

    @Test
    fun showsDefaultsAndEnablesSave() {
        setContent()

        compose.onNodeWithText("120").assertExists()
        compose.onNodeWithText("80").assertExists()
        compose.onNodeWithText("70").assertExists()
        compose.onNodeWithText(string(R.string.action_save)).assertIsEnabled()
    }

    @Test
    fun hidesFieldsAndDisablesSaveWhileLoading() {
        setContent(ReadingEditorUiState(isLoading = true))

        compose.onNodeWithText(string(R.string.field_systolic)).assertDoesNotExist()
        compose.onNodeWithText("120").assertDoesNotExist()
        compose.onNodeWithText(string(R.string.action_save)).assertIsNotEnabled()
    }

    @Test
    fun invalidValueShowsErrorAndDisablesSave() {
        setContent(ReadingEditorUiState(values = ReadingEditorUiState().values + (ValueField.PULSE to "")))

        compose.onNodeWithText(string(R.string.error_value_range, 0, 300)).assertExists()
        compose.onNodeWithText(string(R.string.action_save)).assertIsNotEnabled()
    }

    @Test
    fun forwardsUserActions() {
        setContent()

        compose.onNodeWithText("120").performTextReplacement("135")
        compose.onNodeWithContentDescription(string(R.string.action_increase, string(R.string.field_diastolic))).performClick()
        compose.onNodeWithContentDescription(string(R.string.action_decrease, string(R.string.field_pulse))).performClick()
        compose.onNodeWithText(string(R.string.action_save)).performClick()
        compose.onNodeWithContentDescription(string(R.string.action_cancel)).performClick()
        compose.onNodeWithText("70").performImeAction()

        assertEquals(
            listOf("change SYSTOLIC 135", "step DIASTOLIC 1", "step PULSE -1", "save", "cancel", "save"),
            events,
        )
    }
}
