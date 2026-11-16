package de.medando.bloodpressure.ui.reading.editor

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.medando.bloodpressure.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    fun invalidValueTintsBothSteppers() {
        setContent(ReadingEditorUiState(values = ReadingEditorUiState().values + (ValueField.PULSE to "")))
        val pulse = string(R.string.field_pulse)
        val error = lightColorScheme().error

        assertTrue(compose.onNodeWithContentDescription(string(R.string.action_decrease, pulse)).hasPixel(error))
        assertTrue(compose.onNodeWithContentDescription(string(R.string.action_increase, pulse)).hasPixel(error))
    }

    @Test
    fun steppersExposeValueAsState() {
        setContent()
        val systolic = string(R.string.field_systolic)
        val hasValue = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "120")

        compose.onNodeWithContentDescription(string(R.string.action_decrease, systolic)).assert(hasValue)
        compose.onNodeWithContentDescription(string(R.string.action_increase, systolic)).assert(hasValue)
        compose.onNodeWithText("120").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.LiveRegion))
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

    private fun SemanticsNodeInteraction.hasPixel(color: Color): Boolean {
        val pixels = captureToImage().toPixelMap()
        return (0 until pixels.width).any { x -> (0 until pixels.height).any { y -> pixels[x, y] == color } }
    }
}
