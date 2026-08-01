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

package ca.rmen.android.poetassistant.main.rules

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.test.espresso.IdlingResource
import ca.rmen.android.poetassistant.Constants

class TtsIdlingResource(context: Context) : IdlingResource {

    companion object {
        private val TAG = Constants.TAG + TtsIdlingResource::class.simpleName
    }

    private var callback: IdlingResource.ResourceCallback? = null
    private var ttsStatus: Int = 0
    private val tts: TextToSpeech

    init {
        val ttsListener = TextToSpeech.OnInitListener { status ->
            ttsStatus = status
            if (callback != null) callback?.onTransitionToIdle()
        }
        tts = TextToSpeech(context, ttsListener)
    }

    override fun getName(): String {
        return TAG
    }

    override fun isIdleNow(): Boolean {
        val isSpeaking = ttsStatus == TextToSpeech.SUCCESS && tts.isSpeaking
        if (!isSpeaking && callback != null) callback?.onTransitionToIdle()
        return !isSpeaking
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
        this.callback = callback
    }
}
