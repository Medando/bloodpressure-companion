package de.medando.bloodpressure.ui.disclaimer

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.medando.bloodpressure.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests the stateless screen. Strings come from resources, so the device language doesn't matter. */
@RunWith(AndroidJUnit4::class)
class DisclaimerContentTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private var accepted = 0

    private fun string(@StringRes id: Int): String = compose.activity.getString(id)

    private fun setContent() {
        compose.setContent {
            MaterialTheme {
                DisclaimerContent(onAccept = { accepted++ })
            }
        }
    }

    @Test
    fun showsTitleAsHeadingAndText() {
        setContent()

        compose.onNode(hasText(string(R.string.disclaimer_title)) and isHeading()).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.disclaimer_text)).assertExists()
    }

    @Test
    fun acceptButtonIsDisplayedAndForwardsClicks() {
        setContent()

        compose.onNodeWithText(string(R.string.action_accept)).assertIsDisplayed().performClick()

        assertEquals(1, accepted)
    }
}
