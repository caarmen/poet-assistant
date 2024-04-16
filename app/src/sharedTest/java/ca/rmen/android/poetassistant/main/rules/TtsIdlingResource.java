/*
 * Copyright (c) 2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.rules;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import androidx.test.espresso.IdlingResource;

import ca.rmen.android.poetassistant.Constants;

class TtsIdlingResource implements IdlingResource {
    private static final String TAG = Constants.TAG + TtsIdlingResource.class.getSimpleName();
    private ResourceCallback mCallback;

    private int mTtsStatus;
    private final TextToSpeech mTts;

    TtsIdlingResource(Context context) {
        TextToSpeech.OnInitListener mTtsListener = status -> {
            mTtsStatus = status;
            if (mCallback != null) mCallback.onTransitionToIdle();
        };
        mTts = new TextToSpeech(context, mTtsListener);

    }

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public boolean isIdleNow() {
        boolean isSpeaking = mTtsStatus == TextToSpeech.SUCCESS && mTts.isSpeaking();
        if (!isSpeaking && mCallback != null) mCallback.onTransitionToIdle();
        return !isSpeaking;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        mCallback = callback;
    }

}
