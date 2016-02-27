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
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListData;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListLoader;
import rx.Observable;

public class DictionaryLoader extends ResultListLoader<ResultListData<DictionaryEntry.DictionaryEntryDetails>> {

    private static final String TAG = Constants.TAG + DictionaryLoader.class.getSimpleName();

    public DictionaryLoader(Context context) {
        super(context);
    }

    @Override
    protected Observable<ResultListData<DictionaryEntry.DictionaryEntryDetails>> getEntries(String query, String filter) {
        Log.d(TAG, "getEntries() called with: " + "query = [" + query + "], filter = [" + filter + "]");

            List<DictionaryEntry.DictionaryEntryDetails> result = new ArrayList<>();
            if(TextUtils.isEmpty(query)) return Observable.just(new ResultListData<>(query, result));
            Dictionary dictionary = Dictionary.getInstance(getContext());
            DictionaryEntry entry = dictionary.lookup(query);
            Collections.addAll(result, entry.details);
            return Observable.just(new ResultListData<>(entry.word, result));
    }

}
