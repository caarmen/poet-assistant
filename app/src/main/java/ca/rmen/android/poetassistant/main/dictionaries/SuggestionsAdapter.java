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
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.VectorCompat;

class SuggestionsAdapter extends CursorAdapter {

    private SuggestionsCursor mCursor;

    // We use mHandler and mIsLoading to prevent loading suggestions in parallel AsyncTasks.
    private boolean mIsLoading;
    private final Handler mHandler;

    SuggestionsAdapter(Context context) {
        super(context, null, false);
        mHandler = new Handler();
        mCursor = new SuggestionsCursor(context);
        mCursor.load();
        changeCursor(mCursor);
    }

    void clear() {
        mCursor.clear();
        filterSuggestions(null);
    }

    void addSuggestion(String suggestion) {
        mCursor.saveNewSuggestion(suggestion.toLowerCase(Locale.getDefault()));
        filterSuggestions(null);
    }

    void filterSuggestions(String filter) {
        if (mIsLoading) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler.postDelayed(() -> new SuggestionsLoader().execute(filter), TimeUnit.SECONDS.toMillis(1));
        } else {
            new SuggestionsLoader().execute(filter);
        }
    }

    String getSuggestion(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getString(1);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return View.inflate(context, R.layout.suggested_word, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textView = (TextView) view;
        String suggestion = cursor.getString(1);
        @DrawableRes int iconRes = cursor.getInt(2);
        textView.setText(suggestion);
        VectorCompat.setCompoundVectorDrawables(mContext, textView, iconRes, 0, 0, 0);
    }

    private class SuggestionsLoader extends AsyncTask<String, Void, SuggestionsCursor> {
        @Override
        protected void onPreExecute() {
            mIsLoading = true;
        }

        @Override
        protected SuggestionsCursor doInBackground(String... filter) {
            SuggestionsCursor cursor = new SuggestionsCursor(mContext);
            cursor.setFilter(filter[0]);
            cursor.load();
            return cursor;
        }

        @Override
        protected void onPostExecute(SuggestionsCursor cursor) {
            mCursor = cursor;
            changeCursor(cursor);
            notifyDataSetChanged();
            mIsLoading = false;
        }
    }
}
