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
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import android.text.TextUtils
import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.MutableLiveData
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.Favorite
import ca.rmen.android.poetassistant.Favorites
import ca.rmen.android.poetassistant.dagger.DaggerHelper
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.settings.SettingsPrefs
import javax.inject.Inject

class ResultListViewModel<T: Any> constructor(application: Application, private val tab: Tab) : AndroidViewModel(application) {
    companion object {
        private val TAG = Constants.TAG + ResultListViewModel::class.java.simpleName
    }

    val isDataAvailable = ObservableBoolean()
    val emptyText = MutableLiveData<EmptyText>()
    val layout = MutableLiveData<ca.rmen.android.poetassistant.settings.SettingsPrefs.Layout>()
    val showHeader = MutableLiveData<Boolean>()
    val usedQueryWord = MutableLiveData<String>()
    private var mAdapter: ResultListAdapter<T>? = null
    @Inject lateinit var mFavorites: Favorites

    data class QueryParams(val word: String?, val filter: String?)

    private val mPrefsListener : PrefsListener
    private val mQueryParams = MutableLiveData<QueryParams>()
    val resultListDataLiveData: LiveData<ResultListData<T>>
    val favoritesLiveData: LiveData<List<Favorite>>

    init {
        ResultListFactory.inject(application, tab, this)
        emptyText.value = EmptyTextNoQuery
        mPrefsListener = PrefsListener()
        PreferenceManager.getDefaultSharedPreferences(application).registerOnSharedPreferenceChangeListener(mPrefsListener)
        favoritesLiveData = mFavorites.getFavoritesLiveData()
        resultListDataLiveData = mQueryParams.switchMap { queryParams ->
            @Suppress("UNCHECKED_CAST")
            ResultListFactory.createLiveData(tab, application, queryParams.word, queryParams.filter) as LiveData<ResultListData<T>>
        }
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
            Share.share(getApplication(), tab, query, filter, it.getAll())
        }
    }

    fun setData(loadedData: ResultListData<T>?) {
        Log.v(TAG, "$tab: setData adapter=$mAdapter, data=$loadedData")
        mAdapter?.let {
            if (loadedData != null) it.submitList(loadedData.data)
            else it.submitList(emptyList())
        }
        val hasQuery = loadedData != null && !TextUtils.isEmpty(loadedData.matchedWord)
        if (!hasQuery) {
            emptyText.value = EmptyTextNoQuery
        } else if (loadedData!!.data != null) {
            emptyText.value = EmptyTextNoResults(loadedData.matchedWord)
        } else {
            emptyText.value = EmptyTextHidden
        }
        showHeader.value = hasQuery
        if (loadedData != null) {
            usedQueryWord.value = loadedData.matchedWord
        }
        isDataAvailable.set(loadedData?.data?.isNotEmpty()!!)
        isDataAvailable.notifyChange()
    }

    override fun onCleared() {
        super.onCleared()
        PreferenceManager.getDefaultSharedPreferences(getApplication()).unregisterOnSharedPreferenceChangeListener(mPrefsListener)
    }

    private inner class PrefsListener : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            if (SettingsPrefs.PREF_LAYOUT == key) {
                layout.value = SettingsPrefs.getLayout(DaggerHelper.getMainScreenComponent(getApplication()).getSettingsPrefs())
            }
        }

    }
}
