/*
 * Copyright (c) 2017 Carmen Alvarez
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

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.content.SharedPreferences
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.preference.PreferenceManager
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.util.Log
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.Favorite
import ca.rmen.android.poetassistant.Favorites
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.compat.VectorCompat
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.settings.Settings
import ca.rmen.android.poetassistant.settings.SettingsPrefs
import javax.inject.Inject

class ResultListViewModel<T> constructor(application: Application, private val tab: Tab) : AndroidViewModel(application) {
    companion object {
        private val TAG = Constants.TAG + ResultListViewModel::class.java.simpleName
    }

    val isDataAvailable = ObservableBoolean()
    val emptyText = ObservableField<CharSequence>()
    val layout = MutableLiveData<Settings.Layout>()
    val showHeader = MutableLiveData<Boolean>()
    val usedQueryWord = MutableLiveData<String>()
    private var mAdapter: ResultListAdapter<T>? = null
    @Inject lateinit var mFavorites: Favorites

    data class QueryParams(val word: String, val filter: String?)

    private val mPrefsListener : PrefsListener
    private val mQueryParams = MutableLiveData<QueryParams>()
    val resultListDataLiveData: LiveData<ResultListData<T>>
    val favoritesLiveData: LiveData<List<Favorite>>

    init {
        ResultListFactory.inject(application, tab, this)
        emptyText.set(getNoQueryEmptyText())
        mPrefsListener = PrefsListener()
        PreferenceManager.getDefaultSharedPreferences(application).registerOnSharedPreferenceChangeListener(mPrefsListener)
        favoritesLiveData = mFavorites.favoritesLiveData
        resultListDataLiveData = Transformations.switchMap(mQueryParams, { queryParams ->
            @Suppress("UNCHECKED_CAST")
            ResultListFactory.createLiveData(tab, application, queryParams.word, queryParams.filter) as LiveData<ResultListData<T>>
        })
    }

    fun setQueryParams(queryParams: QueryParams) {
        Log.v(TAG, "$tab: setQueryParams $queryParams")
        if (!TextUtils.isEmpty(queryParams.word) || ResultListFactory.isLoadWithoutQuerySupported(tab)) {
            mQueryParams.value = queryParams
        }
    }

    fun setAdapter(adapter: ResultListAdapter<T>) {
        mAdapter = adapter
    }

    fun share(query: String, filter: String?) {
        mAdapter?.let {
            Share.share(getApplication(), tab, query, filter, it.all)
        }
    }

    private fun updateDataAvailable() {
        mAdapter?.let {
            isDataAvailable.set(it.itemCount > 0)
            isDataAvailable.notifyChange()
        }
    }

    fun setData(loadedData: ResultListData<T>?) {
        Log.v(TAG, "$tab: setData $loadedData")
        mAdapter?.let {
            it.clear()
            if (loadedData != null) it.addAll(loadedData.data)
        }
        val hasQuery = loadedData != null && !TextUtils.isEmpty(loadedData.matchedWord)
        if (!hasQuery) {
            emptyText.set(getNoQueryEmptyText())
        } else if (loadedData!!.data != null) {
            emptyText.set(getNoResultsEmptyText(loadedData.matchedWord))
        } else {
            emptyText.set(null)
        }
        showHeader.value = hasQuery
        if (loadedData != null) {
            usedQueryWord.value = loadedData.matchedWord
        }
        updateDataAvailable()
    }

    // If we have an empty list because the user didn't enter any search term,
    // we'll show a text to tell them to search.
    private fun getNoQueryEmptyText(): CharSequence {
        val emptySearch = getApplication<Application>().getString(R.string.empty_list_without_query)
        val imageSpan = VectorCompat.createVectorImageSpan(getApplication(), R.drawable.ic_action_search_dark)
        val ssb = SpannableStringBuilder(emptySearch)
        val iconIndex = emptySearch.indexOf("%s")
        ssb.setSpan(imageSpan, iconIndex, iconIndex + 2, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        return ssb
    }

    // If the user entered a query and there are no matches, show the normal "no results" text.
    private fun getNoResultsEmptyText(query: String): CharSequence {
        return ResultListFactory.getEmptyListText(getApplication(), tab, query)
    }

    override fun onCleared() {
        super.onCleared()
        PreferenceManager.getDefaultSharedPreferences(getApplication()).unregisterOnSharedPreferenceChangeListener(mPrefsListener)
    }

    private inner class PrefsListener : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            if (Settings.PREF_LAYOUT == key) {
                layout.value = Settings.getLayout(SettingsPrefs.get(getApplication()))
            }
        }

    }
}
