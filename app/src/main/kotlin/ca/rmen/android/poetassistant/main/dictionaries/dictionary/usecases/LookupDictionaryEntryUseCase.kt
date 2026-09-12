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

package ca.rmen.android.poetassistant.main.dictionaries.dictionary.usecases

import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryEntry
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryRepository
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.WordNotFoundException
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.WordSimilarityCalculator
import javax.inject.Inject

/**
 * Use case for looking up a dictionary entry.
 * Orchestrates word lookup: tries exact match, then falls back to fuzzy matching
 * via WordSimilarityCalculator.
 *
 * @param repository The dictionary repository for data access.
 */
class LookupDictionaryEntryUseCase @Inject constructor(
    private val repository: DictionaryRepository,
) {
    private val wordSimilarityCalculator = WordSimilarityCalculator()

    /**
     * Looks up a word in the dictionary, falling back to fuzzy matching if not found.
     *
     * @param word The word to look up.
     * @return The DictionaryEntry for the word (exact match or fuzzy match).
     * @throws WordNotFoundException if the word is not found and no fuzzy match exists.
     */
    suspend operator fun invoke(word: String): DictionaryEntry {
        try {
            return repository.lookup(word)
        } catch (e: WordNotFoundException) {
            // Fall back to fuzzy matching
            val stem = wordSimilarityCalculator.stem(word)
            val candidates = repository.getWordsByStem(stem)
            val bestMatch = wordSimilarityCalculator.findBestMatch(word, candidates)
            if (bestMatch != null) {
                return repository.lookup(bestMatch)
            }
            throw e
        }
    }
}
