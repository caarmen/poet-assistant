/*
 * Copyright (c) 2016-present Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.dictionaries.search

import android.util.Log
import androidx.viewpager.widget.ViewPager
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.main.PagerAdapter
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryFragment
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.usecases.GetRandomWordUseCase
import ca.rmen.android.poetassistant.main.dictionaries.patterns.ui.PatternFragment
import ca.rmen.android.poetassistant.main.dictionaries.rhymes.RhymerFragment
import ca.rmen.android.poetassistant.main.dictionaries.search.suggestions.data.SuggestionsRepository
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ThesaurusFragment
import ca.rmen.android.poetassistant.widget.ViewShownScheduler
import java.util.Locale

/**
 * Glue between the fragments, activity, and view pager, for executing searches.
 * <p/>
 * The activity calls this class to perform searches.  This class retrieves the fragments from
 * the Viewpager, and calls the fragments (which call the adapters) to perform the search and
 * display the search results.
 * <p/>
 */
class Search(
    private val viewPager: ViewPager,
    private val getRandomWordUseCase: GetRandomWordUseCase,
    private val suggestionsRepository: SuggestionsRepository,
    ) {
    companion object {
        private val TAG = Constants.TAG + Search::class.java.simpleName
    }

    private val mPagerAdapter: PagerAdapter

    init {
        mPagerAdapter = viewPager.adapter as PagerAdapter
    }

    /**
     * Search for the given word in the given dictionary, and set the current tab
     * to that dictionary (if it's not already the case).
     */
    fun search(word: String, tab: Tab) {
        Log.d(TAG, "search in $tab for $ word")
        viewPager.setCurrentItem(mPagerAdapter.getPositionForTab(tab), false)
        ViewShownScheduler.runWhenShown(viewPager) {
            if (tab == Tab.DICTIONARY) {
                (mPagerAdapter.getFragment(viewPager, Tab.DICTIONARY) as DictionaryFragment?)?.query(word.trim())
            } else if (tab == Tab.THESAURUS) {
                (mPagerAdapter.getFragment(viewPager, Tab.THESAURUS) as ThesaurusFragment?)?.query(
                    word.trim()
                        .lowercase(Locale.US)
                )
            } else if (tab == Tab.RHYMER) {
                (mPagerAdapter.getFragment(viewPager, Tab.RHYMER) as RhymerFragment?)?.query(
                    word.trim()
                        .lowercase(Locale.US)
                )
            }
        }
    }

    /**
     * Search for the given word in all dictionaries
     */
    fun search(word: String) {
        Log.d(TAG, "search called with $word")
        val wordTrimmed = word.trim().lowercase(Locale.US)

        selectTabForSearch(wordTrimmed)
        ViewShownScheduler.runWhenShown(viewPager) {
            if (Patterns.isPattern(wordTrimmed)) {
                (mPagerAdapter.getFragment(viewPager, Tab.PATTERN) as PatternFragment?)?.query(wordTrimmed)
            } else {
                (mPagerAdapter.getFragment(viewPager, Tab.RHYMER) as RhymerFragment?)?.query(wordTrimmed)
                (mPagerAdapter.getFragment(viewPager, Tab.THESAURUS) as ThesaurusFragment?)?.query(wordTrimmed)
                (mPagerAdapter.getFragment(viewPager, Tab.DICTIONARY) as DictionaryFragment?)?.query(wordTrimmed)
            }
        }
    }

    /**
     * Navigate to the appropriate tab for the search term:
     * If it's a pattern, open the pattern tab.
     * If it's any other word:
     *  - If we're in the reader tab, go to rhymer tab
     *  - Otherwise stay in the current tab
     */
    private fun selectTabForSearch(word: String) {
        val isPattern = Patterns.isPattern(word)
        val currentTab = mPagerAdapter.getTabForPosition(viewPager.currentItem)
        // If we're searching for a pattern, open the pattern tab
        if (isPattern) {
            if (currentTab != Tab.PATTERN) {
                val patternTab = mPagerAdapter.getFragment(viewPager, Tab.PATTERN)
                if (patternTab == null) {
                    mPagerAdapter.setExtraTab(Tab.PATTERN)
                }
                viewPager.setCurrentItem(mPagerAdapter.getPositionForTab(Tab.PATTERN), false)
            }
        } else {
            mPagerAdapter.setExtraTab(null)
            // If we're in the pattern tab but not searching for a pattern, go to the rhymer tab.
            if (currentTab != Tab.RHYMER && currentTab != Tab.THESAURUS && currentTab != Tab.DICTIONARY) {
                viewPager.setCurrentItem(mPagerAdapter.getPositionForTab(Tab.RHYMER), false)
            }
        }
    }

    /**
     * Lookup a random word. Update the view pager tabs with the results of this word.
     */

    suspend fun lookupRandom() {
        Log.d(TAG, "lookupRandom")
        getRandomWordUseCase()?.let { word ->
            search(word)
            viewPager.setCurrentItem(mPagerAdapter.getPositionForTab(Tab.DICTIONARY), false)
        }
    }

    /**
     * Adds the given suggestions to the search history, in a background thread.
     */
    suspend fun addSuggestions(suggestion: String) {
        suggestionsRepository.addSuggestion(suggestion)
    }
}
