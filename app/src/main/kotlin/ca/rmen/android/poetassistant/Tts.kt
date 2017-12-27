/*
 * Copyright (c) 2016 - 2017 Carmen Alvarez
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

import android.annotation.TargetApi
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.TextUtils
import android.util.Log
import ca.rmen.android.poetassistant.settings.Settings
import ca.rmen.android.poetassistant.settings.SettingsPrefs
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

class Tts(private val context: Context, private val settingsPrefs: SettingsPrefs) {
    companion object {
        private var TAG = Constants.TAG + Tts::class.java.simpleName
        private const val PAUSE_DURATION_MS = 500L
        private const val MIN_VOICE_PITCH = 0.25f
        private const val MIN_VOICE_SPEED = 0.25f

    }

    private var mTextToSpeech: TextToSpeech? = null
    private var mTtsStatus = TextToSpeech.ERROR

    private val mTtsLiveData = MutableLiveData<TtsState>()
    private val mUtteranceListener = UtteranceListener()
    private val mInitListener = TtsInitListener()
    // This can't be local or it will be removed from the shared prefs manager!
    private val mTtsPrefsListener = TtsPreferenceListener()


    fun getTtsLiveData(): LiveData<TtsState?> = mTtsLiveData

    init {
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(mTtsPrefsListener)
        init()
    }

    private fun init() {
        Log.v(TAG, "init: initListener = $mInitListener")
        mTtsLiveData.value = TtsState(null, TtsState.TtsStatus.INITIALIZED, null)
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
    fun restart() {
        Log.v(TAG, "restart")
        shutdown()
        init()
    }

    fun getTtsState(): TtsState? = mTtsLiveData.value

    fun isSpeaking(): Boolean = isReady() && mTextToSpeech!!.isSpeaking

    fun speak(text: String) {
        if (!isReady()) return
        val splitText = TtsSplitter.split(text)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) speak21(splitText)
        else speak4(splitText)
    }

    @Suppress("DEPRECATION")
    private fun speak4(text: List<String>) {
        val map = HashMap<String, String>()
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, TAG)
        text.forEach {
            if (TextUtils.isEmpty(it)) {
                mTextToSpeech?.playSilence(PAUSE_DURATION_MS, TextToSpeech.QUEUE_ADD, map)
            } else {
                mTextToSpeech?.speak(it, TextToSpeech.QUEUE_ADD, map)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun speak21(text: List<String>) {
        text.forEach {
            if (TextUtils.isEmpty(it)) {
                mTextToSpeech?.playSilentUtterance(PAUSE_DURATION_MS, TextToSpeech.QUEUE_ADD, TAG)
            } else {
                mTextToSpeech?.speak(it, TextToSpeech.QUEUE_ADD, null, TAG)
            }
        }
    }

    fun speakToFile(text: String) {
        if (!isReady()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val poemAudioExport = PoemAudioExport(context)
            poemAudioExport.speakToFile(mTextToSpeech!!, text)
        }
    }

    fun stop() {
        mTextToSpeech?.stop()
    }

    private fun shutdown() {
        mTextToSpeech?.let {
            it.setOnUtteranceProgressListener(null)
            @Suppress("DEPRECATION")
            it.setOnUtteranceCompletedListener(null)
            it.shutdown()
            mTtsStatus = TextToSpeech.ERROR
            AndroidSchedulers.mainThread().scheduleDirect { mTtsLiveData.value = TtsState(TtsState.TtsStatus.INITIALIZED, TtsState.TtsStatus.UNINITIALIZED, null) }
            mTextToSpeech = null
        }
    }

    private fun useVoiceFromSettings() = useVoice(mTextToSpeech, settingsPrefs.voice)

    private fun setVoiceSpeedFromSettings() =
            mTextToSpeech?.setSpeechRate(Math.max(MIN_VOICE_SPEED, settingsPrefs.voiceSpeed / 100f))

    private fun setVoicePitchFromSettings() =
            mTextToSpeech?.setPitch(Math.max(MIN_VOICE_PITCH, settingsPrefs.voicePitch / 100f))

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun useVoice(textToSpeech: TextToSpeech?, voiceId: String?) {
        textToSpeech?.let {
            try {
                if (voiceId == null || Settings.VOICE_SYSTEM == voiceId) {
                    it.voice = it.defaultVoice
                    Log.v(TAG, "Using default voice ${it.defaultVoice}")
                } else {
                    Observable.fromIterable(it.voices)
                            .filter({ voice ->
                                // The SDK check is here because lint currently ignores @TargetApi in nested lambdas
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && voiceId == voice.name
                            })
                            // If the user changed the tts engine in the system settings, we may not find
                            // the previous voice they selected.
                            .first(it.defaultVoice)
                            .doOnSuccess { voice -> Log.v(TAG, "using selected voice $voice") }
                            .subscribe({ voice -> it.voice = voice })
                }
            } catch (t: Throwable) {
                // This happens if I choose "SoundAbout TTS" as the preferred engine.
                // That implementation throws a NullPointerException.
                Log.w(TAG, "Couldn't load the tts voices: ${t.message}", t)
            }
            return Unit
        }
    }

    private fun isReady() = mTextToSpeech != null && mTtsStatus == TextToSpeech.SUCCESS

    private inner class TtsInitListener : TextToSpeech.OnInitListener {
        override fun onInit(status: Int) {
            Log.v(TAG, "onInit: status = $status")
            mTtsStatus = status
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                useVoiceFromSettings()
            }
            if (status == TextToSpeech.SUCCESS) {
                setVoiceSpeedFromSettings()
                setVoicePitchFromSettings()
                AndroidSchedulers.mainThread().scheduleDirect { mTtsLiveData.value = TtsState(TtsState.TtsStatus.UNINITIALIZED, TtsState.TtsStatus.INITIALIZED, null) }
                AndroidSchedulers.mainThread().scheduleDirect { mTtsLiveData.value = TtsState(TtsState.TtsStatus.INITIALIZED, TtsState.TtsStatus.INITIALIZED, null) }
            } else {
                AndroidSchedulers.mainThread().scheduleDirect { mTtsLiveData.value = TtsState(TtsState.TtsStatus.UNINITIALIZED, TtsState.TtsStatus.UNINITIALIZED, null) }
            }
        }
    }

    private inner class TtsPreferenceListener : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            if (isReady()) {
                when (key) {
                    Settings.PREF_VOICE_SPEED -> setVoiceSpeedFromSettings()
                    Settings.PREF_VOICE_PITCH -> setVoicePitchFromSettings()
                    Settings.PREF_VOICE -> useVoiceFromSettings()
                }
            }
        }
    }

    private inner class UtteranceListener : UtteranceProgressListener() {
        override fun onStart(utteranceId: String) {
            AndroidSchedulers.mainThread().scheduleDirect { mTtsLiveData.value = TtsState(TtsState.TtsStatus.INITIALIZED, TtsState.TtsStatus.SPEAKING, utteranceId) }
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

        private fun onUtteranceCompleted(utteranceId: String) {
            AndroidSchedulers.mainThread().scheduleDirect {
                mTtsLiveData.value = TtsState(TtsState.TtsStatus.SPEAKING, TtsState.TtsStatus.UTTERANCE_COMPLETE, utteranceId)
                mTtsLiveData.value = TtsState(TtsState.TtsStatus.UTTERANCE_COMPLETE, TtsState.TtsStatus.INITIALIZED, null)
            }
        }

        private fun onUtteranceError(utteranceId: String) {
            AndroidSchedulers.mainThread().scheduleDirect {
                mTtsLiveData.value = TtsState(TtsState.TtsStatus.SPEAKING, TtsState.TtsStatus.UTTERANCE_ERROR, utteranceId)
                mTtsLiveData.value = TtsState(TtsState.TtsStatus.UTTERANCE_ERROR, TtsState.TtsStatus.INITIALIZED, utteranceId)
            }
        }
    }
}
