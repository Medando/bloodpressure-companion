package de.medando.bloodpressure.ui.disclaimer

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.medando.bloodpressure.R
import de.medando.bloodpressure.domain.model.Settings
import de.medando.bloodpressure.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests the gate with a real ViewModel on a fake repository. */
@RunWith(AndroidJUnit4::class)
class DisclaimerGateTest {

    /** Emits nothing until [stored] is set, like a store that is still being read. */
    private class FakeSettingsRepository(initial: Settings?) : SettingsRepository {
        val stored = MutableStateFlow(initial)
        override val settings: Flow<Settings> = stored.filterNotNull()
        override suspend fun update(transform: (Settings) -> Settings) {
            stored.update { transform(it ?: Settings()) }
        }
    }

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private fun string(@StringRes id: Int): String = compose.activity.getString(id)

    private fun setContent(stored: Settings?) {
        val viewModel = DisclaimerViewModel(FakeSettingsRepository(stored))
        compose.setContent {
            MaterialTheme {
                DisclaimerGate(viewModel = viewModel) { Text(APP) }
            }
        }
    }

    @Test
    fun showsNothingWhileLoading() {
        setContent(stored = null)

        compose.onNodeWithText(string(R.string.disclaimer_title)).assertDoesNotExist()
        compose.onNodeWithText(APP).assertDoesNotExist()
    }

    @Test
    fun showsDisclaimerUntilAccepted() {
        setContent(stored = Settings())

        compose.onNodeWithText(string(R.string.disclaimer_title)).assertExists()
        compose.onNodeWithText(APP).assertDoesNotExist()

        compose.onNodeWithText(string(R.string.action_accept)).performClick()

        compose.onNodeWithText(APP).assertExists()
        compose.onNodeWithText(string(R.string.disclaimer_title)).assertDoesNotExist()
    }

    @Test
    fun showsAppWhenAlreadyAccepted() {
        setContent(stored = Settings(disclaimerAccepted = true))

        compose.onNodeWithText(APP).assertExists()
        compose.onNodeWithText(string(R.string.disclaimer_title)).assertDoesNotExist()
    }

    private companion object {
        const val APP = "app"
    }
}
