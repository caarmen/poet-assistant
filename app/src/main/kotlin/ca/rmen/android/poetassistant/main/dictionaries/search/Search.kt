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

package ca.rmen.android.poetassistant.main.dictionaries.search

import android.app.Activity
import android.app.SearchManager
import android.content.ContentValues
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.Threading
import ca.rmen.android.poetassistant.dagger.DaggerHelper
import ca.rmen.android.poetassistant.main.PagerAdapter
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.main.dictionaries.ResultListFragment
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary
import ca.rmen.android.poetassistant.widget.DebounceTextWatcher
import ca.rmen.android.poetassistant.widget.ViewShownScheduler
import com.google.android.material.search.SearchView
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

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
class Search constructor(private val searchableActivity: Activity, private val viewPager: ViewPager) {
    companion object {
        private val TAG = Constants.TAG + Search::class.java.simpleName
    }

    private val mPagerAdapter: PagerAdapter
    @Inject
    lateinit var mDictionary: Dictionary
    @Inject lateinit var mThreading: Threading

    init {
        DaggerHelper.getMainScreenComponent(searchableActivity.application).inject(this)
        mPagerAdapter = viewPager.adapter as PagerAdapter
    }

    fun setSearchView(searchView: SearchView, suggestionsViewModel: SuggestionsViewModel) {
        searchView.hint =
            searchableActivity.getString(R.string.search_hint) // To hopefully prevent some crashes (!!) :(
        // Step 1: Setup suggestions
        val suggestionsList: RecyclerView = searchView.findViewById(R.id.search_suggestions_list)
        val adapter = SuggestionsAdapter()
        suggestionsList.adapter = adapter
        // Step 1a: Fetch suggestions from the disk when the user stops typing.
        DebounceTextWatcher.debounce(searchView.editText, {
            val typedText = searchView.editText.text.toString()
            suggestionsViewModel.fetchSuggestions(typedText)
        })
        // Step 2a: When the suggestions list is updated, notify the recycler view adapter.
        suggestionsViewModel.viewModelScope.launch {
            suggestionsViewModel.suggestions.collect { value ->
                Log.d(TAG, "Emitted $value")
                adapter.suggestions.clear()
                adapter.suggestions.addAll(value)
                adapter.notifyDataSetChanged()
            }
        }

        // Step 2: Listen for search events:
        fun searchTermSelected(searchTerm: String) {
            searchView.hide()
            if (searchTerm.isNotBlank()) {
                addSuggestions(searchTerm)
                search(searchTerm)
            }
        }

        // Case 2a: Handle when the user taps enter from the search widget
        searchView.editText.setOnEditorActionListener { _, _, _ ->
            searchTermSelected(searchView.editText.text.toString())
            false
        }
        // Case 2b: Handle when the user clicked on a search suggestion.
        adapter.listener = { value ->
            searchTermSelected(value)
        }
    }

    /**
     * Search for the given word in the given dictionary, and set the current tab
     * to that dictionary (if it's not already the case).
     */
    fun search(word: String, tab: Tab) {
        Log.d(TAG, "search in $tab for $ word")
        viewPager.setCurrentItem(mPagerAdapter.getPositionForTab(tab), false)
        ViewShownScheduler.runWhenShown(viewPager) {
            (mPagerAdapter.getFragment(viewPager, tab) as ResultListFragment<*>?)?.query(word.trim()
                .lowercase(Locale.US))
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
                (mPagerAdapter.getFragment(viewPager, Tab.PATTERN) as ResultListFragment<*>?)?.query(wordTrimmed)
            } else {
                (mPagerAdapter.getFragment(viewPager, Tab.RHYMER) as ResultListFragment<*>?)?.query(wordTrimmed)
                (mPagerAdapter.getFragment(viewPager, Tab.THESAURUS) as ResultListFragment<*>?)?.query(wordTrimmed)
                (mPagerAdapter.getFragment(viewPager, Tab.DICTIONARY) as ResultListFragment<*>?)?.query(wordTrimmed)
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

    fun lookupRandom() {
        Log.d(TAG, "lookupRandom")
        mThreading.execute(
                { mDictionary.getRandomEntry() },
                { entry ->
                    entry?.let {
                        search(entry.word)
                        viewPager.setCurrentItem(mPagerAdapter.getPositionForTab(Tab.DICTIONARY), false)
                    }
                }
        )
    }

    /**
     * Adds the given suggestions to the search history, in a background thread.
     */
    @MainThread
    fun addSuggestions(suggestion: String) {
        mThreading.execute({
            val contentValues = ContentValues(1)
            contentValues.put(SearchManager.QUERY, suggestion)
            searchableActivity.contentResolver.insert(SuggestionsProvider.CONTENT_URI, contentValues)
        })
    }
}
