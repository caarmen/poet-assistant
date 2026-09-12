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

package ca.rmen.android.poetassistant.main.dictionaries.dictionary

import ca.rmen.porterstemmer.PorterStemmer

/**
 * Pure domain service for fuzzy matching.
 * Provides methods for stemming words and calculating similarity scores.
 * This is a pure utility class with no dependencies to mock.
 */
class WordSimilarityCalculator {

    /**
     * Stems a word using the Porter stemming algorithm.
     *
     * @param word The word to stem.
     * @return The stemmed version of the word.
     */
    fun stem(word: String): String = PorterStemmer().stemWord(word)

    /**
     * Calculates a similarity score between two strings.
     * The score is based on the number of matching characters from the start,
     * normalized to a value between 0.0 and 1.0.
     *
     * @param a The first string.
     * @param b The second string.
     * @return A similarity score between 0.0 (completely different) and 1.0 (identical).
     */
    fun similarityScore(a: String, b: String): Double {
        val matchingCount = a.zip(b).takeWhile { (char1, char2) -> char1 == char2 }.count()
        return matchingCount.toDouble() / maxOf(a.length, b.length)
    }

    /**
     * Finds the best matching word from a list of candidates for a target word.
     * Uses similarity scores to determine the best match.
     *
     * @param target The target word to match against.
     * @param candidates The list of candidate words to search through.
     * @return The best matching word, or null if no candidates are provided.
     */
    fun findBestMatch(target: String, candidates: List<String>): String? =
        candidates.maxByOrNull { similarityScore(target, it) }
}
