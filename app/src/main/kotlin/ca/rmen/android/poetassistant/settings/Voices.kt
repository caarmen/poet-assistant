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
package ca.rmen.android.poetassistant.settings

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.text.TextUtils
import android.util.Log
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.compat.HtmlCompat
import java.util.Locale

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class Voices constructor(private val context: Context) {
    companion object {
        private val TAG = Constants.TAG + Voices::class.java.simpleName
    }

    fun getVoices(textToSpeech: TextToSpeech): List<TtsVoice> {

        val voices = try {
            textToSpeech.voices
        } catch (t: Throwable) {
            // This happens if I choose "SoundAbout TTS" as the preferred engine.
            // That implementation throws a NullPointerException.
            Log.w(TAG, "Couldn't load the tts voices: ${t.message}", t)
            return emptyList()
        }

        if (voices == null) {
            Log.w(TAG, "No voices found")
            return emptyList()
        }

        val result = voices.filter({ voice ->
            !voice.isNetworkConnectionRequired
                    && !voice.features.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
                    && voice.name != null
                    && voice.locale != null
                    && voice.locale.language != null
                    && voice.locale.country != null
        })
                .sortedWith(VoiceComparator())
                .map({ voice -> TtsVoice(voice.name, parseVoiceName(voice)) })
                .toMutableList()
        result.add(0, TtsVoice(Settings.VOICE_SYSTEM, context.getString(R.string.pref_voice_default)))
        return result
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
    private fun parseVoiceName(voice: Voice): CharSequence {
        val voiceId = voice.name
        val tokens = voiceId.split("#")
        if (tokens.isEmpty()) return voiceId

        var gender: String? = null
        if (tokens.size == 2) {
            gender = tokens[1]
            // convert "female_3" to "Female 3"
            if (!TextUtils.isEmpty(gender)) {
                gender = gender.replace("_", " ")
                gender = Character.toUpperCase(gender[0]) + gender.substring(1)
            }
        }

        val language = voice.locale.getDisplayLanguage(Locale.getDefault())
        val country = voice.locale.getDisplayCountry(Locale.getDefault())
        if (!TextUtils.isEmpty(country)) {
            // We have a country and gender
            return if (!TextUtils.isEmpty(gender)) {
                HtmlCompat.fromHtml(context.getString(R.string.pref_voice_value_with_country, language, country, gender))
            }
            // We have a country and no gender
            else {
                HtmlCompat.fromHtml(context.getString(R.string.pref_voice_value_with_country, language, country, voiceId))
            }
        }
        // We have a gender but no country
        else if (!TextUtils.isEmpty(gender)) {
            return HtmlCompat.fromHtml(context.getString(R.string.pref_voice_value_without_country, language, gender))
        }
        // We have neither gender nor country.
        else {
            return HtmlCompat.fromHtml(context.getString(R.string.pref_voice_value_without_country, language, voiceId))
        }
    }

    /**
     * Order voices by language and country, putting the voices using the device language and country first.
     */
    private inner class VoiceComparator : Comparator<Voice> {
        override fun compare(voice1: Voice, voice2: Voice): Int {
            val lang1 = voice1.locale.language
            val lang2 = voice2.locale.language
            val country1 = voice1.locale.country
            val country2 = voice2.locale.country
            val deviceLanguage = Locale.getDefault().language
            val deviceCountry = Locale.getDefault().country

            // Give priority to the device language
            if (lang1 == deviceLanguage && lang2 != deviceLanguage) {
                return -1
            }

            if (lang2 == deviceLanguage && lang1 != deviceLanguage) {
                return 1
            }

            // If both voices are using the device language, give priority to the device country.
            if (lang1 == lang2 && lang1 == deviceLanguage) {
                if (country1 == deviceCountry && country2 != deviceCountry) {
                    return -1
                }
                if (country2 == deviceCountry && country1 != deviceCountry) {
                    return 1
                }
            }

            // All other cases: sort by the display name.
            val displayName1 = parseVoiceName(voice1)
            val displayName2 = parseVoiceName(voice2)
            return displayName1.toString().compareTo(displayName2.toString())
        }
    }
}
