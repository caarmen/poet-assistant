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

package ca.rmen.android.poetassistant.main.dictionaries.rt

import android.content.Context
import android.text.TextUtils
import android.util.Log
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.FavoritesRepository
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.di.NonAndroidEntryPoint
import ca.rmen.android.poetassistant.main.dictionaries.ResultListData
import ca.rmen.android.poetassistant.main.dictionaries.ResultListLiveData
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary
import ca.rmen.android.poetassistant.main.dictionaries.search.Patterns
import ca.rmen.android.poetassistant.settings.SettingsPrefs
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope

class PatternLiveData(
    context: Context,
    coroutineScope: CoroutineScope,
    private val query: String,
) :
    ResultListLiveData<ResultListData<RTListItem>>(context, coroutineScope) {
    companion object {
        private val TAG = Constants.TAG + PatternLiveData::class.java.simpleName
    }

    private val mDictionary: Dictionary
    private val mPrefs: SettingsPrefs
    private val mFavoritesRepository: FavoritesRepository

    init {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, NonAndroidEntryPoint::class.java)
        mDictionary = entryPoint.dictionary()
        mPrefs = entryPoint.prefs()
        mFavoritesRepository = entryPoint.favoritesRepository()
    }

    override fun loadInBackground(): ResultListData<RTListItem> {
        Log.d(TAG, "loadInBackground, query=$query")

        val data = ArrayList<RTListItem>()
        if (TextUtils.isEmpty(query)) return emptyResult()
        val matches = mDictionary.findWordsByPattern(Patterns.convertForSqlite(query))
        if (matches.isEmpty()) {
            return emptyResult()
        }

        val favorites = mFavoritesRepository.getFavorites()
        if (favorites.isNotEmpty()) {
            matches.sortWith(MatchComparator(favorites))
        }

        val layout = SettingsPrefs.getLayout(mPrefs)
        matches.forEach { match ->
            data.add(
                RTListItem(
                    type = RTListItem.Type.WORD,
                    text = match,
                    isFavorite = favorites.contains(match),
                    showButtons = layout == SettingsPrefs.Layout.EFFICIENT,
                )
            )
        }

        if (matches.size == Constants.MAX_RESULTS) {
            data.add(RTListItem(
                    type=RTListItem.Type.SUBHEADING,
                    text=context.getString(R.string.max_results, Constants.MAX_RESULTS),
            ))
        }
        return ResultListData(query, data)
    }

    private class MatchComparator constructor(private val favorites: Set<String>) : Comparator<String> {
        override fun compare(o1: String, o2: String): Int {
            if (favorites.contains(o1) && !favorites.contains(o2)) {
                return -1
            }
            if (favorites.contains(o2) && !favorites.contains(o1)) {
                return 1
            }
            return o1.compareTo(o2)
        }
    }

    private fun emptyResult(): ResultListData<RTListItem> = ResultListData(query, emptyList())

}