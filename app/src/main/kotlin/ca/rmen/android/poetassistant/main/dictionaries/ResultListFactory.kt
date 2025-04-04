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

package ca.rmen.android.poetassistant.main.dictionaries

import android.app.Activity
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.View
import androidx.annotation.IdRes
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.TtsState
import ca.rmen.android.poetassistant.databinding.ResultListHeaderBinding
import ca.rmen.android.poetassistant.di.NonAndroidEntryPoint
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryEntry
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryListExporter
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryLiveData
import ca.rmen.android.poetassistant.main.dictionaries.rt.FavoritesListExporter
import ca.rmen.android.poetassistant.main.dictionaries.rt.FavoritesLiveData
import ca.rmen.android.poetassistant.main.dictionaries.rt.PatternListExporter
import ca.rmen.android.poetassistant.main.dictionaries.rt.PatternLiveData
import ca.rmen.android.poetassistant.main.dictionaries.rt.RTEntryViewModel
import ca.rmen.android.poetassistant.main.dictionaries.rt.RhymerListExporter
import ca.rmen.android.poetassistant.main.dictionaries.rt.RhymerLiveData
import ca.rmen.android.poetassistant.main.dictionaries.rt.ThesaurusListExporter
import ca.rmen.android.poetassistant.main.dictionaries.rt.ThesaurusLiveData
import ca.rmen.android.poetassistant.wotd.WotdEntryViewModel
import ca.rmen.android.poetassistant.wotd.WotdListExporter
import ca.rmen.android.poetassistant.wotd.WotdLiveData
import dagger.hilt.android.EntryPointAccessors

object ResultListFactory {
    private val TAG = Constants.TAG + ResultListFactory::class.java.simpleName

    fun createListFragment(tab: Tab, initialQuery: String?): ResultListFragment<Any> {
        Log.d(TAG, "createListFragment: tab=$tab, initialQuery = $initialQuery")
        val fragment = when (tab) {
            Tab.PATTERN, Tab.FAVORITES, Tab.RHYMER, Tab.THESAURUS -> ResultListFragment<RTEntryViewModel>()
            Tab.WOTD -> ResultListFragment<WotdEntryViewModel>()
            else -> ResultListFragment<DictionaryEntry.DictionaryEntryDetails>()
        }
        val bundle = Bundle(2)
        bundle.putSerializable(ResultListFragment.EXTRA_TAB, tab)
        if (initialQuery != null) {
            bundle.putString(ResultListFragment.EXTRA_QUERY, initialQuery)
        }
        fragment.arguments = bundle
        return fragment
    }

    fun createAdapter(activity: Activity, tab: Tab): ResultListAdapter<out Any> {
        return EntryPointAccessors.fromApplication(activity, NonAndroidEntryPoint::class.java)
            .resultListAdapterFactory().createAdapter(activity, tab)
    }

    fun createViewModel(tab: Tab, fragment: Fragment): ResultListViewModel<*>? {
        return if (fragment.context != null) {
            val factory = createViewModelFactory(tab, fragment.context!!.applicationContext as Application)
            ViewModelProvider(fragment, factory).get(ResultListViewModel::class.java)
        } else {
            null
        }
    }

    private fun createViewModelFactory(tab: Tab, application: Application): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return when (tab) {
                    Tab.PATTERN, Tab.FAVORITES, Tab.RHYMER, Tab.THESAURUS -> ResultListViewModel<RTEntryViewModel>(application, tab)
                    Tab.WOTD -> ResultListViewModel<WotdEntryViewModel>(application, tab)
                    else -> ResultListViewModel<DictionaryEntry.DictionaryEntryDetails>(application, tab)
                } as (T)
            }
        }
    }

    fun createLiveData(tab: Tab, context: Context, query: String?, filter: String?): ResultListLiveData<out ResultListData<Any>> {
        return when (tab) {
            Tab.PATTERN -> PatternLiveData(context, query!!)
            Tab.FAVORITES -> FavoritesLiveData(context)
            Tab.WOTD -> WotdLiveData(context)
            Tab.RHYMER -> RhymerLiveData(context, query!!, filter)
            Tab.THESAURUS -> ThesaurusLiveData(context, query!!, filter)
            else -> DictionaryLiveData(context, query!!)
        }
    }

    fun createExporter(context: Context, tab: Tab): ResultListExporter<*> {
        return when (tab) {
            Tab.PATTERN -> PatternListExporter(context)
            Tab.FAVORITES -> FavoritesListExporter(context)
            Tab.WOTD -> WotdListExporter(context)
            Tab.RHYMER -> RhymerListExporter(context)
            Tab.THESAURUS -> ThesaurusListExporter(context)
            else -> DictionaryListExporter(context)
        }
    }

    fun createFilterDialog(context: Context, tab: Tab, text: String?): FilterDialogFragment {
        val dialogMessage = when (tab) {
            Tab.RHYMER -> context.getString(R.string.filter_rhymer_message)
            else -> context.getString(R.string.filter_thesaurus_message)
        }
        return FilterDialogFragment.newInstance(dialogMessage, text)
    }

    fun getFilterLabel(context: Context, tab: Tab): String {
        return when (tab) {
            Tab.RHYMER -> context.getString(R.string.filter_rhymer_label)
            else -> context.getString(R.string.filter_thesaurus_label)
        }
    }

    fun getEmptyListText(context: Context, tab: Tab, query: String): String {
        return when (tab) {
            Tab.FAVORITES -> context.getString(R.string.empty_favorites_list)
            Tab.PATTERN -> context.getString(R.string.empty_pattern_list_with_query, query)
            Tab.RHYMER -> context.getString(R.string.empty_rhymer_list_with_query, query)
            Tab.THESAURUS -> context.getString(R.string.empty_thesaurus_list_with_query, query)
            else -> context.getString(R.string.empty_dictionary_list_with_query, query)
        }
    }

    fun isLoadWithoutQuerySupported(tab: Tab): Boolean {
        return when (tab) {
            Tab.FAVORITES, Tab.WOTD -> true
            else -> false
        }
    }

    /**
     * Set the various buttons which appear in the result list header (ex: tts play,
     * web search, filter, help) to visible or gone, depending on the tab.
     */
    fun updateListHeaderButtonsVisibility(binding: ResultListHeaderBinding, tab: Tab, ttsStatus: TtsState.TtsStatus) {
        when (tab) {
            Tab.FAVORITES -> {
                binding.btnPlay.visibility = View.GONE
                binding.btnWebSearch.visibility = View.GONE
                binding.btnStarQuery.visibility = View.GONE
                binding.btnDelete.visibility = View.VISIBLE
            }
            Tab.WOTD -> {
                binding.btnPlay.visibility = View.GONE
                binding.btnWebSearch.visibility = View.GONE
                binding.btnStarQuery.visibility = View.GONE
                binding.btnDelete.visibility = View.GONE
            }
            Tab.PATTERN -> {
                binding.btnHelp.visibility = View.VISIBLE
                binding.btnPlay.visibility = View.GONE
                binding.btnWebSearch.visibility = View.GONE
                binding.btnStarQuery.visibility = View.GONE
            }
            Tab.RHYMER, Tab.THESAURUS -> {
                binding.btnFilter.visibility = View.VISIBLE
            }
            Tab.DICTIONARY -> {
                val playButtonVisibility = if (ttsStatus == TtsState.TtsStatus.UNINITIALIZED) View.GONE else View.VISIBLE
                binding.btnPlay.visibility = playButtonVisibility
            }
            Tab.READER -> Unit
        }
    }

    /**
     * Determine if the matched word in the results list header should be selectable
     * (for copying/pasting).
     *
     * If we make the matched word always selectable, this causes issues with the
     * app bar: When loading the app, the content scrolls up, partially hiding the
     * top bar.
     *
     * Maybe there's a better way to avoid this scrolling. But in any case, it doesn't
     * make sense for the text to be selectable if it's empty, or if it's just a "label"
     * as in the case of the favorites result list header.
     */
    fun getMatchedWordSelectability(tab: Tab, matchedWord: String) =
        matchedWord.isNotBlank() && tab != Tab.FAVORITES

    fun getTabName(context: Context, tab: Tab): String {
        return when (tab) {
            Tab.PATTERN -> context.getString(R.string.tab_pattern)
            Tab.FAVORITES -> context.getString(R.string.tab_favorites)
            Tab.WOTD -> context.getString(R.string.tab_wotd)
            Tab.RHYMER -> context.getString(R.string.tab_rhymer)
            Tab.THESAURUS -> context.getString(R.string.tab_thesaurus)
            Tab.DICTIONARY -> context.getString(R.string.tab_dictionary)
            else -> context.getString(R.string.tab_reader)
        }
    }
    @IdRes
    fun getRecyclerViewId(tab: Tab): Int = when (tab) {
        Tab.PATTERN -> R.id.pattern_recycler_view
        Tab.FAVORITES -> R.id.favorites_recycler_view
        Tab.WOTD -> R.id.wotd_recycler_view
        Tab.RHYMER -> R.id.rhymer_recycler_view
        Tab.THESAURUS -> R.id.thesaurus_recycler_view
        Tab.DICTIONARY -> R.id.dictionary_recycler_view
        else -> R.id.recycler_view
    }
}
