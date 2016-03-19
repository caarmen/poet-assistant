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

package ca.rmen.android.poetassistant.main.dictionaries.rt;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.util.List;

import ca.rmen.android.poetassistant.Constants;

public class RhymerLoader extends AsyncTaskLoader<List<RTEntry>> {

    private static final String TAG = Constants.TAG + RhymerLoader.class.getSimpleName();

    private final RhymerLookup mRhymerLookup;
    private List<RTEntry> mResult;

    public RhymerLoader(Context context, String query, String filter) {
        super(context);
        mRhymerLookup = new RhymerLookup(context, query, filter);
    }

    @Override
    public List<RTEntry> loadInBackground() {
        Log.d(TAG, "loadInBackground() called");
        return mRhymerLookup.lookup();
    }

    @Override
    public void deliverResult(List<RTEntry> data) {
        Log.d(TAG, "deliverResult() called");
        mResult = data;
        if (isStarted()) super.deliverResult(data);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        Log.d(TAG, "onStartLoading() called");
        if (mResult != null) super.deliverResult(mResult);
        else forceLoad();
    }

}
