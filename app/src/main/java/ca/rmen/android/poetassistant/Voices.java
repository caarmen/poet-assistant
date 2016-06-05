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
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Voices {

    private static final String TAG = Constants.TAG + Voices.class.getSimpleName();

    public static class TtsVoice {
        public final String id;
        public final String name;

        private TtsVoice(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return "TtsVoice{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    static List<TtsVoice> getVoices(TextToSpeech textToSpeech) {
        Set<Voice> voices = textToSpeech.getVoices();
        return StreamSupport.stream(voices)
                .filter(voice ->
                        !voice.isNetworkConnectionRequired()
                                && !voice.getFeatures().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
                                && voice.getName() != null
                                && voice.getLocale() != null
                                && voice.getLocale().getLanguage() != null
                                && voice.getLocale().getCountry() != null)
                .sorted(VOICE_COMPARATOR)
                .map(voice ->
                        new TtsVoice(voice.getName(), parseVoiceName(voice)))
                .collect(Collectors.toList());
    }

    static void useVoice(TextToSpeech textToSpeech, @Nullable String voiceId) {
        if (voiceId == null) return;
        Voice matchingVoice = StreamSupport.stream(textToSpeech.getVoices())
                .filter(voice -> voiceId.equals(voice.getName()))
                .findFirst()
                .get();

        if (matchingVoice != null) {
            Log.v(TAG, "using voice " + matchingVoice);
            textToSpeech.setVoice(matchingVoice);
        }
    }

    /**
     * @return a potentially readable display of the voice's name, as returned by {@link Voice#getName()}.
     * Examples:
     * fr-fr-x-vlf#female_3 => French (France) - female 3
     * en-gb-x-fis#female_3 => English (UK) - female 3
     * es-es-x-ana => Spanish (Spain)
     *
     * If the voice name doesn't match any of these patterns, the voice name itself is returned.
     */
    private static String parseVoiceName(Voice voice) {
        String voiceId = voice.getName();
        String[] tokens = voiceId.split("#");
        if (tokens.length < 1) return voiceId;

        final String gender;
        if (tokens.length == 2) gender = tokens[1];
        else gender = null;

        String language = voice.getLocale().getDisplayLanguage(Locale.getDefault());
        String country = voice.getLocale().getDisplayCountry(Locale.getDefault());
        if (gender != null) {
            return String.format("%s (%s) - %s", language, country, gender.replaceAll("_", " "));
        } else {
            return String.format("%s (%s)", language, country);
        }
    }

    /**
     * Order voices by language and country, putting the voices using the device language and country first.
     */
    private static final Comparator<Voice> VOICE_COMPARATOR = (voice1, voice2) -> {
        String lang1 = voice1.getLocale().getLanguage();
        String lang2 = voice2.getLocale().getLanguage();
        String country1 = voice1.getLocale().getCountry();
        String country2 = voice2.getLocale().getCountry();
        String deviceLanguage = Locale.getDefault().getLanguage();
        String deviceCountry = Locale.getDefault().getCountry();

        // Give priority to the device language
        if (lang1.equals(deviceLanguage) && !lang2.equals(deviceLanguage)) {
            return -1;
        }
        if (lang2.equals(deviceLanguage) && !lang1.equals(deviceLanguage)) {
            return 1;
        }

        // If both voices are using the device language, give priority to the device country.
        if (lang1.equals(lang2) && lang1.equals(deviceLanguage)) {
            if (country1.equals(deviceCountry) && !country2.equals(deviceCountry)) {
                return -1;
            }
            if (country2.equals(deviceCountry) && !country1.equals(deviceCountry)) {
                return 1;
            }
        }

        // All other cases: sort by the display name.
        String displayName1 = parseVoiceName(voice1);
        String displayName2 = parseVoiceName(voice2);
        return displayName1.compareTo(displayName2);
    };

}
