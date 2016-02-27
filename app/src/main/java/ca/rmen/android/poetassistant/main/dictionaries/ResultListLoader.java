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

package ca.rmen.android.poetassistant.main.dictionaries;

import android.content.Context;
import android.text.TextUtils;

import java.util.Collections;

import rx.Observable;

public abstract class ResultListLoader<T> {

    private final Context mContext;

    protected ResultListLoader(Context context) {
        mContext = context.getApplicationContext();
    }

    public Observable<T> observeEntries(String query, String filter) {
        if (TextUtils.isEmpty(query)) {
            //noinspection unchecked
            return Observable.just((T) Collections.emptyList());
        } else {
            return Observable.defer(() -> getEntries(query, filter));
        }
    }

    protected Context getContext() {
        return mContext;
    }

    protected abstract Observable<T> getEntries(String query, String filter);

}
