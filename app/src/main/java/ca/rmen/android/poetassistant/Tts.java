/*
 * Copyright (c) 2016 Carmen Alvarez
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

package ca.rmen.android.poetassistant;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import ca.rmen.android.poetassistant.settings.Settings;
import ca.rmen.android.poetassistant.settings.SettingsPrefs;

public class Tts {
    private static final String TAG = Constants.TAG + Tts.class.getSimpleName();

    private static final float MIN_VOICE_PITCH = 0.25f;
    private static final float MIN_VOICE_SPEED = 0.25f;

    private final Context mContext;
    private TextToSpeech mTextToSpeech;
    private int mTtsStatus = TextToSpeech.ERROR;
    private final Voices mVoices;
    private final SettingsPrefs mSettingsPrefs;

    public static class OnTtsInitialized {
        public final int status;

        private OnTtsInitialized(int status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return "OnTtsInitialized{" +
                    "status=" + status +
                    '}';
        }
    }

    public static class OnUtteranceCompleted {
    }

    public Tts(Context context, SettingsPrefs settingsPrefs) {
        mContext = context;
        mVoices = new Voices(context);
        mSettingsPrefs = settingsPrefs;
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(mTtsPrefsListener);
        init();
    }

    private void init() {
        Log.v(TAG, "init");
        mTextToSpeech = new TextToSpeech(mContext, mInitListener);
        mTextToSpeech.setOnUtteranceProgressListener(mUtteranceListener);
        //noinspection deprecation
        mTextToSpeech.setOnUtteranceCompletedListener(mUtteranceListener);
    }

    /**
     * Force a reinitialization of the TextToSpeech.
     * One use case: if the user changed the default engine, a restart is required to get the new list of voices.
     */
    public void restart() {
        Log.v(TAG, "restart");
        shutdown();
        init();
    }

    public int getStatus() {
        return mTtsStatus;
    }

    public boolean isSpeaking() {
        return isReady() && mTextToSpeech.isSpeaking();
    }

    public void speak(String text) {
        if (!isReady()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            speak21(text);
        else
            speak4(text);
    }

    @SuppressWarnings("deprecation")
    private void speak4(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, TAG);
        mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void speak21(String text) {
        mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, TAG);
    }

    public void stop() {
        if (mTextToSpeech != null) mTextToSpeech.stop();
    }

    private void shutdown() {
        if (mTextToSpeech != null) {
            mTextToSpeech.setOnUtteranceProgressListener(null);
            //noinspection deprecation
            mTextToSpeech.setOnUtteranceCompletedListener(null);
            mTextToSpeech.shutdown();
            mTtsStatus = TextToSpeech.ERROR;
            mTextToSpeech = null;
        }
    }

    private final TextToSpeech.OnInitListener mInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            Log.v(TAG, "onInit: status = " + status);
            mTtsStatus = status;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mVoices.useVoice(mTextToSpeech, mSettingsPrefs.getVoice());
            }
            if (status == TextToSpeech.SUCCESS) {
                setVoiceSpeedFromSettings();
                setVoicePitchFromSettings();
            }
            EventBus.getDefault().post(new OnTtsInitialized(status));
        }
    };

    // This can't be local or it will be removed from the shared prefs manager!
    @SuppressWarnings("FieldCanBeLocal")
    private final SharedPreferences.OnSharedPreferenceChangeListener mTtsPrefsListener
            = (sharedPreferences, key) -> {
                if (!isReady()) return;
                if (Settings.PREF_VOICE_SPEED.equals(key)) {
                    setVoiceSpeedFromSettings();
                } else if (Settings.PREF_VOICE_PITCH.equals(key)) {
                    setVoicePitchFromSettings();
                }
            };

    private void setVoiceSpeedFromSettings() {
        float speed = mSettingsPrefs.getVoiceSpeed() / 100;
        if (speed < MIN_VOICE_SPEED) speed = MIN_VOICE_SPEED;
        mTextToSpeech.setSpeechRate(speed);
    }

    private void setVoicePitchFromSettings() {
        float pitch = mSettingsPrefs.getVoicePitch() / 100;
        if (pitch < MIN_VOICE_PITCH) pitch = MIN_VOICE_PITCH;
        mTextToSpeech.setPitch(pitch);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public List<Voices.TtsVoice> getVoices() {
        if (!isReady()) return Collections.emptyList();
        return mVoices.getVoices(mTextToSpeech);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void useVoice(String voiceId) {
        if (!isReady()) return;
        mVoices.useVoice(mTextToSpeech, voiceId);
    }

    private boolean isReady() {
        return mTextToSpeech != null && mTtsStatus == TextToSpeech.SUCCESS;
    }

    @SuppressWarnings("deprecation")
    private static class UtteranceListener extends UtteranceProgressListener
            implements TextToSpeech.OnUtteranceCompletedListener {

        private void onUtteranceCompleted() {
            EventBus.getDefault().post(new OnUtteranceCompleted());
        }

        @Override
        public void onStart(String utteranceId) {
            onUtteranceCompleted();
        }

        @Override
        public void onDone(String utteranceId) {
            onUtteranceCompleted();
        }

        @Override
        public void onError(String utteranceId) {
            onUtteranceCompleted();
        }

        @Override
        public void onError(String utteranceId, int errorCode) {
            super.onError(utteranceId, errorCode);
            onUtteranceCompleted();
        }

        @Override
        public void onStop(String utteranceId, boolean interrupted) {
            super.onStop(utteranceId, interrupted);
            onUtteranceCompleted();
        }

        @Override
        public void onUtteranceCompleted(String utteranceId) {
            onUtteranceCompleted();
        }
    }

    private final UtteranceListener mUtteranceListener = new UtteranceListener();
}
