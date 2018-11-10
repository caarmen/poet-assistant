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

package ca.rmen.android.poetassistant.wotd

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.format.DateUtils
import android.util.Log
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.Favorites
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.dagger.DaggerHelper
import ca.rmen.android.poetassistant.main.dictionaries.ResultListData
import ca.rmen.android.poetassistant.main.dictionaries.ResultListLiveData
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary
import ca.rmen.android.poetassistant.settings.Settings
import ca.rmen.android.poetassistant.settings.SettingsPrefs
import java.util.Calendar
import java.util.Random
import java.util.TimeZone
import javax.inject.Inject

class WotdLiveData(context: Context) : ResultListLiveData<ResultListData<WotdEntryViewModel>>(context) {
    companion object {
        private val TAG = Constants.TAG + WotdLiveData::class.java.simpleName
    }

    @Inject lateinit var mDictionary: Dictionary
    private val mPrefs = SettingsPrefs.get(context.applicationContext)
    @Inject lateinit var mFavorites: Favorites

    init {
        DaggerHelper.getWotdComponent(context).inject(this)
    }

    override fun loadInBackground(): ResultListData<WotdEntryViewModel> {
        Log.d(TAG, "loadInBackground")
        val data = ArrayList<WotdEntryViewModel>(100)
        val cursor = mDictionary.getRandomWordCursor() ?: return emptyResult()

        cursor.use {
            if (cursor.count == 0) return emptyResult()
            val favorites = mFavorites.getFavorites()
            val calendar = Wotd.getTodayUTC()
            val calendarDisplay = Wotd.getTodayUTC()
            calendarDisplay.timeZone = TimeZone.getDefault()
            val layout = Settings.getLayout(mPrefs)
            for (i in 0 until 100) {
                val random = Random()
                random.setSeed(calendar.timeInMillis)
                val date = DateUtils.formatDateTime(context,
                        calendarDisplay.timeInMillis,
                        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_ALL)
                val position = random.nextInt(cursor.count)
                if (cursor.moveToPosition(position)) {
                    val word = cursor.getString(0)
                    /*@ColorRes*/
                    val color = if (i % 2 == 0) R.color.row_background_color_even else R.color.row_background_color_odd
                    data.add(WotdEntryViewModel(context,
                            word,
                            date,
                            ContextCompat.getColor(context, color),
                            favorites.contains(word),
                            layout == Settings.Layout.EFFICIENT))
                }
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                calendarDisplay.add(Calendar.DAY_OF_YEAR, -1)
            }
        }
        return ResultListData(context.getString(R.string.wotd_list_header), data)
    }

    private fun emptyResult(): ResultListData<WotdEntryViewModel> = ResultListData(context.getString(R.string.wotd_list_header), emptyList())

}
