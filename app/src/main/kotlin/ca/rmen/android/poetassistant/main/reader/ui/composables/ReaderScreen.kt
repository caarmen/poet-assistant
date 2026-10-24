/*
 * Copyright (c) 2016-present Carmen Alvarez
 *
 * This file is part of Poet Assistant.
 *
 * Poet Assistant is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Poet Assistant is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Poet Assistant.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.rmen.android.poetassistant.main.reader.ui.composables

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.reader.PlayButtonState
import ca.rmen.android.poetassistant.main.reader.ReaderScreenViewModel

const val READER_POEM_TEXT_FIELD_TAG = "Reader_Poem_Text_Field"
const val READER_PLAY_BUTTON_TAG = "Reader_Play_Button"

/**
 * Top-level composable for the reader screen.
 * Collects the ViewModel state and events, and observes the IME
 * insets.
 *
 * @param viewModel The ReaderScreenViewModel.
 * @param onEvent Callback for one-time events (snackbars). The
 * fragment resolves the message and any action.
 * @param onImeClosed Called when the keyboard closes, to expand the
 * app bar.
 * @param modifier The modifier for this composable.
 */
@Composable
fun ReaderScreen(
    viewModel: ReaderScreenViewModel,
    onEvent: (ReaderScreenViewModel.ReaderEvent) -> Unit,
    onImeClosed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val event by viewModel.event.collectAsStateWithLifecycle()

    LaunchedEffect(event) {
        event?.let {
            onEvent(it)
            viewModel.onEventHandled()
        }
    }

    // Expand the app bar when the keyboard closes.
    val density = LocalDensity.current
    val isImeVisible = WindowInsets.ime.getBottom(density) > 0
    var wasImeVisible by remember { mutableStateOf(isImeVisible) }
    LaunchedEffect(isImeVisible) {
        if (wasImeVisible && !isImeVisible) onImeClosed()
        wasImeVisible = isImeVisible
    }

    var showWordCountHelpDialog by remember { mutableStateOf(false) }

    ReaderScreenContent(
        state = state,
        onPoemEdited = viewModel::onPoemEdited,
        onPlayClicked = viewModel::onPlayClicked,
        onWordCountClicked = { showWordCountHelpDialog = true },
        modifier = modifier,
    )

    if (showWordCountHelpDialog) {
        ReaderWordCountHelpDialog(onDismiss = { showWordCountHelpDialog = false })
    }
}

