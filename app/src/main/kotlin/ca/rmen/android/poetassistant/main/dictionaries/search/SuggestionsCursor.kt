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
import android.database.MatrixCursor
import android.provider.BaseColumns
import androidx.annotation.DrawableRes
import ca.rmen.android.poetassistant.R

/**
 * SharedPreferences and db-backed cursor to read suggestions.  Suggestions include
 * words which have been looked up before, as well as similar words in the database.
 */
class SuggestionsCursor(
    private val suggestionsRepository: SuggestionsRepository,
    private val filter: String?,
) : MatrixCursor(COLUMNS) {

    companion object {
        private val COLUMNS = arrayOf(
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_ICON_1,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA
        )
    }

    suspend fun load() {
        suggestionsRepository.getSuggestions(filter).forEach { entry ->
            addSuggestion(
                word = entry.word,
                iconId = when (entry.source) {
                    // https://code.google.com/p/android/issues/detail?id=226686
                    Source.HISTORY -> R.drawable.ic_search_history
                    Source.DICTIONARY -> R.drawable.ic_action_search
                }
            )
        }
    }


    private fun addSuggestion(word: String, @DrawableRes iconId: Int) {
        addRow(arrayOf<Any>(count, word, iconId, word))
    }
}
