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

package ca.rmen.android.poetassistant.main.dictionaries.search

import android.text.TextUtils
import ca.rmen.android.poetassistant.main.dictionaries.EmbeddedDb
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.Locale

enum class Source {
    HISTORY,
    DICTIONARY,
}

data class Entry(val source: Source, val word: String)

class SuggestionsRepository(
    private val suggestionDao: SuggestionDao,
    private val embeddedDb: EmbeddedDb,
    private val ioDispatcher: CoroutineDispatcher,
) {

    companion object {
        private const val MAX_PREFIX_MATCHES = 10
    }

    suspend fun getSuggestions(filter: String?): List<Entry> {
        return withContext(ioDispatcher) {
            val history = suggestionDao.getSuggestions().map { Entry(Source.HISTORY, it.getWord()) }
                .asSequence().filter { TextUtils.isEmpty(filter) || it.word.contains(filter!!) }
                .distinct()
                .sortedBy { it.word }
                .toList()

            val similarSoundingWords = if (filter.isNullOrBlank()) emptyList() else
                findWordsWithPrefix(filter.trim().lowercase(Locale.getDefault())).map {Entry(Source.DICTIONARY, it)}

            history + similarSoundingWords
        }
    }

    suspend fun addSuggestion(suggestion: String) = withContext(ioDispatcher) {
        suggestionDao.insertAll(Suggestion(suggestion))
    }

    suspend fun clear() = withContext(ioDispatcher) {
        suggestionDao.deleteAll()
    }

    /**
     * @return at most limit words starting with the given prefix
     */
    private fun findWordsWithPrefix(prefix: String): Array<String> {
        val projection = arrayOf("word")
        val selection = "has_definition=1 AND word LIKE ?"
        val selectionArgs = arrayOf("$prefix%")
        val orderBy = "word"
        embeddedDb.query(true, "word_variants", projection, selection, selectionArgs,
            orderBy, MAX_PREFIX_MATCHES.toString())?.use { cursor ->
            if (cursor.count > 0) {
                val result = Array(cursor.count) { "" }
                while (cursor.moveToNext()) {
                    result[cursor.position] = cursor.getString(0)
                }
                return result
            }
        }
        return emptyArray()
    }


}
