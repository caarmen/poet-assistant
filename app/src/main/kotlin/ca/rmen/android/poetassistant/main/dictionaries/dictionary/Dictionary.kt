/*
 * Copyright (c) 2016 - 2017 Carmen Alvarez
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

import android.database.Cursor
import android.text.TextUtils
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.main.dictionaries.EmbeddedDb
import ca.rmen.android.poetassistant.main.dictionaries.textprocessing.WordSimilarities
import java.util.Random
import javax.inject.Inject

class Dictionary @Inject constructor(private val embeddedDb: EmbeddedDb) {
    companion object {
        // When looking up random words, their "frequency" is a factor in the selection.
        // Words which are too frequent (a, the, why) are not interesting words.
        // Words which are too rare (aalto) are likely not interesting either.
        private const val MIN_INTERESTING_FREQUENCY = 1500
        private const val MAX_INTERESTING_FREQUENCY = 25000

        private const val MAX_PREFIX_MATCHES = 10
    }

    fun isLoaded(): Boolean = embeddedDb.isLoaded()

    fun lookup(word: String): DictionaryEntry {

        val projection = arrayOf("part_of_speech", "definition")
        val selection = "word=?"
        var selectionArgs = arrayOf(word)
        var lookupWord = word
        var cursor = embeddedDb.query("dictionary", projection, selection, selectionArgs)
        if (cursor != null && cursor.count == 0) {
            val closestWord = WordSimilarities().findClosestWord(word, embeddedDb)
            if (closestWord != null) {
                lookupWord = closestWord
                cursor.close()
                selectionArgs = arrayOf(lookupWord)
                cursor = embeddedDb.query("dictionary", projection, selection, selectionArgs)
            }
        }
        if (cursor != null) {
            val result = ArrayList<DictionaryEntry.DictionaryEntryDetails>()
            return try {
                while (cursor.moveToNext()) {
                    val partOfSpeech = cursor.getString(0)
                    val definition = cursor.getString(1)
                    result.add(DictionaryEntry.DictionaryEntryDetails(partOfSpeech, definition))
                }
                DictionaryEntry(lookupWord, result)
            } finally {
                cursor.close()
            }
        }
        return DictionaryEntry(word, emptyList())
    }

    fun findWordsByPattern(pattern: String): Array<String> {
        val projection = arrayOf("word")
        val selection = "word LIKE ?"
        val selectionArgs = arrayOf(pattern)
        val orderBy = "word"
        val limit = Constants.MAX_RESULTS.toString()
        embeddedDb.query(true, "dictionary", projection, selection, selectionArgs, orderBy, limit)?.use { cursor ->
            if (cursor.count > 0) {
                val result = Array(cursor.count, { _ -> "" })
                while (cursor.moveToNext()) {
                    result[cursor.position] = cursor.getString(0)
                }
                return result
            }
        }
        return emptyArray()
    }

    /**
     * @return at most limit words starting with the given prefix
     */
    fun findWordsWithPrefix(prefix: String): Array<String> {
        val projection = arrayOf("word")
        val selection = "has_definition=1 AND word LIKE ?"
        val selectionArgs = arrayOf(prefix + "%")
        val orderBy = "word"
        embeddedDb.query(true, "word_variants", projection, selection, selectionArgs,
                orderBy, MAX_PREFIX_MATCHES.toString())?.use { cursor ->
            if (cursor.count > 0) {
                val result = Array(cursor.count, { _ -> "" })
                while (cursor.moveToNext()) {
                    result[cursor.position] = cursor.getString(0)
                }
                return result
            }
        }
        return emptyArray()
    }

    fun getRandomEntry(): DictionaryEntry? {
        return getRandomEntry(0)
    }

    /**
     * @param seed used for the random selection
     */
    fun getRandomEntry(seed: Long): DictionaryEntry? {
        return getRandomWordCursor()?.use { cursor ->
            var word: String? = null
            val random = Random()
            if (seed > 0) random.setSeed(seed)
            val position = random.nextInt(cursor.count)
            if (cursor.moveToPosition(position)) {
                word = cursor.getString(0)
            }
            if (TextUtils.isEmpty(word)) null
            else lookup(word!!)
        }
    }

    fun getRandomWordCursor(): Cursor? {
        val projection = arrayOf("word")
        val selection = "google_ngram_frequency > ? AND google_ngram_frequency < ?"
        val orderBy = "word ASC"
        val args = arrayOf(MIN_INTERESTING_FREQUENCY.toString(), MAX_INTERESTING_FREQUENCY.toString())
        return embeddedDb.query(false, "stems", projection, selection, args, orderBy, null)
    }
}
