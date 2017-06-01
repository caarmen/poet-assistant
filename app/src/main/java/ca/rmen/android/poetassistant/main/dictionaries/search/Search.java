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
import android.support.annotation.MainThread;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.util.Log;

import java.util.Locale;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.dagger.DaggerHelper;
import ca.rmen.android.poetassistant.main.PagerAdapter;
import ca.rmen.android.poetassistant.main.Tab;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListFragment;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary;
import ca.rmen.android.poetassistant.widget.ViewShownCompletable;
import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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
        DaggerHelper.getMainScreenComponent(searchableActivity.getApplication()).inject(this);
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
        ViewShownCompletable.create(mViewPager).subscribe(() -> ((ResultListFragment<?>) mPagerAdapter.getFragment(mViewPager, tab)).query(word.trim().toLowerCase(Locale.US)));
    }

    /**
     * Search for the given word in all dictionaries
     */
    public void search(String word) {
        Log.d(TAG, "search() called with: " + "word = [" + word + "]");
        String wordTrimmed = word.trim().toLowerCase(Locale.US);

        selectTabForSearch(wordTrimmed);
        ViewShownCompletable.create(mViewPager)
                .subscribe(() -> {
                    if (Patterns.isPattern(wordTrimmed)) {
                        ((ResultListFragment<?>) mPagerAdapter.getFragment(mViewPager, Tab.PATTERN)).query(wordTrimmed);
                    } else {
                        ((ResultListFragment<?>) mPagerAdapter.getFragment(mViewPager, Tab.RHYMER)).query(wordTrimmed);
                        ((ResultListFragment<?>) mPagerAdapter.getFragment(mViewPager, Tab.THESAURUS)).query(wordTrimmed);
                        ((ResultListFragment<?>) mPagerAdapter.getFragment(mViewPager, Tab.DICTIONARY)).query(wordTrimmed);
                    }
                });
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

        Maybe.fromCallable(() -> mDictionary.getRandomEntry())
                .map(entry -> entry.word)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(word -> {
                    search(word);
                    mViewPager.setCurrentItem(mPagerAdapter.getPositionForTab(Tab.DICTIONARY), false);
                });
    }

    /**
     * Adds the given suggestions to the search history, in a background thread.
     */
    @MainThread
    public void addSuggestions(String suggestion) {
        Schedulers.io().scheduleDirect(()->{
            ContentValues contentValues = new ContentValues(1);
            contentValues.put(SearchManager.QUERY, suggestion);
            mContext.getContentResolver().insert(SuggestionsProvider.CONTENT_URI, contentValues);
        });
    }

}
