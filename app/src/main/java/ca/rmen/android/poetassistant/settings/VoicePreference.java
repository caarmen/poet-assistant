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

package ca.rmen.android.poetassistant.settings;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;

import java.util.List;

import ca.rmen.android.poetassistant.Tts;
import ca.rmen.android.poetassistant.Voices;
import java8.util.stream.StreamSupport;

public class VoicePreference extends ListPreference {
    public VoicePreference(Context context) {
        super(context);
    }

    public VoicePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VoicePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public VoicePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void loadVoices() {
        Tts tts = Tts.getInstance(getContext());
        List<Voices.TtsVoice> voices = tts.getVoices();
        setEntryValues(StreamSupport.stream(voices)
                .map(voice -> voice.id)
                .toArray(size -> new CharSequence[voices.size()]));
        setEntries(StreamSupport.stream(voices)
                .map(voice -> voice.name)
                .toArray(size -> new CharSequence[voices.size()]));
    }
}
