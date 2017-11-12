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
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.text.TextUtils;
import android.util.Log;

import java.util.Comparator;
import java.util.Locale;
import java.util.Set;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.compat.HtmlCompat;
import io.reactivex.Observable;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
final class Voices {

    private static final String TAG = Constants.TAG + Voices.class.getSimpleName();
    private final Context mContext;

    Voices(Context context) {
        mContext = context;
    }

    Observable<TtsVoice> getVoices(TextToSpeech textToSpeech) {

        Set<Voice> voices;
        try {
            voices = textToSpeech.getVoices();
        } catch (Throwable t) {
            // This happens if I choose "SoundAbout TTS" as the preferred engine.
            // That implementation throws a NullPointerException.
            Log.w(TAG, "Couldn't load the tts voices: " + t.getMessage(), t);
            return Observable.empty();
        }
        if (voices == null) {
            Log.w(TAG, "No voices found");
            return Observable.empty();
        }
        return Observable.fromIterable(voices)
                .filter(voice ->
                        !voice.isNetworkConnectionRequired()
                                && !voice.getFeatures().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
                                && voice.getName() != null
                                && voice.getLocale() != null
                                && voice.getLocale().getLanguage() != null
                                && voice.getLocale().getCountry() != null)
                .sorted(new VoiceComparator())
                .map(voice ->
                        new TtsVoice(voice.getName(), parseVoiceName(voice)))
                .startWith(new TtsVoice(Settings.VOICE_SYSTEM, mContext.getString(R.string.pref_voice_default)));
    }

    /**
     * @return a potentially readable display of the voice's name, as returned by {@link Voice#getName()}.
     * Examples:
     * fr-fr-x-vlf#female_3 => French (France) - female 3
     * en-gb-x-fis#female_3 => English (UK) - female 3
     * es-es-x-ana => Spanish (Spain)
     * <p>
     * If the voice name doesn't match any of these patterns, the voice name itself is returned.
     */
    private CharSequence parseVoiceName(Voice voice) {
        String voiceId = voice.getName();

        String[] tokens = voiceId.split("#");
        if (tokens.length < 1) return voiceId;

        String gender = null;
        if (tokens.length == 2) {
            gender = tokens[1];
            // convert "female_3" to "Female 3"
            if (!TextUtils.isEmpty(gender)) {
                gender = gender.replaceAll("_", " ");
                gender = Character.toUpperCase(gender.charAt(0)) + gender.substring(1);
            }
        }

        String language = voice.getLocale().getDisplayLanguage(Locale.getDefault());
        String country = voice.getLocale().getDisplayCountry(Locale.getDefault());
        if (!TextUtils.isEmpty(country)) {
            // We have a country and gender
            if (!TextUtils.isEmpty(gender)) {
                return HtmlCompat.INSTANCE.fromHtml(mContext.getString(R.string.pref_voice_value_with_country, language, country, gender));
            }
            // We have a country and no gender
            else {
                return HtmlCompat.INSTANCE.fromHtml(mContext.getString(R.string.pref_voice_value_with_country, language, country, voiceId));
            }
        }
        // We have a gender but no country
        else if (!TextUtils.isEmpty(gender)){
            return HtmlCompat.INSTANCE.fromHtml(mContext.getString(R.string.pref_voice_value_without_country, language, gender));
        }
        // We have neither gender nor country.
        else {
            return HtmlCompat.INSTANCE.fromHtml(mContext.getString(R.string.pref_voice_value_without_country, language, voiceId));
        }
    }

    /**
     * Order voices by language and country, putting the voices using the device language and country first.
     */
    private class VoiceComparator implements Comparator<Voice> {

        @Override
        public int compare(Voice voice1, Voice voice2) {
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
            CharSequence displayName1 = parseVoiceName(voice1);
            CharSequence displayName2 = parseVoiceName(voice2);
            return displayName1.toString().compareTo(displayName2.toString());
        }

    }

}
