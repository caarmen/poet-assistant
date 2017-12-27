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

import android.util.Log
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.main.dictionaries.EmbeddedDb
import ca.rmen.android.poetassistant.settings.SettingsPrefs
import ca.rmen.rhymer.WordVariant
import java.util.Locale
import java.util.SortedSet
import java.util.TreeSet
import javax.inject.Inject

class Rhymer @Inject constructor(private val embeddedDb: EmbeddedDb, private val prefs: SettingsPrefs) : ca.rmen.rhymer.Rhymer() {
    companion object {
        private val TAG = Constants.TAG + Rhymer::class.java.simpleName
    }

    fun isLoaded(): Boolean = embeddedDb.isLoaded()

    override fun getWordVariants(word: String): List<WordVariant> {
        val result = ArrayList<WordVariant>()
        val projection = arrayOf("variant_number", "stress_syllables", "last_syllable", "last_two_syllables", "last_three_syllables")
        val selection = "word=?"
        val selectionArgs = arrayOf(word)
        embeddedDb.query("word_variants", projection, selection, selectionArgs)?.use { cursor ->
            while (cursor.moveToNext()) {
                var column = 0
                val variantNumber = cursor.getInt(column++)
                val lastStressSyllable = cursor.getString(column++)
                val lastSyllable = cursor.getString(column++)
                val lastTwoSyllables = cursor.getString(column++)
                val lastThreeSyllables = cursor.getString(column)
                val wordVariant = WordVariant(variantNumber, lastStressSyllable, lastSyllable, lastTwoSyllables, lastThreeSyllables)
                result.add(wordVariant)
            }
        }
        return result
    }

    /**
     * @return the words which rhyme with the given word, in any order, matching one, two or three
     * syllables.
     */
    fun getFlatRhymes(word: String): Set<String> {
        val rhymeResults = super.getRhymingWords(word, Constants.MAX_RESULTS)
        val flatRhymes = HashSet<String>()
        rhymeResults.forEach {
            flatRhymes.addAll(it.strictRhymes)
            flatRhymes.addAll(it.oneSyllableRhymes)
            flatRhymes.addAll(it.twoSyllableRhymes)
            flatRhymes.addAll(it.threeSyllableRhymes)
        }
        return flatRhymes
    }

    override fun getWordsWithLastStressSyllable(lastStressSyllable: String): SortedSet<String> {
        return lookupBySyllable(lastStressSyllable, "stress_syllables")
    }

    override fun getWordsWithLastSyllable(lastSyllable: String): SortedSet<String> {
        return lookupBySyllable(lastSyllable, "last_syllable")
    }

    override fun getWordsWithLastTwoSyllables(lastTwoSyllables: String): SortedSet<String> {
        return lookupBySyllable(lastTwoSyllables, "last_two_syllables")
    }

    override fun getWordsWithLastThreeSyllables(lastThreeSyllables: String): SortedSet<String> {
        return lookupBySyllable(lastThreeSyllables, "last_three_syllables")
    }

    /**
     * Of the given words, returns a set containing those which have a definition in the dictionary table.
     */
    fun getWordsWithDefinitions(words: Array<String>): Set<String> {
        if (words.isEmpty()) return emptySet()
        Log.v(TAG, "getWordsWithDefinitions for ${words.size} words")
        val result = HashSet<String>()
        val projection = arrayOf("word")
        val queryCount = EmbeddedDb.getQueryCount(words.size)
        for (i in 0 until queryCount) {
            val queryWords = EmbeddedDb.getArgsInQuery(words, i)
            Log.v(TAG, "getWordsWithDefinitions: query $i has ${queryWords.size} words")
            val selection = "word in " + EmbeddedDb.buildInClause(queryWords.size) + " AND has_definition=1"
            embeddedDb.query("word_variants", projection, selection, queryWords)?.use { cursor ->
                while (cursor.moveToNext()) {
                    result.add(cursor.getString(0))
                }
            }
        }
        return result
    }

    private fun lookupBySyllable(syllables: String, columnName: String): SortedSet<String> {
        val result = TreeSet<String>()
        val projection = arrayOf("word")
        var selectionColumn = columnName
        var inputSyllables = syllables
        if (prefs.isAORAOMatchEnabled && syllables.contains("AO")) {
            selectionColumn = String.format(Locale.US, "replace(%s, 'AOR', 'AO'", selectionColumn)
            inputSyllables = inputSyllables.replace("AOR", "AO")
        }
        if (prefs.isAOAAMatchEnabled && (syllables.contains("AO") || syllables.contains("AA"))) {
            selectionColumn = String.format(Locale.US, "replace(%s, 'AO', 'AA')", selectionColumn)
            inputSyllables = inputSyllables.replace("AO", "AA")
        }
        var selection = selectionColumn + " = ? "
        if (!prefs.isAllRhymesEnabled) {
            selection += "AND has_definition=1"
        }
        val selectionArgs = arrayOf(inputSyllables)
        embeddedDb.query("word_variants", projection, selection, selectionArgs)?.use { cursor ->
            while (cursor.moveToNext()) {
                result.add(cursor.getString(0))
            }
        }
        return result
    }
}
