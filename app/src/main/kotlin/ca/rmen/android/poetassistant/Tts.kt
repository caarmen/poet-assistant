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

package ca.rmen.android.poetassistant

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.TextUtils
import android.util.Log
import androidx.annotation.MainThread
import ca.rmen.android.poetassistant.settings.SettingsPrefs
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.decrementAndFetch

class Tts(private val context: Context, private val settingsPrefs: SettingsPrefs) {
    companion object {
        private var TAG = Constants.TAG + Tts::class.java.simpleName
        private const val PAUSE_DURATION_MS = 500L
        private const val MIN_VOICE_PITCH = 0.25f
        private const val MIN_VOICE_SPEED = 0.25f

    }

    private var mTextToSpeech: TextToSpeech? = null
    private var mTtsStatus = TextToSpeech.ERROR

    private val mUtteranceListener = UtteranceListener()
    private val mInitListener = TtsInitListener()
    // This can't be local or it will be removed from the shared prefs manager!
    private val mTtsPrefsListener = TtsPreferenceListener()

    val ttsFlow: Flow<TtsState?>
    field = MutableSharedFlow<TtsState?>(replay=1, extraBufferCapacity = 10)

    @OptIn(ExperimentalAtomicApi::class)
    private val utteranceCount = AtomicInt(0)

    init {
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(mTtsPrefsListener)
        init()
    }

    private fun init() {
        Log.v(TAG, "init: initListener = $mInitListener")
        ttsFlow.tryEmit(TtsState(previousStatus = null, currentStatus = TtsState.TtsStatus.INITIALIZED, utteranceId = null))
        mTextToSpeech = TextToSpeech(context, mInitListener)
        mTextToSpeech?.setOnUtteranceProgressListener(mUtteranceListener)
    }

    fun getTextToSpeech(): TextToSpeech? {
        if (isReady()) return mTextToSpeech
        return null
    }

    /**
     * Force a reinitialization of the TextToSpeech.
     * One use case: if the user changed the default engine, a restart is required to get the new list of voices.
     */
    @MainThread
    fun restart() {
        Log.v(TAG, "restart")
        shutdown()
        init()
    }

    fun getTtsState(): TtsState? = ttsFlow.replayCache.last()

    fun isSpeaking(): Boolean = isReady() && mTextToSpeech!!.isSpeaking

    @OptIn(ExperimentalAtomicApi::class)
    fun speak(text: String) {
        if (!isReady()) return
        val splitText = TtsSplitter.split(text)
        utteranceCount.store(splitText.size)
        splitText.forEach {
            if (TextUtils.isEmpty(it)) {
                mTextToSpeech?.playSilentUtterance(PAUSE_DURATION_MS, TextToSpeech.QUEUE_ADD, TAG)
            } else {
                mTextToSpeech?.speak(it, TextToSpeech.QUEUE_ADD, null, TAG)
            }
        }
    }

    fun speakToFile(text: String, coroutineScope: CoroutineScope, ioDispatcher: CoroutineDispatcher) {
        if (!isReady()) return
        val poemAudioExport = PoemAudioExport(context, this)
        poemAudioExport.speakToFile(mTextToSpeech!!, text, coroutineScope, ioDispatcher)
    }

    fun stop() {
        mTextToSpeech?.stop()
    }

    @MainThread
    fun shutdown() {
        mTextToSpeech?.let {
            it.setOnUtteranceProgressListener(null)
            @Suppress("DEPRECATION")
            it.setOnUtteranceCompletedListener(null)
            it.shutdown()
            mTtsStatus = TextToSpeech.ERROR
            ttsFlow.tryEmit(TtsState(TtsState.TtsStatus.INITIALIZED, TtsState.TtsStatus.UNINITIALIZED, null))
            mTextToSpeech = null
        }
        PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(mTtsPrefsListener)
    }

    private fun useVoiceFromSettings() = useVoice(mTextToSpeech, settingsPrefs.voice)

    private fun setVoiceSpeedFromSettings() =
            mTextToSpeech?.setSpeechRate(Math.max(MIN_VOICE_SPEED, settingsPrefs.voiceSpeed / 100f))

    private fun setVoicePitchFromSettings() =
            mTextToSpeech?.setPitch(Math.max(MIN_VOICE_PITCH, settingsPrefs.voicePitch / 100f))

    private fun useVoice(textToSpeech: TextToSpeech?, voiceId: String?) {
        if (textToSpeech != null) {
            try {
                if (voiceId == null || SettingsPrefs.VOICE_SYSTEM == voiceId) {
                    textToSpeech.voice = textToSpeech.defaultVoice
                    Log.v(TAG, "Using default voice ${textToSpeech.defaultVoice}")
                } else {
                    textToSpeech.voice = textToSpeech.voices
                            .asSequence()
                            .filter { voice ->
                                voiceId == voice.name
                            }
                            // If the user changed the tts engine in the system settings, we may not find
                            // the previous voice they selected.
                            .elementAtOrElse(0) { textToSpeech.defaultVoice }
                    Log.v(TAG, "Using voice ${textToSpeech.voice}")
                }
            } catch (t: Throwable) {
                // This happens if I choose "SoundAbout TTS" as the preferred engine.
                // That implementation throws a NullPointerException.
                Log.w(TAG, "Couldn't load the tts voices: ${t.message}", t)
            }
        }
    }

    private fun isReady() = mTextToSpeech != null && mTtsStatus == TextToSpeech.SUCCESS

    private inner class TtsInitListener : TextToSpeech.OnInitListener {
        override fun onInit(status: Int) {
            Log.v(TAG, "onInit: status = $status")
            mTtsStatus = status
            useVoiceFromSettings()

            if (status == TextToSpeech.SUCCESS) {
                setVoiceSpeedFromSettings()
                setVoicePitchFromSettings()
                ttsFlow.tryEmit(TtsState(TtsState.TtsStatus.UNINITIALIZED, TtsState.TtsStatus.INITIALIZED, null))
                ttsFlow.tryEmit(TtsState(TtsState.TtsStatus.INITIALIZED, TtsState.TtsStatus.INITIALIZED, null))
            } else {
                ttsFlow.tryEmit(TtsState(TtsState.TtsStatus.UNINITIALIZED, TtsState.TtsStatus.UNINITIALIZED, null))
            }
        }
    }

    private inner class TtsPreferenceListener : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
            if (isReady()) {
                when (key) {
                    SettingsPrefs.PREF_VOICE_SPEED -> setVoiceSpeedFromSettings()
                    SettingsPrefs.PREF_VOICE_PITCH -> setVoicePitchFromSettings()
                    SettingsPrefs.PREF_VOICE -> useVoiceFromSettings()
                }
            }
        }
    }

    private inner class UtteranceListener : UtteranceProgressListener() {
        override fun onStart(utteranceId: String) {
            ttsFlow.tryEmit(TtsState(TtsState.TtsStatus.INITIALIZED, TtsState.TtsStatus.SPEAKING, utteranceId))
        }

        override fun onDone(utteranceId: String) = onUtteranceCompleted(utteranceId)

        @Suppress("OverridingDeprecatedMember")
        override fun onError(utteranceId: String) = onUtteranceError(utteranceId)

        override fun onError(utteranceId: String, errorCode: Int) {
            super.onError(utteranceId, errorCode)
            onUtteranceError(utteranceId)
        }

        override fun onStop(utteranceId: String, interrupted: Boolean) {
            super.onStop(utteranceId, interrupted)
            onUtteranceCompleted(utteranceId)
        }

        @OptIn(ExperimentalAtomicApi::class)
        private fun onUtteranceCompleted(utteranceId: String) {
            val newUtteranceCount = utteranceCount.decrementAndFetch()
            if (newUtteranceCount <= 0) {
                utteranceCount.store(0)
                ttsFlow.tryEmit(TtsState(TtsState.TtsStatus.SPEAKING, TtsState.TtsStatus.UTTERANCE_COMPLETE, utteranceId))
                ttsFlow.tryEmit(TtsState(TtsState.TtsStatus.UTTERANCE_COMPLETE, TtsState.TtsStatus.INITIALIZED, null))
            }
        }

        @OptIn(ExperimentalAtomicApi::class)
        private fun onUtteranceError(utteranceId: String) {
            utteranceCount.store(0)
            ttsFlow.tryEmit(TtsState(TtsState.TtsStatus.SPEAKING, TtsState.TtsStatus.UTTERANCE_ERROR, utteranceId))
            ttsFlow.tryEmit(TtsState(TtsState.TtsStatus.UTTERANCE_ERROR, TtsState.TtsStatus.INITIALIZED, utteranceId))
        }
    }
}
