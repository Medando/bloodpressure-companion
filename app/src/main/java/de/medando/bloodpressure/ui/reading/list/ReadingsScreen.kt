package de.medando.bloodpressure.ui.reading.list

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import de.medando.bloodpressure.R
import de.medando.bloodpressure.ui.PlaceholderScreen

/** The list itself comes with LS-1; until then this offers the entry point to add a reading. */
@Composable
fun ReadingsScreen(onAddReading: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = onAddReading) {
                Icon(painterResource(R.drawable.ic_add), stringResource(R.string.action_add_reading))
            }
        },
    ) { innerPadding ->
        PlaceholderScreen(
            title = R.string.nav_readings,
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),
        )
    }
}
