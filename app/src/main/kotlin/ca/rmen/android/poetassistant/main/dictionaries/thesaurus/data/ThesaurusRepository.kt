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

import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ThesaurusEntry

/**
 * Repository defining thesaurus data access.
 */
interface ThesaurusRepository {

    /**
     * Forward lookup in the thesaurus table.
     *
     * @param word The word to look up.
     * @return the entry for the word, or null if the word has no row in
     * the thesaurus table — the closest-word fallback trigger.
     */
    suspend fun lookupForward(word: String): ThesaurusEntry?

    /**
     * Entries of words listing [word] among their synonyms, excluding
     * [excludeWords] (the forward synonyms), merged per word type.
     *
     * @param word The word to reverse-look up.
     * @param excludeWords Words to exclude from the result.
     * @return the reverse-lookup entries, merged per word type.
     */
    suspend fun lookupReverseSynonyms(word: String, excludeWords: Set<String>): List<ThesaurusEntry.ThesaurusEntryDetails>

    /**
     * Entries of words listing [word] among their antonyms, excluding
     * [excludeWords] (the forward antonyms), merged per word type.
     *
     * @param word The word to reverse-look up.
     * @param excludeWords Words to exclude from the result.
     * @return the reverse-lookup entries, merged per word type.
     */
    suspend fun lookupReverseAntonyms(word: String, excludeWords: Set<String>): List<ThesaurusEntry.ThesaurusEntryDetails>

    /**
     * Gets words sharing the given stem.
     *
     * @param stem The stem to search for.
     * @return List of words that share this stem.
     */
    suspend fun getWordsByStem(stem: String): List<String>
}
