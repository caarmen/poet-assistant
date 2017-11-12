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

package ca.rmen.android.poetassistant.widget

import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import java.util.concurrent.TimeUnit

object DebounceTextWatcher {
    fun observe(textView: TextView): Observable<String> {
        return Observable.create({ emitter: ObservableEmitter<String> ->
            val textWatcher = EmitterTextWatcher(emitter)
            textView.addTextChangedListener(textWatcher)
            emitter.setCancellable({ textView.removeTextChangedListener(textWatcher) })
        }).debounce(5000, TimeUnit.MILLISECONDS)
    }

    private class EmitterTextWatcher(emitter: ObservableEmitter<String>) : TextWatcher {

        private val mEmitter: ObservableEmitter<String> = emitter

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            mEmitter.onNext(s.toString())
        }

        override fun afterTextChanged(p0: Editable?) {
        }
    }
}
