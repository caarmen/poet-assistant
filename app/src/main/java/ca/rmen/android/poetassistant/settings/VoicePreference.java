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

package ca.rmen.android.poetassistant.settings;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.dagger.DaggerHelper;
import ca.rmen.android.poetassistant.Tts;
import io.reactivex.Observable;

public class VoicePreference extends ListPreference {
    @Inject Tts mTts;

    @SuppressWarnings("unused")
    public VoicePreference(Context context) {
        super(context);
        init();
    }

    @SuppressWarnings("unused")
    public VoicePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @SuppressWarnings("unused")
    public VoicePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressWarnings("unused")
    public VoicePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        DaggerHelper.INSTANCE.getSettingsComponent(getContext()).inject(this);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void loadVoices(Context context) {
        TextToSpeech tts = mTts.getTextToSpeech();
        if (tts != null) {
            Observable<TtsVoice> voices = new Voices(context).getVoices(tts);
            voices.map(voice -> voice.id)
                    .toList()
                    .subscribe(voiceIds -> setEntryValues(voiceIds.toArray(new CharSequence[voiceIds.size()])));
            voices.map(voice -> voice.name)
                    .toList()
                    .subscribe(voiceNames -> setEntries(voiceNames.toArray(new CharSequence[voiceNames.size()])));
        }
    }
}
