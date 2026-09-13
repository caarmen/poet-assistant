/*
 * Copyright (c) 2016 - present Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.dictionaries.dictionary.data

import ca.rmen.android.poetassistant.di.IODispatcher
import ca.rmen.android.poetassistant.main.dictionaries.EmbeddedDb
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryEntry
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryRepository
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.WordNotFoundException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementation of DictionaryRepository using the existing EmbeddedDb class.
 * This is the only place in the new architecture where EmbeddedDb is used.
 * Do not modify EmbeddedDb's package or imports (per spec).
 *
 * @param embeddedDb The legacy embedded database access class.
 * @param ioDispatcher The coroutine dispatcher for IO operations.
 */
class EmbeddedDbDictionaryRepository @Inject constructor(
    private val embeddedDb: EmbeddedDb,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : DictionaryRepository {

    /**
     * Looks up a word in the dictionary.
     *
     * @param word The word to look up.
     * @return The DictionaryEntry containing the word and its definitions.
     * @throws WordNotFoundException if the word is not found in the dictionary.
     */
    override suspend fun lookup(word: String): DictionaryEntry = withContext(ioDispatcher) {
        embeddedDb.query("dictionary", arrayOf("part_of_speech", "definition"), "word=?", arrayOf(word))
            ?.use { cursor ->
                if (cursor.count == 0) throw WordNotFoundException(word)
                val result = mutableListOf<DictionaryEntry.DictionaryEntryDetails>()
                while (cursor.moveToNext()) {
                    result.add(DictionaryEntry.DictionaryEntryDetails(
                        partOfSpeech = cursor.getString(0),
                        definition = cursor.getString(1)
                    ))
                }
                DictionaryEntry(word, result)
            } ?: throw WordNotFoundException(word)
    }

    /**
     * Gets words that share the same stem as the given word.
     *
     * @param stem The stem to search for.
     * @return List of words that share this stem, or an empty list if none found.
     */
    override suspend fun getWordsByStem(stem: String): List<String> = withContext(ioDispatcher) {
        embeddedDb.query(true, "stems", arrayOf("word"), "stem=?", arrayOf(stem), null, null)
            ?.use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(cursor.getString(0))
                    }
                }
            } ?: emptyList()
    }
}
