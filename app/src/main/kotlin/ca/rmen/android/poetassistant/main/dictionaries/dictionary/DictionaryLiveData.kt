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

package ca.rmen.android.poetassistant.main.dictionaries.dictionary

import android.content.Context
import android.text.TextUtils
import android.util.Log
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.dagger.DaggerHelper
import ca.rmen.android.poetassistant.main.dictionaries.ResultListData
import ca.rmen.android.poetassistant.main.dictionaries.ResultListLiveData

class DictionaryLiveData(context: Context, private val query: String) : ResultListLiveData<ResultListData<DictionaryEntry.DictionaryEntryDetails>>(context) {
    companion object {
        private val TAG = Constants.TAG + DictionaryLiveData::class.java.simpleName
    }

    override fun loadInBackground(): ResultListData<DictionaryEntry.DictionaryEntryDetails> {
        Log.v(TAG, "loadInBackground: query=$query")
        val result = ArrayList<DictionaryEntry.DictionaryEntryDetails>()
        if (TextUtils.isEmpty(query)) return ResultListData(query, result)
        val dictionary = DaggerHelper.getMainScreenComponent(context).getDictionary()
        val entry = dictionary.lookup(query)
        result.addAll(entry.details)
        return ResultListData(entry.word, result)
    }
}
