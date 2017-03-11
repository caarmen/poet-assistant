/*
 * Copyright (c) 2016-2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.dictionaries.search;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.ViewTreeObserver;

import java.util.Locale;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.dagger.DaggerHelper;
import ca.rmen.android.poetassistant.main.PagerAdapter;
import ca.rmen.android.poetassistant.main.Tab;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListFragment;
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
    private final ViewPager mViewPager;
    private final PagerAdapter mPagerAdapter;
    private final Context mContext;
    @Inject Dictionary mDictionary;

    public Search(Activity searchableActivity, ViewPager viewPager) {
        DaggerHelper.getMainScreenComponent(searchableActivity).inject(this);
        mContext = searchableActivity;
        mViewPager = viewPager;
        mPagerAdapter = (PagerAdapter) viewPager.getAdapter();
    }

    public void setSearchView(SearchView searchView) {
        SearchManager searchManager = (SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE);
        ComponentName searchableActivityComponentName = new ComponentName(mContext, mContext.getClass());
        searchView.setSearchableInfo(searchManager.getSearchableInfo(searchableActivityComponentName));
    }

    /**
     * Search for the given word in the given dictionary, and set the current tab
     * to that dictionary (if it's not already the case).
     */
    public void search(String word, Tab tab) {
        Log.d(TAG, "search() called with: " + "word = [" + word + "], tab = [" + tab + "]");
        mViewPager.setCurrentItem(mPagerAdapter.getPositionForTab(tab), false);
        word = word.trim().toLowerCase(Locale.US);
        // Not intuitive: instantiateItem will actually return an existing Fragment, whereas getItem() will always instantiate a new Fragment.
        // We want to retrieve the existing fragment.
        ((ResultListFragment<?>) mPagerAdapter.getFragment(mViewPager, tab)).query(word);
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
                ((ResultListFragment<?>) mPagerAdapter.getFragment(mViewPager, Tab.PATTERN)).query(wordTrimmed);
            } else {
                ((ResultListFragment<?>) mPagerAdapter.getFragment(mViewPager, Tab.RHYMER)).query(wordTrimmed);
                ((ResultListFragment<?>) mPagerAdapter.getFragment(mViewPager, Tab.THESAURUS)).query(wordTrimmed);
                ((ResultListFragment<?>) mPagerAdapter.getFragment(mViewPager, Tab.DICTIONARY)).query(wordTrimmed);
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
                            mViewPager.post(performSearch);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                mViewPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            } else {
                                //noinspection deprecation
                                mViewPager.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }
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
                    mPagerAdapter.setExtraTab(Tab.PATTERN);
                }
                mViewPager.setCurrentItem(mPagerAdapter.getPositionForTab(Tab.PATTERN), false);
            }
        } else {
            mPagerAdapter.setExtraTab(null);
            // If we're in the pattern tab but not searching for a pattern, go to the rhymer tab.
            if (currentTab != Tab.RHYMER && currentTab != Tab.THESAURUS && currentTab != Tab.DICTIONARY) {
                mViewPager.setCurrentItem(mPagerAdapter.getPositionForTab(Tab.RHYMER), false);
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
                DictionaryEntry entry = mDictionary.getRandomEntry();
                return entry == null ? null : entry.word;
            }

            @Override
            protected void onPostExecute(@Nullable String word) {
                if (word != null) {
                    search(word);
                    mViewPager.setCurrentItem(mPagerAdapter.getPositionForTab(Tab.DICTIONARY), false);
                }
            }
        }.execute();
    }

    /**
     * Adds the given suggestions to the search history, in a background thread.
     */
    @MainThread
    public void addSuggestions(String... suggestions) {
        new AsyncTask<String, Void, Void> () {

            @Override
            protected Void doInBackground(String... searchTerms) {
                ContentValues[] contentValues = new ContentValues[suggestions.length];
                for (int i = 0; i < suggestions.length; i++) {
                    ContentValues contentValue = new ContentValues(1);
                    contentValue.put(SearchManager.QUERY, suggestions[i]);
                    contentValues[i] = contentValue;
                }
                mContext.getContentResolver().bulkInsert(SuggestionsProvider.CONTENT_URI, contentValues);
                return null;
            }
        }.execute(suggestions);
    }

}
