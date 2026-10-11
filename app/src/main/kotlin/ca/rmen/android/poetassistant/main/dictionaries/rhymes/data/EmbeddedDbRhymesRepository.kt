/*
 * Copyright (c) 2016-present Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.dictionaries.rhymes.data

import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.di.IODispatcher
import ca.rmen.android.poetassistant.main.dictionaries.EmbeddedDb
import ca.rmen.android.poetassistant.main.dictionaries.rhymes.RhymeResult
import ca.rmen.android.poetassistant.main.dictionaries.rhymes.RhymesRepository
import ca.rmen.rhymer.RhymeResult as JarRhymeResult
import ca.rmen.rhymer.WordVariant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.SortedSet
import java.util.TreeSet
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Rhymes repository backed by the embedded db.
 *
 * The syllable queries are provided to the pure-Java rhymer algorithm
 * library, on the embedded db. The rhymer instance is created per call:
 * the match settings are captured by the instance, so concurrent calls
 * cannot interfere.
 */
@Singleton
class EmbeddedDbRhymesRepository @Inject constructor(
    private val embeddedDb: EmbeddedDb,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : RhymesRepository {

    override suspend fun getFlatRhymes(
        word: String,
        allRhymesEnabled: Boolean,
        aoToAaMatchEnabled: Boolean,
        aorToAoMatchEnabled: Boolean,
    ): Set<String> = withContext(ioDispatcher) {
        val rhymeResults = rhymingWords(word, allRhymesEnabled, aoToAaMatchEnabled, aorToAoMatchEnabled)
        buildSet {
            rhymeResults.forEach { rhymeResult ->
                addAll(rhymeResult.strictRhymes)
                addAll(rhymeResult.oneSyllableRhymes)
                addAll(rhymeResult.twoSyllableRhymes)
                addAll(rhymeResult.threeSyllableRhymes)
            }
        }
    }

    override suspend fun getRhymingWords(
        word: String,
        allRhymesEnabled: Boolean,
        aoToAaMatchEnabled: Boolean,
        aorToAoMatchEnabled: Boolean,
    ): List<RhymeResult>? = withContext(ioDispatcher) {
        // An unknown word (no pronunciation variants) is the not-found path.
        if (getWordVariants(word).isEmpty()) null
        else rhymingWords(word, allRhymesEnabled, aoToAaMatchEnabled, aorToAoMatchEnabled).map { it.toRhymeResult() }
    }

    override suspend fun getWordsWithDefinitions(words: List<String>): Set<String> = withContext(ioDispatcher) {
        if (words.isEmpty()) return@withContext emptySet()
        val result = HashSet<String>()
        val projection = arrayOf("word")
        val allArgs = words.toTypedArray()
        val queryCount = EmbeddedDb.getQueryCount(allArgs.size)
        for (i in 0 until queryCount) {
            val queryWords = EmbeddedDb.getArgsInQuery(allArgs, i)
            val selection = "word in " + EmbeddedDb.buildInClause(queryWords.size) + " AND has_definition=1"
            embeddedDb.query("word_variants", projection, selection, queryWords)?.use { cursor ->
                while (cursor.moveToNext()) {
                    result.add(cursor.getString(0))
                }
            }
        }
        result
    }

    /**
     * The rhyme results per pronunciation variant, from the rhymer algorithm
     * on the embedded db.
     */
    private fun rhymingWords(
        word: String,
        allRhymesEnabled: Boolean,
        aoToAaMatchEnabled: Boolean,
        aorToAoMatchEnabled: Boolean,
    ): List<JarRhymeResult> = rhymer(allRhymesEnabled, aoToAaMatchEnabled, aorToAoMatchEnabled)
        .getRhymingWords(word, Constants.MAX_RESULTS)

    /**
     * A rhymer algorithm instance over the embedded db, capturing the match
     * settings.
     */
    private fun rhymer(
        allRhymesEnabled: Boolean,
        aoToAaMatchEnabled: Boolean,
        aorToAoMatchEnabled: Boolean,
    ): ca.rmen.rhymer.Rhymer = object : ca.rmen.rhymer.Rhymer() {
        override fun getWordVariants(word: String): List<WordVariant> =
            this@EmbeddedDbRhymesRepository.getWordVariants(word)

        override fun getWordsWithLastStressSyllable(lastStressSyllable: String): SortedSet<String> =
            lookupBySyllable(lastStressSyllable, "stress_syllables", allRhymesEnabled, aoToAaMatchEnabled, aorToAoMatchEnabled)

        override fun getWordsWithLastSyllable(lastSyllable: String): SortedSet<String> =
            lookupBySyllable(lastSyllable, "last_syllable", allRhymesEnabled, aoToAaMatchEnabled, aorToAoMatchEnabled)

        override fun getWordsWithLastTwoSyllables(lastTwoSyllables: String): SortedSet<String> =
            lookupBySyllable(lastTwoSyllables, "last_two_syllables", allRhymesEnabled, aoToAaMatchEnabled, aorToAoMatchEnabled)

        override fun getWordsWithLastThreeSyllables(lastThreeSyllables: String): SortedSet<String> =
            lookupBySyllable(lastThreeSyllables, "last_three_syllables", allRhymesEnabled, aoToAaMatchEnabled, aorToAoMatchEnabled)
    }

    /**
     * Maps a rhymer library result to the repository model.
     */
    private fun JarRhymeResult.toRhymeResult() = RhymeResult(
        variantNumber = variantNumber,
        strictRhymes = strictRhymes.toList(),
        oneSyllableRhymes = oneSyllableRhymes.toList(),
        twoSyllableRhymes = twoSyllableRhymes.toList(),
        threeSyllableRhymes = threeSyllableRhymes.toList(),
    )

    /**
     * The pronunciation variants of the given word, from the embedded db.
     */
    private fun getWordVariants(word: String): List<WordVariant> {
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
                result.add(WordVariant(variantNumber, lastStressSyllable, lastSyllable, lastTwoSyllables, lastThreeSyllables))
            }
        }
        return result
    }

    /**
     * Queries words matching the given syllables.
     * The match settings transform the syllables sequentially: AOR->AO first,
     * then AO->AA on the already-transformed syllables. Each substitution
     * applies only when the original syllables contain its phoneme.
     * When all-rhymes is disabled, only words with definitions are returned.
     */
    private fun lookupBySyllable(
        syllables: String,
        columnName: String,
        allRhymesEnabled: Boolean,
        aoToAaMatchEnabled: Boolean,
        aorToAoMatchEnabled: Boolean,
    ): SortedSet<String> {
        val result = TreeSet<String>()
        val projection = arrayOf("word")
        var selectionColumn = columnName
        var inputSyllables = syllables
        if (aorToAoMatchEnabled && syllables.contains("AO")) {
            selectionColumn = String.format(Locale.US, "replace(%s, 'AOR', 'AO')", selectionColumn)
            inputSyllables = inputSyllables.replace("AOR", "AO")
        }
        if (aoToAaMatchEnabled && (syllables.contains("AO") || syllables.contains("AA"))) {
            selectionColumn = String.format(Locale.US, "replace(%s, 'AO', 'AA')", selectionColumn)
            inputSyllables = inputSyllables.replace("AO", "AA")
        }
        var selection = "$selectionColumn = ? "
        if (!allRhymesEnabled) {
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
