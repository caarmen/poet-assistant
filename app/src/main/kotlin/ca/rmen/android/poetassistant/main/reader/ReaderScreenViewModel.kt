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

package ca.rmen.android.poetassistant.main.reader

import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import ca.rmen.android.poetassistant.main.common.models.Share
import ca.rmen.android.poetassistant.main.reader.data.Poem
import ca.rmen.android.poetassistant.main.reader.data.PoemFileRepository
import ca.rmen.android.poetassistant.main.reader.data.PoemRepository
import ca.rmen.android.poetassistant.main.reader.usecases.CreatePoemShareUseCase
import ca.rmen.android.poetassistant.main.reader.usecases.GetWordCountUseCase
import ca.rmen.android.poetassistant.main.reader.usecases.SpeakPoemToFileUseCase
import ca.rmen.android.poetassistant.main.reader.usecases.ToggleReaderSpeechUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * ViewModel for the reader screen.
 * The ViewModel owns the full TextFieldValue (text + selection) so the
 * selection survives configuration changes.
 *
 * @param poemRepository Repository for poem persistence.
 * @param poemFileRepository Repository for poem file I/O.
 * @param getTtsStateUseCase Use case for observing the TTS state.
 * @param toggleReaderSpeechUseCase Use case for play/stop.
 * @param speakPoemToFileUseCase Use case for the audio file export.
 * @param getWordCountUseCase Use case for the word count.
 * @param createPoemShareUseCase Use case for creating shareable content.
 */
@HiltViewModel
class ReaderScreenViewModel @Inject constructor(
) : ViewModel() {

    /**
     * One-time UI events, shown as snackbars. Null means no event is
     * pending; the UI resets via [onEventHandled].
     */
    sealed class ReaderEvent {
        object TtsError : ReaderEvent()
        data class PoemOpened(val fileName: String?) : ReaderEvent()
        data class PoemSaved(val fileName: String?) : ReaderEvent()
        object PoemOpenError : ReaderEvent()
        object PoemSaveError : ReaderEvent()
        object PoemSharedAsAudio : ReaderEvent()
    }

    private val textFieldValue = MutableStateFlow(TextFieldValue("hello"))

    /**
     * Current UI state for the reader screen.
     */
    val state: StateFlow<ReaderScreenState> = MutableStateFlow(initialState())

    /**
     * One-time event to show as a snackbar. Null means no event.
     * The UI resets it via [onEventHandled].
     */
    val event: StateFlow<ReaderEvent?> field = MutableStateFlow(null)

    /**
     * Resets the pending event after the UI showed it.
     */
    fun onEventHandled() {
    }

    /**
     * Share data to be shared. Null means no share action is pending.
     */
    val share: StateFlow<Share?> field = MutableStateFlow(null)

    /**
     * Updates the poem text and selection. Does not persist; the poem
     * persists on pause ([onPersistPoem]).
     *
     * @param textFieldValue The new text and selection.
     */
    fun onPoemEdited(textFieldValue: TextFieldValue) {
    }

    /**
     * Toggles the speech playback of the poem.
     */
    fun onPlayClicked() {
    }

    /**
     * Appends text shared into the app to the poem: sharing must not
     * replace the user's work. Persists at once, keeping any file
     * binding.
     *
     * @param text The shared text.
     */
    fun onInitialPoemLoaded(text: String) {
    }

    /**
     * The user requested to clear the poem.
     * We must ask for confirmation before actually clearing it.
     */
    fun onClearPoemRequestClicked() {
    }

    fun onClearPoemRequestDismissed() {
    }

    /**
     * Clears the poem.
     */
    fun onClearPoemConfirmClicked() {
    }

    /**
     * Opens the poem at the given uri. On failure the current poem is
     * kept and a [ReaderEvent.PoemOpenError] event is emitted: a
     * failed open must not lose the user's work.
     *
     * @param uri The uri of the poem file to open.
     */
    fun onOpenPoem(uri: Uri) {
    }

    /**
     * Saves the poem to the given uri.
     *
     * @param uri The uri of the poem file to write to.
     */
    fun onSavePoemAs(uri: Uri) {
    }

    /**
     * Saves the poem to its file. Silently no-ops when the poem is not
     * persisted to a file (unreachable when the menu enablement is
     * correct).
     */
    fun onSavePoem() {
    }

    /**
     * Requests sharing the poem text as text. The fragment collects
     * [share], launches the chooser, then calls [onShareSent].
     */
    fun onSharePoem() {
    }

    /**
     * Resets the pending share after the chooser was launched.
     */
    fun onShareSent() {
    }

    /**
     * Exports the poem to an audio file. The event fires when the
     * write starts, so the snackbar shows while the file is being
     * written.
     */
    fun onSharePoemAsAudio() {
    }

    /**
     * Persists the poem text, keeping the current file binding (if
     * any). Called by the fragment on pause.
     */
    fun onPersistPoem() {
    }

    /**
     * The initial state, shown before the state flow starts emitting.
     * Sets all fields.
     */
    private fun initialState() = ReaderScreenState(
        textFieldValue = textFieldValue.value,
        dialogState = null,
        playButtonState = PlayButtonState.PLAY_ENABLED,
        wordCount = WordCount(2, 3),
        poem = Poem.TempPoem("hello")
    )

}
