/*
 * Copyright (c) 2016 - present Carmen Alvarez
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

package ca.rmen.android.poetassistant.settings.data

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.Tts
import ca.rmen.android.poetassistant.compat.HtmlCompat
import ca.rmen.android.poetassistant.di.IODispatcher
import ca.rmen.android.poetassistant.settings.Settings
import ca.rmen.android.poetassistant.settings.TtsVoice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Voice enumeration via the TextToSpeech engine owned by [Tts].
 *
 * Obtains the engine through the [Tts.getTextToSpeech] accessor: the
 * repository never owns a TextToSpeech instance, so it lists the voices
 * of the same engine which speaks the text.
 *
 * @param context The application context.
 * @param tts The application-scoped text-to-speech service.
 * @param ioDispatcher The dispatcher for the binder call listing
 * the voices.
 */
@Singleton
class TtsVoiceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tts: Tts,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : VoiceRepository {

    companion object {
        private val TAG = Constants.TAG + TtsVoiceRepository::class.java.simpleName
    }

    override suspend fun getVoices(): List<TtsVoice> {
        val textToSpeech = tts.getTextToSpeech() ?: return emptyList()
        val voices = withContext(ioDispatcher) {
            try {
                textToSpeech.voices
            } catch (t: Throwable) {
                // This happens with engines which throw instead of
                // listing their voices (e.g. "SoundAbout TTS").
                Log.w(TAG, "Couldn't load the tts voices: ${t.message}", t)
                null
            }
        } ?: return emptyList<TtsVoice>().also { Log.w(TAG, "No voices found") }

        val systemVoice = TtsVoice(Settings.VOICE_SYSTEM, context.getString(R.string.pref_voice_default))
        val deviceVoices = voices.asSequence().filter { voice ->
            !voice.isNetworkConnectionRequired
                    && !voice.features.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
                    && voice.name != null
                    && voice.locale != null
        }
                .sortedWith(voiceComparator())
                .map { voice -> TtsVoice(voice.name, parseVoiceName(voice)) }
                .toList()
        return listOf(systemVoice) + deviceVoices
    }

    /**
     * @return a potentially readable display of the voice's name, as returned by Voice.getName().
     * Examples:
     * fr-fr-x-vlf#female_3 => French (France) - female 3
     * en-gb-x-fis#female_3 => English (UK) - female 3
     * es-es-x-ana => Spanish (Spain)
     *
     * If the voice name doesn't match any of these patterns, the voice name itself is returned.
     */
    private fun parseVoiceName(voice: Voice): CharSequence {
        val voiceId = voice.name
        val tokens = voiceId.split("#")

        // convert "female_3" to "Female 3"; the gender is only used
        // when the voice id has exactly two parts.
        val gender = if (tokens.size == 2) {
            tokens[1]
                .takeIf { it.isNotEmpty() }
                ?.replace("_", " ")
                ?.replaceFirstChar { it.uppercase() }
        } else {
            null
        }

        val language = voice.locale.getDisplayLanguage(Locale.getDefault())
        val country = voice.locale.getDisplayCountry(Locale.getDefault())
        return when {
            country.isNotEmpty() && gender != null ->
                // We have a country and a gender
                HtmlCompat.fromHtml(context.getString(R.string.pref_voice_value_with_country, language, country, gender))
            country.isNotEmpty() ->
                // We have a country and no gender
                HtmlCompat.fromHtml(context.getString(R.string.pref_voice_value_with_country, language, country, voiceId))
            gender != null ->
                // We have a gender but no country
                HtmlCompat.fromHtml(context.getString(R.string.pref_voice_value_without_country, language, gender))
            else ->
                // We have neither gender nor country.
                HtmlCompat.fromHtml(context.getString(R.string.pref_voice_value_without_country, language, voiceId))
        }
    }

    /**
     * Order voices by language and country, putting the voices using
     * the device language and country first; ties are broken by the
     * display name.
     */
    private fun voiceComparator(): Comparator<Voice> {
        val deviceLanguage = Locale.getDefault().language
        val deviceCountry = Locale.getDefault().country
        return compareBy(
            { if (it.locale.language == deviceLanguage) 0 else 1 },
            { if (it.locale.language == deviceLanguage && it.locale.country == deviceCountry) 0 else 1 },
            { parseVoiceName(it).toString() },
        )
    }
}
