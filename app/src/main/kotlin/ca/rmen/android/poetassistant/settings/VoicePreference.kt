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

package ca.rmen.android.poetassistant.settings

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import androidx.preference.ListPreference
import android.util.AttributeSet
import ca.rmen.android.poetassistant.Tts
import ca.rmen.android.poetassistant.dagger.DaggerHelper
import javax.inject.Inject

class VoicePreference : ListPreference {
    @Suppress("unused")
    constructor(context: Context) : super(context) {
        init()
    }

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    @Inject
    lateinit var mTts: Tts

    private fun init() {
        DaggerHelper.getSettingsComponent(context).inject(this)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    suspend fun loadVoices() {
        val tts = mTts.getTextToSpeech()
        if (tts != null) {
            val voices = Voices(context).getVoices(tts)
            entryValues = voices.map(TtsVoice::id).toTypedArray()
            entries = voices.map { voice ->voice.name}.toTypedArray()
        }
    }
}
