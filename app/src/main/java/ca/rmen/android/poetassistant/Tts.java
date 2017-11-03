/*
 * Copyright (c) 2016-2017 Carmen Alvarez
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
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import ca.rmen.android.poetassistant.settings.Settings;
import ca.rmen.android.poetassistant.settings.SettingsPrefs;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class Tts {
    private static final String TAG = Constants.TAG + Tts.class.getSimpleName();

    private static final int PAUSE_DURATION_MS = 500;
    private static final float MIN_VOICE_PITCH = 0.25f;
    private static final float MIN_VOICE_SPEED = 0.25f;

    private final Context mContext;
    private TextToSpeech mTextToSpeech;
    private int mTtsStatus = TextToSpeech.ERROR;
    private final SettingsPrefs mSettingsPrefs;

    public class TtsState {
        public final TtsStatus previousStatus;
        public final TtsStatus currentStatus;
        public final String utteranceId;

        public TtsState(TtsStatus previousStatus, TtsStatus currentStatus, String utteranceId) {
            this.previousStatus = previousStatus;
            this.currentStatus = currentStatus;
            this.utteranceId = utteranceId;
        }

        @Override
        public String toString() {
            return "TtsState{" +
                    "previousStatus=" + previousStatus +
                    ", currentStatus=" + currentStatus +
                    ", utteranceId='" + utteranceId + '\'' +
                    '}';
        }
    }

    public enum TtsStatus {
        UNINITIALIZED,
        INITIALIZED,
        SPEAKING,
        UTTERANCE_COMPLETE,
        UTTERANCE_ERROR
    }

    private final MutableLiveData<TtsState> mTtsLiveData = new MutableLiveData<>();
    public LiveData<TtsState> getTtsLiveData() {
        return mTtsLiveData;
    }

    public Tts(Context context, SettingsPrefs settingsPrefs) {
        mContext = context;
        mSettingsPrefs = settingsPrefs;
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(mTtsPrefsListener);
        init();
    }

    private void init() {
        Log.v(TAG, "init");
        mTtsLiveData.setValue(new TtsState(null, TtsStatus.UNINITIALIZED, null));
        mTextToSpeech = new TextToSpeech(mContext, mInitListener);
        mTextToSpeech.setOnUtteranceProgressListener(mUtteranceListener);
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

    public TtsState getTtsState() {
        return mTtsLiveData.getValue();
    }

    public boolean isSpeaking() {
        return isReady() && mTextToSpeech.isSpeaking();
    }

    public void speak(String text) {
        if (!isReady()) return;
        List<String> splitText = split(text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            speak21(splitText);
        else
            speak4(splitText);
    }

    @SuppressWarnings("deprecation")
    private void speak4(List<String> text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, TAG);
        for (String subtext : text) {
            if (TextUtils.isEmpty(subtext)) {
                mTextToSpeech.playSilence(PAUSE_DURATION_MS, TextToSpeech.QUEUE_ADD, map);
            } else {
                mTextToSpeech.speak(subtext, TextToSpeech.QUEUE_ADD, map);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void speak21(List<String> text) {
        for (String subtext : text) {
            if (TextUtils.isEmpty(subtext)) {
                mTextToSpeech.playSilentUtterance(PAUSE_DURATION_MS, TextToSpeech.QUEUE_ADD, TAG);
            } else {
                mTextToSpeech.speak(subtext, TextToSpeech.QUEUE_ADD, null, TAG);
            }
        }
    }

    public void speakToFile(String text) {
        if (!isReady()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            PoemAudioExport poemAudioExport = new PoemAudioExport(mContext);
            poemAudioExport.speakToFile(mTextToSpeech, text);
        }
    }

    public void stop() {
        if (mTextToSpeech != null) mTextToSpeech.stop();
    }

    /**
     * Splits a string into multiple tokens for pausing playback.
     *
     * @param text A "..." in the input text indicates a pause, and each subsequent "." after the initial "..." indicates an additional pause.
     *
     * Examples:
     * "To be or not to be... that is the question":  1 pause:  "To be or not to be", "",     " that is the question"
     * "To be or not to be.... that is the question": 2 pauses: "To be or not to be", "", "", " that is the question"
     * "To be or not to be. that is the question":    0 pauses: "To be or not to be. that is the question"
     *
     * @return the input split into multiple tokens. An empty-string token in the result indicates a pause.
     */
    @VisibleForTesting
    static List<String> split(String text) {
        List<String> tokens = new ArrayList<>();
        StringTokenizer stringTokenizer = new StringTokenizer(text, ".", true);
        // In a sequence of dots, we want to skip the first two.
        int skippedDots = 0;
        String prevToken = null;
        while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken();
            // The current token is a dot. It may or may not be used to pause.
            if (".".equals(token)) {
                // We've skipped at least two consecutive dots. We can now start adding all dots as
                // pause tokens.
                if (skippedDots == 2) {
                    String pauseToken = "";
                    tokens.add(pauseToken);
                    prevToken = pauseToken;
                }
                // Beginning of a dot sequence. We have to skip the first two dots.
                else {
                    skippedDots++;
                }
            }
            // The current token is actual text to speak.
            else {
                final String textToken;
                // This is either the first text token of the entire input, or a text token after a pause token.
                // We simply add it to the list.
                if (prevToken == null || "".equals(prevToken)){
                    textToken = token;
                    tokens.add(textToken);
                }
                // The previous token was also actual text.
                // Concatenate the previous token with this one, separating by a single period.
                // This optimization allows us to minimize the number of tokens we'll return, and to rely
                // on the sentence pausing of the TTS engine when less than 3 dots separate two sentences.
                else /* prevToken != null && prevToken != "" */{
                    textToken = prevToken + "." + token;
                    tokens.set(tokens.size() - 1, textToken);
                }
                prevToken = textToken;
                skippedDots = 0;
            }
        }
        return tokens;
    }

    private void shutdown() {
        if (mTextToSpeech != null) {
            mTextToSpeech.setOnUtteranceProgressListener(null);
            //noinspection deprecation
            mTextToSpeech.setOnUtteranceCompletedListener(null);
            mTextToSpeech.shutdown();
            mTtsStatus = TextToSpeech.ERROR;
            AndroidSchedulers.mainThread().scheduleDirect(() -> mTtsLiveData.setValue(new TtsState(TtsStatus.INITIALIZED, TtsStatus.UNINITIALIZED, null)));
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
            AndroidSchedulers.mainThread().scheduleDirect(() -> mTtsLiveData.setValue(new TtsState(TtsStatus.UNINITIALIZED, TtsStatus.INITIALIZED, null)));
            AndroidSchedulers.mainThread().scheduleDirect(() -> mTtsLiveData.setValue(new TtsState(TtsStatus.INITIALIZED, TtsStatus.INITIALIZED, null)));
        } else {
            AndroidSchedulers.mainThread().scheduleDirect(() -> mTtsLiveData.setValue(new TtsState(TtsStatus.UNINITIALIZED, TtsStatus.UNINITIALIZED, null)));
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
        try {
            if (voiceId == null || Settings.VOICE_SYSTEM.equals(voiceId)) {
                textToSpeech.setVoice(textToSpeech.getDefaultVoice());
                Log.v(TAG, "Using default voice " + textToSpeech.getDefaultVoice());
            } else {
                Observable.fromIterable(textToSpeech.getVoices())
                        .filter(voice ->
                                // The SDK check is here because lint currently ignores @TargetApi in nested lambdas
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && voiceId.equals(voice.getName()))
                        // If the user changed the tts engine in the system settings, we may not find
                        // the previous voice they selected.
                        .first(textToSpeech.getDefaultVoice())
                        .doOnSuccess(voice -> Log.v(TAG, "using selected voice " + voice))
                        .subscribe(textToSpeech::setVoice);
            }
        } catch (Throwable t) {
            // This happens if I choose "SoundAbout TTS" as the preferred engine.
            // That implementation throws a NullPointerException.
            Log.w(TAG, "Couldn't load the tts voices: " + t.getMessage(), t);
        }
    }


    private boolean isReady() {
        return mTextToSpeech != null && mTtsStatus == TextToSpeech.SUCCESS;
    }

    @SuppressWarnings("deprecation")
    private class UtteranceListener extends UtteranceProgressListener {

        @Override
        public void onStart(String utteranceId) {
            AndroidSchedulers.mainThread().scheduleDirect(() -> mTtsLiveData.setValue(new TtsState(TtsStatus.INITIALIZED, TtsStatus.SPEAKING, utteranceId)));
        }

        @Override
        public void onDone(String utteranceId) {
            onUtteranceCompleted(utteranceId);
        }

        @Override
        public void onError(String utteranceId) {
            onUtteranceError(utteranceId);
        }

        @Override
        public void onError(String utteranceId, int errorCode) {
            super.onError(utteranceId, errorCode);
            onUtteranceError(utteranceId);
        }

        @Override
        public void onStop(String utteranceId, boolean interrupted) {
            super.onStop(utteranceId, interrupted);
            onUtteranceCompleted(utteranceId);
        }

        private void onUtteranceCompleted(String utteranceId) {
            AndroidSchedulers.mainThread().scheduleDirect(() -> {
                mTtsLiveData.setValue(new TtsState(TtsStatus.SPEAKING, TtsStatus.UTTERANCE_COMPLETE, utteranceId));
                mTtsLiveData.setValue(new TtsState(TtsStatus.UTTERANCE_COMPLETE, TtsStatus.INITIALIZED, null));
            });
        }

        private void onUtteranceError(String utteranceId) {
            AndroidSchedulers.mainThread().scheduleDirect(() -> {
                mTtsLiveData.setValue(new TtsState(TtsStatus.SPEAKING, TtsStatus.UTTERANCE_ERROR, utteranceId));
                mTtsLiveData.setValue(new TtsState(TtsStatus.UTTERANCE_ERROR, TtsStatus.INITIALIZED, null));
            });
        }
    }

    private final UtteranceListener mUtteranceListener = new UtteranceListener();
}
