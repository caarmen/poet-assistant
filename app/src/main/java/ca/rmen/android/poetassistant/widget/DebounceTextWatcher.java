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
package ca.rmen.android.poetassistant.widget;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

public final class DebounceTextWatcher {
    public static Observable<String> observe(TextView textView) {
        return Observable.create((ObservableEmitter<String> emmiter) -> {
            EmitterTextWatcher textWatcher = new EmitterTextWatcher(emmiter);
            textView.addTextChangedListener(textWatcher);
        }).debounce(5000, TimeUnit.MILLISECONDS);
    }

    private static class EmitterTextWatcher implements TextWatcher {
        private final ObservableEmitter<String> mEmitter;

        EmitterTextWatcher(ObservableEmitter<String> emitter) {
            mEmitter = emitter;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mEmitter.onNext(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

    }
}
