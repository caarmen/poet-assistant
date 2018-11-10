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
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.Favorites
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.dagger.DaggerHelper
import ca.rmen.android.poetassistant.main.dictionaries.ResultListData
import ca.rmen.android.poetassistant.main.dictionaries.ResultListLiveData
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary
import ca.rmen.android.poetassistant.main.dictionaries.search.Patterns
import ca.rmen.android.poetassistant.settings.SettingsPrefs
import javax.inject.Inject

class PatternLiveData constructor(context: Context, private val query: String) : ResultListLiveData<ResultListData<RTEntryViewModel>>(context) {
    companion object {
        private val TAG = Constants.TAG + PatternLiveData::class.java.simpleName
    }

    @Inject lateinit var mDictionary: Dictionary
    @Inject lateinit var mPrefs: SettingsPrefs
    @Inject lateinit var mFavorites: Favorites

    init {
        DaggerHelper.getMainScreenComponent(context).inject(this)
    }

    override fun loadInBackground(): ResultListData<RTEntryViewModel> {
        Log.d(TAG, "loadInBackground, query=$query")

        val data = ArrayList<RTEntryViewModel>()
        if (TextUtils.isEmpty(query)) return emptyResult()
        val matches = mDictionary.findWordsByPattern(Patterns.convertForSqlite(query))
        if (matches.isEmpty()) {
            return emptyResult()
        }

        val favorites = mFavorites.getFavorites()
        if (favorites.isNotEmpty()) {
            matches.sortWith(MatchComparator(favorites))
        }

        val layout = SettingsPrefs.getLayout(mPrefs)
        matches.forEachIndexed { i, match ->
            /*@ColorRes*/
            val color = if (i % 2 == 0) R.color.row_background_color_even else R.color.row_background_color_odd
            data.add(RTEntryViewModel(
                    context,
                    RTEntryViewModel.Type.WORD,
                    match,
                    ContextCompat.getColor(context, color),
                    favorites.contains(match),
                    layout == SettingsPrefs.Layout.EFFICIENT))
        }

        if (matches.size == Constants.MAX_RESULTS) {
            data.add(RTEntryViewModel(
                    context,
                    RTEntryViewModel.Type.SUBHEADING,
                    context.getString(R.string.max_results, Constants.MAX_RESULTS)))
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

    private fun emptyResult(): ResultListData<RTEntryViewModel> = ResultListData(query, emptyList())

}