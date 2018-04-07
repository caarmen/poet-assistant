/*
 * Copyright (c) 2017-2018 Carmen Alvarez
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
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.print.PrintJob
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.text.Selection
import android.text.TextUtils
import android.util.Log
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.Tts
import ca.rmen.android.poetassistant.TtsState
import ca.rmen.android.poetassistant.dagger.DaggerHelper
import ca.rmen.android.poetassistant.databinding.BindingCallbackAdapter
import ca.rmen.android.poetassistant.databinding.LiveDataMapping
import ca.rmen.android.poetassistant.main.dictionaries.Share
import javax.inject.Inject

class ReaderViewModel(application: Application) : AndroidViewModel(application) {
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


    private val mPrefsListener : PrefsListener
    val poem = ObservableField<String>("")
    val playButtonDrawable = ObservableInt(R.drawable.ic_play_disabled)
    val playButtonEnabled = ObservableBoolean()
    val wordCountText = ObservableField<String>()

    val snackbarText = MutableLiveData<SnackbarText>()

    val ttsError = MutableLiveData<Boolean>()

    val poemFile = MutableLiveData<PoemFile>()

    val playButtonStateLiveData = MediatorLiveData<ReaderViewModel.PlayButtonState>()

    @Inject
    lateinit var mTts: Tts
    private val mPoemPrefs: PoemPrefs
    private val mSharedPreferences: SharedPreferences

    init {
        DaggerHelper.getMainScreenComponent(application).inject(this)
        mPoemPrefs = PoemPrefs(application)
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
        mPrefsListener = PrefsListener()
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mPrefsListener)
        poemFile.value = mPoemPrefs.getSavedPoem()
        playButtonStateLiveData.addSource(mTts.getTtsLiveData(),
                { ttsState -> playButtonStateLiveData.value = toPlayButtonState(ttsState, poem.get()) })
        playButtonStateLiveData.addSource(LiveDataMapping.fromObservableField(poem),
                { poemText -> playButtonStateLiveData.value = toPlayButtonState(mTts.getTtsState(), poemText) })
        poem.addOnPropertyChangedCallback(BindingCallbackAdapter(callback = object : BindingCallbackAdapter.Callback {
            override fun onChanged() {
                wordCountText.set(WordCounter.getWordCountText(application, poem.get()))
            }

        }))
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

    /**
     * Read the selected text in our text view.
     */
    private fun speakSelectedText(text: CharSequence) {
        if (TextUtils.isEmpty(text)) {
            poem.get()?.let {mTts.speak(it)}
        } else {
            var startPosition = Selection.getSelectionStart(text)
            var endPosition = Selection.getSelectionEnd(text)
            Log.d(TAG, "selection $startPosition - $endPosition")
            if (startPosition == -1) startPosition = 0
            if (endPosition == -1) endPosition = 0
            if (startPosition == text.length) startPosition = 0
            if (startPosition == endPosition) endPosition = text.length
            Log.d(TAG, "now selection $startPosition - $endPosition")
            mTts.speak(text.toString().substring(startPosition, endPosition))
        }
    }

    fun speakToFile() {
        poem.get()?.let {
            mTts.speakToFile(it)
            snackbarText.value = SnackbarText(R.string.share_poem_audio_snackbar)
        }
    }

    // end TTS

    // Begin saving/Opening files
    fun updatePoemText() {
        Log.d(TAG, "Update poem text")
        poem.get()?.let { mPoemPrefs.updatePoemText(it)}
    }

    fun setSavedPoem(savedPoem: PoemFile) {
        Log.v(TAG, "setSavedPoem $savedPoem")
        mPoemPrefs.setSavedPoem(savedPoem)
        poem.set(savedPoem.text)
    }

    private fun getSaveAsFilename(): String? {
        val poemFile = mPoemPrefs.getSavedPoem()
        return if (poemFile != null) {
            poemFile.name
        } else {
            poem.get()?.let {PoemFile.generateFileName(it)}
        }
    }

    fun clearPoem() {
        mPoemPrefs.clear()
        poem.set("")
    }

    fun save(context: Context) {
        val savedPoem = mPoemPrefs.getSavedPoem()
        if (savedPoem != null) {
            poem.get()?.let {
                PoemFile.save(context, savedPoem.uri, it, mPoemFileCallback)
            }
        }
    }

    fun saveAs(context: Context, uri: Uri) {
        poem.get()?.let {
            PoemFile.save(context, uri, it, mPoemFileCallback)
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
        PoemFile.open(context, uri, mPoemFileCallback)
    }

    fun loadPoem() {
        // Load the poem we previously saved
        if (mPoemPrefs.hasSavedPoem()) {
            val savedPoem = mPoemPrefs.getSavedPoem()
            if (savedPoem != null) {
                poem.set(savedPoem.text)
            }
        } else if (mPoemPrefs.hasTempPoem()) {
            val tempPoemText = mPoemPrefs.getTempPoem()
            poem.set(tempPoemText)
        }
    }

    fun sharePoem() {
        poem.get()?.let { Share.share(getApplication(), it)}
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun print(context: Context) {
        val poemFileValue = poemFile.value
        if (poemFileValue == null) {
            poem.get()?.let {PoemFile.print(context, PoemFile(null, PoemFile.generateFileName(it), poem.get()), mPoemFileCallback)}
        } else {
            PoemFile.print(context, poemFileValue, mPoemFileCallback)
        }
    }

    private val mPoemFileCallback = object : PoemFileCallback {
        override fun onPoemLoaded(poemFile: PoemFile?) {
            Log.d(TAG, "onPoemLoaded, loadedPoem = $poemFile")
            if (poemFile == null) {
                clearPoem()
                snackbarText.value = SnackbarText(R.string.file_opened_error)
            } else {
                setSavedPoem(poemFile)
                snackbarText.value = SnackbarText(R.string.file_opened, poemFile.name ?: "")
            }
        }

        override fun onPoemSaved(poemFile: PoemFile?) {
            if (poemFile == null) {
                snackbarText.value = SnackbarText(R.string.file_saved_error)
            } else {
                Log.d(TAG, "onPoemSaved, savedPoem = $poemFile")
                setSavedPoem(poemFile)
                snackbarText.value = SnackbarText(R.string.file_saved, poemFile.name ?: "")
            }
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        override fun onPrintJobCreated(poemFile: PoemFile, printJob: PrintJob?) {
            Log.d(TAG, "onPrintJobCreated: poemFile = $poemFile, printJob = $printJob")
            if (printJob != null) {
                Log.d(TAG, "Print job id = ${printJob.id}, info = ${printJob.info}")
            }
        }
    }

    private inner class PrefsListener : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            Log.v(TAG, "onSharedPreferenceChanged: key=$key")
            // Prevent the search EditText from disappearing while the user is typing,
            // by only notifying actual changes in the poem text.
            // When starting a new instrumentation test after completing another instrumentation
            // test, we get a shared prefs change with a null value before and after for the shared poem.
            // This resulted in an invalidation of the options menu, causing problems when entering
            // search text.
            val oldPoemText = poemFile.value
            val newPoemText = mPoemPrefs.getSavedPoem()
            Log.v(TAG, "old: $oldPoemText, new: $newPoemText")
            if ((oldPoemText == null && newPoemText == null)
                    || (oldPoemText != null && oldPoemText == newPoemText)
                    || (newPoemText != null && newPoemText == oldPoemText)) {
                Log.v(TAG, "Ignoring uninteresting poem file change")
            } else {
                poemFile.value = mPoemPrefs.getSavedPoem()
            }
        }
    }

    override fun onCleared() {
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mPrefsListener)
        super.onCleared()
    }

}
