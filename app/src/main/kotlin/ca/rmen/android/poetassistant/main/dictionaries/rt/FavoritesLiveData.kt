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
import androidx.core.content.ContextCompat
import android.util.Log
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.Favorites
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.dagger.DaggerHelper
import ca.rmen.android.poetassistant.main.dictionaries.ResultListData
import ca.rmen.android.poetassistant.main.dictionaries.ResultListLiveData
import ca.rmen.android.poetassistant.settings.SettingsPrefs
import java.util.TreeSet
import javax.inject.Inject

class FavoritesLiveData(context: Context) : ResultListLiveData<ResultListData<RTEntryViewModel>>(context) {
    companion object {
        private val TAG = Constants.TAG + FavoritesLiveData::class.java.simpleName
    }

    @Inject lateinit var mPrefs: SettingsPrefs
    @Inject lateinit var mFavorites: Favorites

    init {
        DaggerHelper.getMainScreenComponent(context).inject(this)
    }

    override fun loadInBackground(): ResultListData<RTEntryViewModel> {
        Log.d(TAG, "loadInBackground")
        val data = ArrayList<RTEntryViewModel>()
        val favorites = mFavorites.getFavorites()
        if (favorites.isEmpty()) return emptyResult()

        val sortedFavorites = TreeSet<String>(favorites)
        val layout = SettingsPrefs.getLayout(mPrefs)
        sortedFavorites.forEachIndexed { i, favorite ->
            /*@ColorRes*/
            val color = if (i % 2 == 0) R.color.row_background_color_even else R.color.row_background_color_odd
            data.add(RTEntryViewModel(
                    context,
                    RTEntryViewModel.Type.WORD,
                    favorite,
                    ContextCompat.getColor(context, color),
                    true,
                    layout == SettingsPrefs.Layout.EFFICIENT
            ))
        }
        return ResultListData(context.getString(R.string.favorites_list_header), data)
    }

    private fun emptyResult(): ResultListData<RTEntryViewModel> = ResultListData(context.getString(R.string.favorites_list_header), emptyList())

}
