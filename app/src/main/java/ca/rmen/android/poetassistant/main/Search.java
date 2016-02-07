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

package ca.rmen.android.poetassistant.main;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.support.v7.widget.SearchView;

import java.util.Locale;

class Search {
    private SearchView mSearchView;
    private final Suggestions.SuggestionsCursorAdapter mSuggestionsCursorAdapter;
    private final Activity mSearchableActivity;

    public Search(Activity searchableActivity) {
        mSearchableActivity = searchableActivity;
        mSuggestionsCursorAdapter = new Suggestions.SuggestionsCursorAdapter(mSearchableActivity);
    }

    public void setSearchView(SearchView searchView) {
        mSearchView = searchView;
        SearchManager searchManager = (SearchManager) mSearchableActivity.getSystemService(Context.SEARCH_SERVICE);
        ComponentName searchableActivityComponentName = new ComponentName(mSearchableActivity, mSearchableActivity.getClass());
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(searchableActivityComponentName));
        mSearchView.setOnQueryTextListener(mOnQueryTextListener);
        mSearchView.setSuggestionsAdapter(mSuggestionsCursorAdapter);
        mSearchView.setOnSuggestionListener(mOnSuggestionListener);
    }

    public void clearSearchHistory(){
        mSuggestionsCursorAdapter.clear();
    }

    private final SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            mSuggestionsCursorAdapter.addSuggestion(query.trim().toLowerCase(Locale.US));
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

    private final SearchView.OnSuggestionListener mOnSuggestionListener = new SearchView.OnSuggestionListener() {
        @Override
        public boolean onSuggestionSelect(int position) {
            String suggestion = mSuggestionsCursorAdapter.getSuggestion(position);
            mSearchView.setQuery(suggestion, false);
            return false;
        }

        @Override
        public boolean onSuggestionClick(int position) {
            String suggestion = mSuggestionsCursorAdapter.getSuggestion(position);
            mSearchView.setQuery(suggestion, false);
            return false;
        }
    };
}
