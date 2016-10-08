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

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.ViewTreeObserver;

import java.util.Locale;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.main.PagerAdapter;
import ca.rmen.android.poetassistant.main.Tab;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryEntry;

/**
 * Glue between the fragments, activity, and view pager, for executing searches.
 * <p/>
 * The activity calls this class to perform searches.  This class retrieves the fragments from
 * the Viewpager, and calls the fragments (which call the adapters) to perform the search and
 * display the search results.
 * <p/>
 * This class also configures the SearchView widget, and intercepts searches to add them to
 * the list of suggested words.
 */
public class Search {
    private static final String TAG = Constants.TAG + Search.class.getSimpleName();
    private SearchView mSearchView;
    private final ViewPager mViewPager;
    private final PagerAdapter mPagerAdapter;
    private final SuggestionsAdapter mSuggestionsAdapter;
    private final Activity mSearchableActivity;

    public Search(Activity searchableActivity, ViewPager viewPager) {
        mSearchableActivity = searchableActivity;
        mViewPager = viewPager;
        mPagerAdapter = (PagerAdapter) viewPager.getAdapter();
        mSuggestionsAdapter = new SuggestionsAdapter(mSearchableActivity);
    }

    public void setSearchView(SearchView searchView) {
        mSearchView = searchView;
        SearchManager searchManager = (SearchManager) mSearchableActivity.getSystemService(Context.SEARCH_SERVICE);
        ComponentName searchableActivityComponentName = new ComponentName(mSearchableActivity, mSearchableActivity.getClass());
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(searchableActivityComponentName));
        mSearchView.setOnQueryTextListener(mOnQueryTextListener);
        mSearchView.setSuggestionsAdapter(mSuggestionsAdapter);
        mSearchView.setOnSuggestionListener(mOnSuggestionListener);
    }

    /**
     * Search for the given word in the given dictionary, and set the current tab
     * to that dictionary (if it's not already the case).
     */
    public void search(String word, Tab tab) {
        Log.d(TAG, "search() called with: " + "word = [" + word + "], tab = [" + tab + "]");
        mViewPager.setCurrentItem(mPagerAdapter.getPositionForTab(tab));
        word = word.trim().toLowerCase(Locale.US);
        // Not intuitive: instantiateItem will actually return an existing Fragment, whereas getItem() will always instantiate a new Fragment.
        // We want to retrieve the existing fragment.
        ((ResultListFragment) mPagerAdapter.getFragment(mViewPager, tab)).query(word);
    }

    /**
     * Search for the given word in all dictionaries
     */
    public void search(String word) {
        Log.d(TAG, "search() called with: " + "word = [" + word + "]");
        String wordTrimmed = word.trim().toLowerCase(Locale.US);

        selectTabForSearch(wordTrimmed);
        final Runnable performSearch = () -> {
            if (Patterns.isPattern(wordTrimmed)) {
                ((ResultListFragment) mPagerAdapter.getFragment(mViewPager, Tab.PATTERN)).query(wordTrimmed);
            } else {
                ((ResultListFragment) mPagerAdapter.getFragment(mViewPager, Tab.RHYMER)).query(wordTrimmed);
                ((ResultListFragment) mPagerAdapter.getFragment(mViewPager, Tab.THESAURUS)).query(wordTrimmed);
                ((ResultListFragment) mPagerAdapter.getFragment(mViewPager, Tab.DICTIONARY)).query(wordTrimmed);
            }
        };
        // Issue #19: In a specific scenario, the fragments may not be "ready" yet (onCreateView() may not have been called).
        // Wait until the ViewPager is laid out before invoking anything on the fragments.
        // (We assume that the fragments are "ready" once the ViewPager is laid out.)
        if (mViewPager.isShown()) {
            Log.d(TAG, "searching immediately");
            mViewPager.post(performSearch);
        } else {
            mViewPager.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            Log.d(TAG, "searching after layout");
                            performSearch.run();
                            mViewPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
            );
        }
    }

    /**
     * Navigate to the appropriate tag for the search term:
     * If it's a pattern, open the pattern tab.
     * If it's any other word:
     *  - If we're in the reader tab, go to rhymer tab
     *  - Otherwise stay in the current tab
     */
    private void selectTabForSearch(String word) {
        final boolean isPattern = Patterns.isPattern(word);
        Tab currentTab = mPagerAdapter.getTabForPosition(mViewPager.getCurrentItem());
        // If we're searching for a pattern, open the pattern tab
        if (isPattern) {
            if (currentTab != Tab.PATTERN) {
                Fragment patternTab = mPagerAdapter.getFragment(mViewPager, Tab.PATTERN);
                if (patternTab == null) {
                    mPagerAdapter.setPatternTabVisible(true);
                }
                mViewPager.setCurrentItem(mPagerAdapter.getPositionForTab(Tab.PATTERN));
            }
        } else {
            mPagerAdapter.setPatternTabVisible(false);
            // If we're in the pattern tab but not searching for a pattern, go to the rhymer tab.
            if (currentTab == Tab.PATTERN || currentTab == Tab.READER) {
                mViewPager.setCurrentItem(mPagerAdapter.getPositionForTab(Tab.RHYMER));
            }
        }

    }

    /**
     * Lookup a random word. Update the view pager tabs with the results of this word.
     */
    public void lookupRandom() {
        Log.d(TAG, "lookupRandom");
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                DictionaryEntry entry = Dictionary.getInstance(mSearchableActivity).getRandomEntry();
                return entry == null ? null : entry.word;
            }

            @Override
            protected void onPostExecute(@Nullable String word) {
                if (word != null) {
                    search(word);
                    mViewPager.setCurrentItem(mPagerAdapter.getPositionForTab(Tab.DICTIONARY));
                }
            }
        }.execute();
    }

    public void clearSearchHistory() {
        mSuggestionsAdapter.clear();
    }

    private final SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            mSuggestionsAdapter.addSuggestion(query.trim().toLowerCase(Locale.US));
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            mSuggestionsAdapter.filterSuggestions(newText);
            return false;
        }
    };

    private final SearchView.OnSuggestionListener mOnSuggestionListener = new SearchView.OnSuggestionListener() {
        @Override
        public boolean onSuggestionSelect(int position) {
            String suggestion = mSuggestionsAdapter.getSuggestion(position);
            mSearchView.setQuery(suggestion, false);
            return false;
        }

        @Override
        public boolean onSuggestionClick(int position) {
            String suggestion = mSuggestionsAdapter.getSuggestion(position);
            mSearchView.setQuery(suggestion, true);
            return true;
        }
    };
}
