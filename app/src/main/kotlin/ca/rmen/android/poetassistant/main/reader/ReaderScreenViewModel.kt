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
import androidx.lifecycle.viewModelScope
import ca.rmen.android.poetassistant.TtsState
import ca.rmen.android.poetassistant.main.common.models.Share
import ca.rmen.android.poetassistant.main.common.usecases.GetTtsStateUseCase
import ca.rmen.android.poetassistant.main.reader.data.Poem
import ca.rmen.android.poetassistant.main.reader.data.PoemFileRepository
import ca.rmen.android.poetassistant.main.reader.data.PoemRepository
import ca.rmen.android.poetassistant.main.reader.usecases.CreatePoemShareUseCase
import ca.rmen.android.poetassistant.main.reader.usecases.GetWordCountUseCase
import ca.rmen.android.poetassistant.main.reader.usecases.SpeakPoemToFileUseCase
import ca.rmen.android.poetassistant.main.reader.usecases.ToggleReaderSpeechUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

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
    private val poemRepository: PoemRepository,
    private val poemFileRepository: PoemFileRepository,
    private val getTtsStateUseCase: GetTtsStateUseCase,
    private val toggleReaderSpeechUseCase: ToggleReaderSpeechUseCase,
    private val speakPoemToFileUseCase: SpeakPoemToFileUseCase,
    private val getWordCountUseCase: GetWordCountUseCase,
    private val createPoemShareUseCase: CreatePoemShareUseCase,
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

    private val textFieldValue = MutableStateFlow(TextFieldValue(initialText()))

    /**
     * The last word count. Fed eagerly by a VM-scoped debounced
     * collector, so the count survives unsubscribes and shows at once
     * when the reader tab regains focus.
     */
    private val wordCount = MutableStateFlow<WordCount?>(null)

    init {
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            textFieldValue
                .map { it.text }
                .distinctUntilChanged()
                .debounce(500.milliseconds)
                .collect { wordCount.value = getWordCountUseCase(it) }
        }
    }

    /**
     * Current UI state for the reader screen.
     */
    val state: StateFlow<ReaderScreenState> = combine(
        textFieldValue,
        getTtsStateUseCase(),
        wordCount,
        poemRepository.poem,
    ) { textFieldValue, ttsState, wordCount, poem ->
        ReaderScreenState(
            textFieldValue = textFieldValue,
            playButtonState = toPlayButtonState(ttsState, textFieldValue.text),
            wordCount = wordCount,
            poem = poem,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState(),
    )

    /**
     * One-time event to show as a snackbar. Null means no event.
     * The UI resets it via [onEventHandled].
     */
    val event: StateFlow<ReaderEvent?> field = MutableStateFlow(null)

    /**
     * Resets the pending event after the UI showed it.
     */
    fun onEventHandled() {
        event.value = null
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
        this.textFieldValue.value = textFieldValue
    }

    /**
     * Toggles the speech playback of the poem.
     */
    fun onPlayClicked() {
        val result = toggleReaderSpeechUseCase(
            textFieldValue.value.text,
            textFieldValue.value.selection,
        )
        if (result == ToggleReaderSpeechUseCase.Result.TtsError) {
            event.value = ReaderEvent.TtsError
        }
    }

    /**
     * Appends text shared into the app to the poem: sharing must not
     * replace the user's work. Persists at once, keeping any file
     * binding.
     *
     * @param text The shared text.
     */
    fun onInitialPoemLoaded(text: String) {
        val currentText = textFieldValue.value.text
        val newText = if (currentText.isBlank()) text else "$currentText\n$text"
        poemRepository.updatePoemText(newText)
        textFieldValue.value = TextFieldValue(newText)
    }

    /**
     * Clears the poem.
     */
    fun onClearPoemClicked() {
        poemRepository.clear()
        textFieldValue.value = TextFieldValue("")
    }

    /**
     * Opens the poem at the given uri. On failure the current poem is
     * kept and a [ReaderEvent.PoemOpenError] event is emitted: a
     * failed open must not lose the user's work.
     *
     * @param uri The uri of the poem file to open.
     */
    fun onOpenPoem(uri: Uri) {
        viewModelScope.launch {
            val loadedPoem = poemFileRepository.read(uri)
            if (loadedPoem == null) {
                event.value = ReaderEvent.PoemOpenError
            } else {
                poemRepository.setPoem(loadedPoem)
                textFieldValue.value = TextFieldValue(loadedPoem.text)
                event.value = ReaderEvent.PoemOpened(loadedPoem.name)
            }
        }
    }

    /**
     * Saves the poem to the given uri.
     *
     * @param uri The uri of the poem file to write to.
     */
    fun onSavePoemAs(uri: Uri) {
        viewModelScope.launch {
            val savedPoem = poemFileRepository.save(uri, textFieldValue.value.text)
            onPoemSaved(savedPoem)
        }
    }

    /**
     * Saves the poem to its file. Silently no-ops when the poem is not
     * persisted to a file (unreachable when the menu enablement is
     * correct).
     */
    fun onSavePoem() {
        val savedPoemUri = (poemRepository.poem.value as? Poem.PersistedPoem)?.uri
        if (savedPoemUri != null) {
            viewModelScope.launch {
                val savedPoem = poemFileRepository.save(savedPoemUri, textFieldValue.value.text)
                onPoemSaved(savedPoem)
            }
        }
    }

    /**
     * Requests sharing the poem text as text. The fragment collects
     * [share], launches the chooser, then calls [onShareSent].
     */
    fun onSharePoem() {
        share.value = createPoemShareUseCase(textFieldValue.value.text)
    }

    /**
     * Resets the pending share after the chooser was launched.
     */
    fun onShareSent() {
        share.value = null
    }

    /**
     * Exports the poem to an audio file. The event fires when the
     * write starts, so the snackbar shows while the file is being
     * written.
     */
    fun onSharePoemAsAudio() {
        speakPoemToFileUseCase(textFieldValue.value.text)
        event.value = ReaderEvent.PoemSharedAsAudio
    }

    /**
     * Persists the poem text, keeping the current file binding (if
     * any). Called by the fragment on pause.
     */
    fun onPersistPoem() {
        poemRepository.updatePoemText(textFieldValue.value.text)
    }

    /**
     * Handles the result of saving the poem to a file: persists the
     * saved poem and emits a saved or error event.
     *
     * @param savedPoem The saved poem, or null on failure.
     */
    private fun onPoemSaved(savedPoem: Poem.PersistedPoem?) {
        if (savedPoem == null) {
            event.value = ReaderEvent.PoemSaveError
        } else {
            poemRepository.setPoem(savedPoem)
            event.value = ReaderEvent.PoemSaved(savedPoem.name)
        }
    }

    /**
     * The initial poem text: the stored poem's text, empty when no
     * poem is stored.
     */
    private fun initialText(): String = poemRepository.poem.value?.text.orEmpty()

    /**
     * The initial state, shown before the state flow starts emitting.
     * Sets all fields.
     */
    private fun initialState() = ReaderScreenState(
        textFieldValue = textFieldValue.value,
        playButtonState = toPlayButtonState(getTtsStateUseCase.current, textFieldValue.value.text),
        wordCount = wordCount.value,
        poem = poemRepository.poem.value,
    )

    /**
     * The play button rules: TTS initialized + non-empty text →
     * play; TTS speaking → stop; anything else → disabled.
     */
    private fun toPlayButtonState(ttsState: TtsState?, poemText: String): PlayButtonState {
        return if (ttsState?.currentStatus == TtsState.TtsStatus.INITIALIZED) {
            if (poemText.isEmpty()) PlayButtonState.PLAY_DISABLED else PlayButtonState.PLAY_ENABLED
        } else if (ttsState?.currentStatus == TtsState.TtsStatus.SPEAKING) {
            PlayButtonState.STOP
        } else {
            PlayButtonState.PLAY_DISABLED
        }
    }
}
