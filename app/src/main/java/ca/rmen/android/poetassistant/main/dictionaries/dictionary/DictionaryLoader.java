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

package ca.rmen.android.poetassistant.main.dictionaries.dictionary;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import ca.rmen.android.poetassistant.Constants;

class DictionaryLoader extends AsyncTaskLoader<Dictionary.DictionaryEntry[]> {

    private static final String TAG = Constants.TAG + DictionaryLoader.class.getSimpleName();

    private final String mQuery;

    public DictionaryLoader(Context context, String query) {
        super(context);
        mQuery = query;
    }

    @Override
    public Dictionary.DictionaryEntry[] loadInBackground() {
        Log.d(TAG, "loadInBackground() called with: " + "");
        Dictionary dictionary = Dictionary.getInstance(getContext());
        return dictionary.getEntries(mQuery);
    }


}
