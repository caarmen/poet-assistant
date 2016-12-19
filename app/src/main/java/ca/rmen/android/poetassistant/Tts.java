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
import android.speech.tts.Voice;
import android.support.annotation.Nullable;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

import ca.rmen.android.poetassistant.settings.Settings;
import ca.rmen.android.poetassistant.settings.SettingsPrefs;
import java8.util.Optional;
import java8.util.stream.StreamSupport;

public class Tts {
    private static final String TAG = Constants.TAG + Tts.class.getSimpleName();

    private static final float MIN_VOICE_PITCH = 0.25f;
    private static final float MIN_VOICE_SPEED = 0.25f;

    private final Context mContext;
    private TextToSpeech mTextToSpeech;
    private int mTtsStatus = TextToSpeech.ERROR;
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

    private final TextToSpeech.OnInitListener mInitListener = status -> {
        Log.v(TAG, "onInit: status = " + status);
        mTtsStatus = status;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            useVoiceFromSettings();
        }
        if (status == TextToSpeech.SUCCESS) {
            setVoiceSpeedFromSettings();
            setVoicePitchFromSettings();
        }
        EventBus.getDefault().post(new OnTtsInitialized(status));
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
                } else if (Settings.PREF_VOICE.equals(key)) {
                    useVoiceFromSettings();
                }
            };

    private void useVoiceFromSettings() {
        useVoice(mTextToSpeech, mSettingsPrefs.getVoice());
    }

    private void setVoiceSpeedFromSettings() {
        float speed = ((float) mSettingsPrefs.getVoiceSpeed()) / 100;
        if (speed < MIN_VOICE_SPEED) speed = MIN_VOICE_SPEED;
        mTextToSpeech.setSpeechRate(speed);
    }

    private void setVoicePitchFromSettings() {
        float pitch = ((float) mSettingsPrefs.getVoicePitch()) / 100;
        if (pitch < MIN_VOICE_PITCH) pitch = MIN_VOICE_PITCH;
        mTextToSpeech.setPitch(pitch);
    }

    public TextToSpeech getTextToSpeech() {
        if (isReady()) return mTextToSpeech;
        return null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void useVoice(TextToSpeech textToSpeech, @Nullable String voiceId) {
        final Voice matchingVoice;
        try {
            if (voiceId == null || Settings.VOICE_SYSTEM.equals(voiceId)) {
                matchingVoice = textToSpeech.getDefaultVoice();
            } else {
                Optional<Voice> optionalVoice = StreamSupport.stream(textToSpeech.getVoices())
                        .filter(voice -> voiceId.equals(voice.getName()))
                        .findFirst();
                // If the user changed the tts engine in the system settings, we may not find
                // the previous voice they selected.
                if (optionalVoice.isPresent()) {
                    matchingVoice = optionalVoice.get();
                } else {
                    matchingVoice = textToSpeech.getDefaultVoice();
                }

            }
        } catch (Throwable t) {
            // This happens if I choose "SoundAbout TTS" as the preferred engine.
            // That implementation throws a NullPointerException.
            Log.w(TAG, "Couldn't load the tts voices: " + t.getMessage(), t);
            return;
        }

        if (matchingVoice != null) {
            Log.v(TAG, "using voice " + matchingVoice);
            textToSpeech.setVoice(matchingVoice);
        }
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
