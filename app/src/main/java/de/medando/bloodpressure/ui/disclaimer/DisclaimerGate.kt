package de.medando.bloodpressure.ui.disclaimer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.medando.bloodpressure.R

/** Shows [content] only once the disclaimer has been accepted; until then the disclaimer replaces it. */
@Composable
fun DisclaimerGate(
    viewModel: DisclaimerViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (state) {
        // Empty surface while the stored value loads, so neither screen flashes.
        DisclaimerState.LOADING -> Surface(modifier = Modifier.fillMaxSize()) {}
        DisclaimerState.REQUIRED -> DisclaimerContent(onAccept = viewModel::accept)
        DisclaimerState.ACCEPTED -> content()
    }
}

@Composable
fun DisclaimerContent(
    onAccept: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        // The text scrolls while the button stays pinned, so it's reachable at any font scale.
        Column(modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                // Capped so the text doesn't stretch across large windows.
                Column(modifier = Modifier.widthIn(max = 600.dp)) {
                    Text(
                        text = stringResource(R.string.disclaimer_title),
                        modifier = Modifier.semantics { heading() },
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.disclaimer_text),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.action_accept))
                }
            }
        }
    }
}
