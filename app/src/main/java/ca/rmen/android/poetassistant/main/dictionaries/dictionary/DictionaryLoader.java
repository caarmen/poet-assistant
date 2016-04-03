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
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListData;

public class DictionaryLoader extends AsyncTaskLoader<ResultListData<DictionaryEntry.DictionaryEntryDetails>> {

    private static final String TAG = Constants.TAG + DictionaryLoader.class.getSimpleName();

    private final String mQuery;
    private ResultListData<DictionaryEntry.DictionaryEntryDetails> mResult;

    public DictionaryLoader(Context context, String query) {
        super(context);
        mQuery = query;
    }

    @Override
    public ResultListData<DictionaryEntry.DictionaryEntryDetails> loadInBackground() {
        Log.d(TAG, "loadInBackground() called with: " + "");
        List<DictionaryEntry.DictionaryEntryDetails> result = new ArrayList<>();
        if(TextUtils.isEmpty(mQuery)) return new ResultListData<>(mQuery, result);
        Dictionary dictionary = Dictionary.getInstance(getContext());
        DictionaryEntry entry = dictionary.lookup(mQuery);
        Collections.addAll(result, entry.details);
        return new ResultListData<>(entry.word, result);
    }

    @Override
    public void deliverResult(ResultListData<DictionaryEntry.DictionaryEntryDetails> data) {
        Log.d(TAG, "deliverResult() called with: query = " + mQuery + ", data = [" + data + "]");
        mResult = data;
        if (isStarted()) super.deliverResult(data);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        Log.d(TAG, "onStartLoading() called with: query = " + mQuery);
        if (mResult != null) super.deliverResult(mResult);
        else forceLoad();
    }

}
