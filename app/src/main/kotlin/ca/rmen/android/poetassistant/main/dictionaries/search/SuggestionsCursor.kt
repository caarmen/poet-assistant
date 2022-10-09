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

import android.app.SearchManager
import android.content.Context
import android.database.MatrixCursor
import android.provider.BaseColumns
import android.text.TextUtils
import androidx.annotation.DrawableRes
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.dagger.DaggerHelper
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary
import java.util.Locale
import javax.inject.Inject

/**
 * SharedPreferences and db-backed cursor to read suggestions.  Suggestions include
 * words which have been looked up before, as well as similar words in the database.
 */
class SuggestionsCursor(context: Context, private val filter: String?) : MatrixCursor(COLUMNS) {

    companion object {
        private val COLUMNS = arrayOf(BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_ICON_1,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA)
    }

    @Inject lateinit var mDictionary: Dictionary
    @Inject lateinit var mSuggestions: Suggestions

    init {
        DaggerHelper.getMainScreenComponent(context).inject(this)
        loadHistory()
        loadSimilarWords()
    }

    private fun loadHistory() {
        // https://code.google.com/p/android/issues/detail?id=226686
        /*@DrawableRes*/
        val iconId = R.drawable.ic_search_history
        val suggestions = mSuggestions.getSuggestions()
        suggestions.asSequence().filter { TextUtils.isEmpty(filter) || it.contains(filter!!) }
                .distinct()
                .sorted().toList()
                .forEach { addSuggestion(it, iconId) }

    }

    private fun loadSimilarWords() {
        if (!TextUtils.isEmpty(filter)) {
            val similarSoundingWords = mDictionary.findWordsWithPrefix(filter!!.trim().lowercase(Locale.getDefault()))
            // https://code.google.com/p/android/issues/detail?id=226686
            /*@DrawableRes*/
            val iconId = R.drawable.ic_action_search
            similarSoundingWords.forEach { addSuggestion(it, iconId) }
        }
    }

    private fun addSuggestion(word: String, @DrawableRes iconId: Int) {
        addRow(arrayOf(count, word, iconId, word))
    }
}
