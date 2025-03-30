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

package ca.rmen.android.poetassistant.wotd

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.Threading
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// Split into separate impl and base class to get full code coverage stats:
// https://medium.com/livefront/dagger-hilt-testing-injected-android-components-with-code-coverage-30089a1f6872

@AndroidEntryPoint
class WotdBroadcastReceiver : WotdBroadcastReceiverImpl()

open class WotdBroadcastReceiverImpl : BroadcastReceiver() {
    companion object {
        private val TAG = Constants.TAG + WotdBroadcastReceiver::class.java.simpleName
    }

    @Inject lateinit var dictionary: Dictionary
    @Inject lateinit var threading: Threading

    override fun onReceive(context: Context, intent: Intent) {
        Log.v(TAG, "onReceive: intent=$intent")
        threading.execute({Wotd.notifyWotd(context, dictionary)})
    }
}
