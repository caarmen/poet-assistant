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

/**
 * Repository interface defining dictionary data access.
 * Provides methods for looking up dictionary entries and finding words by stem.
 */
interface DictionaryRepository {

    /**
     * Looks up a word in the dictionary.
     *
     * @param word The word to look up.
     * @return The DictionaryEntry for the word.
     * @throws WordNotFoundException if the word is not found.
     */
    suspend fun lookup(word: String): DictionaryEntry

    /**
     * Gets words that share the same stem as the given word.
     *
     * @param stem The stem to search for.
     * @return List of words that share this stem.
     */
    suspend fun getWordsByStem(stem: String): List<String>
}
