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

import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import ca.rmen.android.poetassistant.R;

class SuggestionsAdapter extends CursorAdapter {

    private SuggestionsCursor mCursor;

    public SuggestionsAdapter(Context context) {
        super(context, null, false);
        mCursor = new SuggestionsCursor(context);
        mCursor.load();
        changeCursor(mCursor);
    }

    public void clear() {
        mCursor.clear();
        reload();
    }

    private void reload() {
        mCursor = new SuggestionsCursor(mContext);
        mCursor.load();
        changeCursor(mCursor);
        notifyDataSetChanged();
    }

    public void addSuggestion(String suggestion) {
        mCursor.addSuggestion(suggestion.toLowerCase(Locale.getDefault()));
        reload();
    }

    public void filterSuggestions(String filter) {
        mCursor = new SuggestionsCursor(mContext);
        mCursor.setFilter(filter);
        mCursor.load();
        changeCursor(mCursor);
        notifyDataSetChanged();
    }

    public String getSuggestion(int position) {
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
        textView.setText(suggestion);
    }

    /**
     * SharedPreferences-backed cursor to read and add suggestions
     */
    private static class SuggestionsCursor extends MatrixCursor {
        private static final String[] COLUMNS =
                new String[]{BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1};

        private static final String PREF_SUGGESTIONS = "pref_suggestions";

        private final SharedPreferences mSharedPreferences;
        private String mFilter;

        public SuggestionsCursor(Context context) {
            super(COLUMNS);
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }

        public void setFilter(String filter) {
            mFilter = filter;
        }

        public void load() {
            Set<String> suggestions = mSharedPreferences.getStringSet(PREF_SUGGESTIONS, new TreeSet<>());
            TreeSet<String> sortedSuggestions = new TreeSet<>();
            sortedSuggestions.addAll(suggestions);
            int i = 0;
            for (String suggestion : sortedSuggestions) {
                if (TextUtils.isEmpty(mFilter) || suggestion.contains(mFilter))
                    addRow(new Object[]{i++, suggestion});
            }
        }

        public void clear() {
            mFilter = null;
            mSharedPreferences.edit().remove(PREF_SUGGESTIONS).apply();
        }

        public void addSuggestion(String suggestion) {
            Set<String> suggestionsReadOnly = mSharedPreferences.getStringSet(PREF_SUGGESTIONS, new TreeSet<>());
            if (!suggestionsReadOnly.contains(suggestion)) {
                addRow(new Object[]{getCount(), suggestion});
                TreeSet<String> suggestions = new TreeSet<>();
                suggestions.addAll(suggestionsReadOnly);
                suggestions.add(suggestion);
                mSharedPreferences.edit().putStringSet(PREF_SUGGESTIONS, suggestions).apply();
            }
        }

    }
}
