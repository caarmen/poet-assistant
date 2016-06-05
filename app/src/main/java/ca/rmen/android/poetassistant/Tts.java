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
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;

import ca.rmen.android.poetassistant.settings.SettingsPrefs;

public class Tts {
    private static final String TAG = Constants.TAG + Tts.class.getSimpleName();

    private final Context mContext;
    private TextToSpeech mTextToSpeech;
    private int mTtsStatus = TextToSpeech.ERROR;
    private final Voices mVoices;
    private static Tts sInstance;

    public static class OnTtsInitialized {
        public final int status;

        public OnTtsInitialized(int status) {
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

    public synchronized static Tts getInstance(Context context) {
        if (sInstance == null) sInstance = new Tts(context);
        return sInstance;
    }

    private Tts(Context context) {
        mContext = context.getApplicationContext();
        mTextToSpeech = new TextToSpeech(context.getApplicationContext(), new OnInitListener());
        UtteranceListener utteranceListener = new UtteranceListener();
        mTextToSpeech.setOnUtteranceProgressListener(utteranceListener);
        //noinspection deprecation
        mTextToSpeech.setOnUtteranceCompletedListener(utteranceListener);
        mVoices = new Voices(mContext);
    }

    public int getStatus() {
        return mTtsStatus;
    }

    public boolean isSpeaking() {
        return mTextToSpeech != null && mTextToSpeech.isSpeaking();
    }

    public void speak(String text) {
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

    public void shutdown() {
        if (mTextToSpeech != null) {
            mTextToSpeech.shutdown();
            mTextToSpeech = null;
        }
    }

    private class OnInitListener implements TextToSpeech.OnInitListener {
        @Override
        public void onInit(int status) {
            mTtsStatus = status;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mVoices.useVoice(mTextToSpeech, SettingsPrefs.get(mContext).getVoice());
            }
            EventBus.getDefault().post(new OnTtsInitialized(status));
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public List<Voices.TtsVoice> getVoices() {
        return mVoices.getVoices(mTextToSpeech);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void useVoice(String voiceId) {
        mVoices.useVoice(mTextToSpeech, voiceId);
    }

    @SuppressWarnings("deprecation")
    public static class UtteranceListener extends UtteranceProgressListener
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
}
