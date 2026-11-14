package de.medando.bloodpressure.ui.reading.editor

import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.medando.bloodpressure.R
import de.medando.bloodpressure.domain.model.Reading

/** Stateful entry point: wires the ViewModel to [ReadingEditorContent]. */
@Composable
fun ReadingEditorScreen(
    onDone: () -> Unit,
    viewModel: ReadingEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnDone by rememberUpdatedState(onDone)
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) currentOnDone()
    }
    ReadingEditorContent(
        uiState = uiState,
        onValueChange = viewModel::onValueChange,
        onStep = viewModel::onStep,
        onSave = viewModel::save,
        onCancel = onDone,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingEditorContent(
    uiState: ReadingEditorUiState,
    onValueChange: (ValueField, String) -> Unit,
    onStep: (ValueField, Int) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reading_editor_title_new)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(painterResource(R.drawable.ic_close), stringResource(R.string.action_cancel))
                    }
                },
                actions = {
                    TextButton(onClick = onSave, enabled = uiState.canSave) {
                        Text(stringResource(R.string.action_save))
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            // Capped so the form doesn't stretch across large windows.
            Column(modifier = Modifier.widthIn(max = 600.dp)) {
                ValueField.entries.forEach { field ->
                    ValueInput(
                        field = field,
                        text = uiState.values.getValue(field),
                        isError = !uiState.isValid(field),
                        isLast = field == ValueField.entries.last(),
                        onValueChange = { onValueChange(field, it) },
                        onStep = { onStep(field, it) },
                        onDone = onSave,
                    )
                }
            }
        }
    }
}

@Composable
private fun ValueInput(
    field: ValueField,
    text: String,
    isError: Boolean,
    isLast: Boolean,
    onValueChange: (String) -> Unit,
    onStep: (Int) -> Unit,
    onDone: () -> Unit,
) {
    val label = stringResource(field.label)

    // The whole value is selected on focus, so typing replaces the pre-filled number.
    var value by remember { mutableStateOf(TextFieldValue(text)) }
    if (value.text != text) value = TextFieldValue(text, TextRange(text.length))
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    LaunchedEffect(isFocused) {
        if (isFocused) value = value.copy(selection = TextRange(0, value.text.length))
    }

    OutlinedTextField(
        value = value,
        onValueChange = {
            value = it
            if (it.text != text) onValueChange(it.text)
        },
        modifier = Modifier
            .fillMaxWidth()
            // Announces a value changed by the steppers.
            .semantics { liveRegion = LiveRegionMode.Polite },
        label = { Text(label) },
        leadingIcon = {
            IconButton(onClick = { onStep(-1) }) {
                Icon(painterResource(R.drawable.ic_remove), stringResource(R.string.action_decrease, label))
            }
        },
        trailingIcon = {
            IconButton(onClick = { onStep(1) }) {
                Icon(painterResource(R.drawable.ic_add), stringResource(R.string.action_increase, label))
            }
        },
        suffix = { Text(stringResource(field.unit)) },
        isError = isError,
        // Always present, so the layout doesn't jump while a field is briefly empty.
        supportingText = {
            Text(
                if (isError) {
                    stringResource(R.string.error_value_range, Reading.VALUE_RANGE.first, Reading.VALUE_RANGE.last)
                } else {
                    ""
                },
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = if (isLast) ImeAction.Done else ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        interactionSource = interactionSource,
    )
}

@get:StringRes
private val ValueField.label: Int
    get() = when (this) {
        ValueField.SYSTOLIC -> R.string.field_systolic
        ValueField.DIASTOLIC -> R.string.field_diastolic
        ValueField.PULSE -> R.string.field_pulse
    }

@get:StringRes
private val ValueField.unit: Int
    get() = when (this) {
        ValueField.SYSTOLIC, ValueField.DIASTOLIC -> R.string.unit_mmhg
        ValueField.PULSE -> R.string.unit_pulse
    }
