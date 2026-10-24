/*
 * Copyright (c) 2017-present Carmen Alvarez
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

import android.annotation.TargetApi
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.print.PrintJob
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import android.text.Selection
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.viewModelScope
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.Tts
import ca.rmen.android.poetassistant.TtsState
import ca.rmen.android.poetassistant.di.IODispatcher
import ca.rmen.android.poetassistant.main.dictionaries.Share
import ca.rmen.android.poetassistant.main.reader.data.Poem
import ca.rmen.android.poetassistant.main.reader.data.PoemFileRepository
import ca.rmen.android.poetassistant.main.reader.data.PoemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class ReaderViewModel @Inject constructor(
    application: Application,
    private val mTts: Tts,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val poemRepository: PoemRepository,
    private val poemFileRepository: PoemFileRepository,
    ) : AndroidViewModel(application) {
    companion object {
        private val TAG = Constants.TAG + ReaderViewModel::class.java.simpleName
        /**
         * The button shall be disabled if TTS isn't initialized, or if there is no text to play.
         * The button should display a "Play" icon if TTS isn't running but can be started.
         * The button should display a "Stop" icon if TTS is currently running.
         */
        private fun toPlayButtonState(ttsState: TtsState?, poemText: String?): ReaderViewModel.PlayButtonState {
            Log.v(TAG, "toPlayButtonState: ttsState = $ttsState, poemText = $poemText")
            return if (ttsState != null) {
                if (ttsState.currentStatus == TtsState.TtsStatus.INITIALIZED) {
                    if (TextUtils.isEmpty(poemText)) {
                        ReaderViewModel.PlayButtonState(false, R.drawable.ic_play_disabled)
                    } else {
                        ReaderViewModel.PlayButtonState(true, R.drawable.ic_play_enabled)
                    }
                } else if (ttsState.currentStatus == TtsState.TtsStatus.SPEAKING) {
                    ReaderViewModel.PlayButtonState(true, R.drawable.ic_stop)
                } else {
                    ReaderViewModel.PlayButtonState(false, R.drawable.ic_play_disabled)
                }
            } else {
                ReaderViewModel.PlayButtonState(false, R.drawable.ic_play_disabled)
            }
        }
    }

    class SnackbarText(@StringRes val stringResId: Int, vararg val params: Any)
    data class PlayButtonState(val isEnabled: Boolean, @DrawableRes val iconId: Int)


    val wordCountText: StateFlow<String?>
        field = MutableStateFlow(null)

    val snackbarText: StateFlow<SnackbarText?>
        field = MutableStateFlow<SnackbarText?>(null)

    val ttsError: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val poemFile: StateFlow<PoemFile?>
        field = MutableStateFlow<PoemFile?>(null)

    val poem: StateFlow<String?> field= MutableStateFlow(null)

    val playButtonStateFlow: StateFlow<PlayButtonState> =
        mTts.ttsFlow
            .combine(poem) { ttsState, poemText ->
                // This block runs whenever either ttsFlow OR poem emits a new value
                toPlayButtonState(ttsState, poemText)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                // Provide an initial value based on the current states right now
                initialValue = toPlayButtonState(
                    mTts.getTtsState(),
                    poem.value
                )
            )

    init {
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            poem.debounce(500.milliseconds).collect {
                updateWordCount()
            }
        }
        poemFile.value = poemRepository.savedPoem.value.toPoemFile()
        // Mirror the legacy PrefsListener: reflect changes to the saved
        // poem (e.g. made by other components) into poemFile.
        viewModelScope.launch {
            poemRepository.savedPoem.collect { poem ->
                poemFile.value = poem.toPoemFile()
            }
        }
    }

    /**
     * Convert a [Poem] from the new repository layer into the legacy
     * [PoemFile] shape used by the fragment-facing API.
     * Deleted in Step 2 together with this ViewModel.
     */
    private fun Poem?.toPoemFile(): PoemFile? = this?.let { PoemFile(it.uri, it.name, it.text) }

    fun updateWordCount() {
        wordCountText.value = WordCounter.getWordCountText(getApplication(), poem.value)
    }

    // begin TTS

    fun play(charSequence: CharSequence) {
        Log.v(TAG, "Play button clicked")
        val ttsState = mTts.getTtsState()
        if (mTts.isSpeaking()) {
            mTts.stop()
        } else if (ttsState != null && ttsState.currentStatus == TtsState.TtsStatus.INITIALIZED) {
            speakSelectedText(charSequence)
        } else {
            ttsError.value = true
            ttsError.value = false
        }
    }

    fun setTypedText(typedText: String) {
        poem.value = typedText
    }


    /**
     * Read the selected text in our text view.
     */
    private fun speakSelectedText(text: CharSequence) {
        if (TextUtils.isEmpty(text)) {
            poem.value?.let {mTts.speak(it)}
        } else {
            mTts.speak(getTextToSpeak(text))
        }
    }

    fun getTextToSpeak(text: CharSequence) : String {
        var startPosition = Selection.getSelectionStart(text)
        var endPosition = Selection.getSelectionEnd(text)
        Log.d(TAG, "selection $startPosition - $endPosition")
        if (startPosition == -1) startPosition = 0
        if (endPosition == -1) endPosition = 0
        if (startPosition == text.length) startPosition = 0
        // empty selection in the middle: select from the start position to the end of the text
        if (startPosition == endPosition || text.substring(startPosition, endPosition).trim().isEmpty()) endPosition = text.length
        Log.d(TAG, "now selection $startPosition - $endPosition")
        val result = text.toString().substring(startPosition, endPosition)
        if (TextUtils.isEmpty(result.trim())) return text.toString()
        return result
    }

    fun speakToFile() {
        poem.value?.let {
            mTts.speakToFile(it, viewModelScope, ioDispatcher)
            snackbarText.value = SnackbarText(R.string.share_poem_audio_snackbar)
        }
    }

    // end TTS

    // Begin saving/Opening files
    fun updatePoemText() {
        Log.d(TAG, "Update poem text")
        poem.value?.let { poemRepository.updatePoemText(it)}
    }

    fun setSavedPoem(savedPoem: PoemFile) {
        Log.v(TAG, "setSavedPoem $savedPoem")
        poemRepository.setPoem(Poem(savedPoem.uri, savedPoem.name, savedPoem.text))
        poem.value = savedPoem.text
    }

    private fun getSaveAsFilename(): String? {
        val poemFile = poemRepository.savedPoem.value
        return if (poemFile != null) {
            poemFile.name
        } else {
            poem.value?.let {PoemFile.generateFileName(it)}
        }
    }

    fun clearPoem() {
        poemRepository.clear()
        poem.value = ""
    }

    fun save(context: Context) {
        // context no longer used; parameter kept until Step 2
        val savedPoemUri = poemRepository.savedPoem.value?.uri
        if (savedPoemUri != null) {
            poem.value?.let {
                viewModelScope.launch {
                    val savedPoem = poemFileRepository.save(savedPoemUri, it).toPoemFile()
                    onPoemSaved(savedPoem)
                }
            }
        }
    }

    fun saveAs(context: Context, uri: Uri) {
        // context no longer used; parameter kept until Step 2
        poem.value?.let {
            viewModelScope.launch {
                val savedPoem = poemFileRepository.save(uri, it).toPoemFile()
                onPoemSaved(savedPoem)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun getOpenFileIntent(): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        val poemFileValue = poemFile.value
        if (poemFileValue != null) {
            intent.data = poemFileValue.uri
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        return intent
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun getSaveAsFileIntent(): Intent {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        val fileName = getSaveAsFilename()
        if (!TextUtils.isEmpty(fileName)) intent.putExtra(Intent.EXTRA_TITLE, fileName)
        return intent
    }

    fun open(context: Context, uri: Uri) {
        // context no longer used; parameter kept until Step 2
        viewModelScope.launch {
            val loadedPoem = poemFileRepository.read(uri).toPoemFile()
            onPoemLoaded(loadedPoem)
        }
    }

    fun loadPoem() {
        // Load the poem we previously saved
        val savedPoem = poemRepository.savedPoem.value
        if (savedPoem != null) {
            poem.value = savedPoem.text
        } else {
            val tempPoemText = poemRepository.getTempPoemText()
            if (tempPoemText != null) {
                poem.value = tempPoemText
            }
        }
    }

    fun sharePoem() {
        poem.value?.let { Share.share(getApplication(), it)}
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun print(context: Context) {
        val poemFileValue = poemFile.value
        if (poemFileValue == null) {
            poem.value?.let {PoemFile.print(context, PoemFile(null, PoemFile.generateFileName(it), poem.value), mPoemFileCallback)}
        } else {
            PoemFile.print(context, poemFileValue, mPoemFileCallback)
        }
    }

    private val mPoemFileCallback = object : PoemFileCallback {

        @TargetApi(Build.VERSION_CODES.KITKAT)
        override fun onPrintJobCreated(poemFile: PoemFile, printJob: PrintJob?) {
            Log.d(TAG, "onPrintJobCreated: poemFile = $poemFile, printJob = $printJob")
            if (printJob != null) {
                Log.d(TAG, "Print job id = ${printJob.id}, info = ${printJob.info}")
            }
        }
    }

    fun onPoemLoaded(poemFile: PoemFile?) {
        Log.d(TAG, "onPoemLoaded, loadedPoem = $poemFile")
        if (poemFile == null) {
            clearPoem()
            snackbarText.value = SnackbarText(R.string.file_opened_error)
        } else {
            setSavedPoem(poemFile)
            snackbarText.value = SnackbarText(R.string.file_opened, poemFile.name ?: "")
        }
    }

    fun onPoemSaved(poemFile: PoemFile?) {
        if (poemFile == null) {
            snackbarText.value = SnackbarText(R.string.file_saved_error)
        } else {
            Log.d(TAG, "onPoemSaved, savedPoem = $poemFile")
            setSavedPoem(poemFile)
            snackbarText.value = SnackbarText(R.string.file_saved, poemFile.name ?: "")
        }
    }
}
