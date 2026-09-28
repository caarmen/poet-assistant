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

package ca.rmen.android.poetassistant.main.dictionaries.thesaurus.data

import ca.rmen.android.poetassistant.di.IODispatcher
import ca.rmen.android.poetassistant.main.dictionaries.EmbeddedDb
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ThesaurusEntry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.Collections.singletonList
import java.util.Locale
import javax.inject.Inject

/**
 * Thesaurus repository backed by the embedded db.
 * This is the only place in the thesaurus architecture where EmbeddedDb is used.
 *
 * @param embeddedDb The embedded database access class.
 * @param ioDispatcher The coroutine dispatcher for IO operations.
 */
class EmbeddedDbThesaurusRepository @Inject constructor(
    private val embeddedDb: EmbeddedDb,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : ThesaurusRepository {

    private enum class RelationType(val columnName: String) {
        SYNONYM("synonyms"),
        ANTONYM("antonyms")
    }

    override suspend fun lookupForward(word: String): ThesaurusEntry? = withContext(ioDispatcher) {
        val projection = arrayOf("word_type", "synonyms", "antonyms")
        val selection = "word=?"
        val selectionArgs = arrayOf(word)
        embeddedDb.query("thesaurus", projection, selection, selectionArgs)?.use { cursor ->
            // No rows: the word has no thesaurus entry. This triggers the
            // closest-word fallback in the use case.
            if (cursor.count == 0) return@use null
            val result = mutableListOf<ThesaurusEntry.ThesaurusEntryDetails>()
            while (cursor.moveToNext()) {
                val wordType = ThesaurusEntry.WordType.valueOf(cursor.getString(0))
                val synonyms = split(cursor.getString(1))
                val antonyms = split(cursor.getString(2))
                result.add(ThesaurusEntry.ThesaurusEntryDetails(wordType, synonyms, antonyms))
            }
            ThesaurusEntry(word, result)
        }
    }

    override suspend fun lookupReverseSynonyms(word: String, excludeWords: Set<String>): List<ThesaurusEntry.ThesaurusEntryDetails> =
        lookupReverseRelatedWords(RelationType.SYNONYM, word, excludeWords)

    override suspend fun lookupReverseAntonyms(word: String, excludeWords: Set<String>): List<ThesaurusEntry.ThesaurusEntryDetails> =
        lookupReverseRelatedWords(RelationType.ANTONYM, word, excludeWords)

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

    /**
     * Gets entries of words listing the given word among their synonyms or
     * antonyms (per the relation type), excluding the given words — the
     * forward lookup results. The entries are merged per word type:
     * reverse-lookup entries are unioned among themselves only, never with
     * forward entries.
     */
    private suspend fun lookupReverseRelatedWords(
        relationType: RelationType,
        word: String,
        excludeWords: Set<String>,
    ): List<ThesaurusEntry.ThesaurusEntryDetails> = withContext(ioDispatcher) {
        val projection = arrayOf("word", "word_type")
        var selection = String.format(
            Locale.US, "(%s = ? OR %S LIKE ? OR %S LIKE ? OR %S LIKE ?) ",
            relationType.columnName, relationType.columnName, relationType.columnName, relationType.columnName
        )
        val selectionArgs = Array(4 + excludeWords.size) { "" }
        var i = 0
        selectionArgs[i++] = word // only related word
        selectionArgs[i++] = String.format(Locale.US, "%s,%%", word) // first related word
        selectionArgs[i++] = String.format(Locale.US, "%%,%s", word) // last related word
        selectionArgs[i++] = String.format(Locale.US, "%%,%s,%%", word) // somewhere in the list of related words
        if (excludeWords.isNotEmpty()) {
            selection += " AND word NOT IN " + EmbeddedDb.buildInClause(excludeWords.size)
            excludeWords.forEach { selectionArgs[i++] = it }
        }
        embeddedDb.query("thesaurus", projection, selection, selectionArgs)?.use { cursor ->
            val reverseRelatedWords = mutableListOf<ThesaurusEntry.ThesaurusEntryDetails>()
            while (cursor.moveToNext()) {
                val relatedWord = cursor.getString(0)
                val wordType = ThesaurusEntry.WordType.valueOf(cursor.getString(1))
                val entryDetails = if (relationType == RelationType.SYNONYM) {
                    ThesaurusEntry.ThesaurusEntryDetails(wordType, singletonList(relatedWord), emptyList())
                } else {
                    ThesaurusEntry.ThesaurusEntryDetails(wordType, emptyList(), singletonList(relatedWord))
                }
                reverseRelatedWords.add(entryDetails)
            }
            merge(reverseRelatedWords)
        } ?: emptyList()
    }

    /**
     * Merges entries into one entry per word type: the synonyms and antonyms
     * are unioned.
     */
    private fun merge(entries: List<ThesaurusEntry.ThesaurusEntryDetails>): List<ThesaurusEntry.ThesaurusEntryDetails> {
        return entries.asSequence().groupBy { it.wordType }
            .map { group ->
                group.value.reduce { acc, entryDetails ->
                    ThesaurusEntry.ThesaurusEntryDetails(
                        acc.wordType,
                        acc.synonyms.union(entryDetails.synonyms).toList(),
                        acc.antonyms.union(entryDetails.antonyms).toList(),
                    )
                }
            }.toList()
    }

    private fun split(string: String?): List<String> {
        if (string.isNullOrEmpty()) return emptyList()
        return string.split(",")
    }
}
