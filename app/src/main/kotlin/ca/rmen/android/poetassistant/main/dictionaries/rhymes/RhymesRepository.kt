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

package ca.rmen.android.poetassistant.main.dictionaries.rhymes

/**
 * Repository for rhyme lookups.
 */
interface RhymesRepository {

    /**
     * Words rhyming with [word] (1-3 syllable matches), in no particular order.
     *
     * When [allRhymesEnabled] is false, the word queries add the
     * `has_definition=1` filter.
     * [aoToAaMatchEnabled] applies the AO->AA phonetic substitution.
     * [aorToAoMatchEnabled] applies the AOR->AO phonetic substitution.
     *
     * @param word The word to find rhymes for.
     * @param allRhymesEnabled Whether to include words without definitions.
     * @param aoToAaMatchEnabled Whether to apply the AO->AA substitution.
     * @param aorToAoMatchEnabled Whether to apply the AOR->AO substitution.
     * @return the words rhyming with the given word.
     */
    suspend fun getFlatRhymes(
        word: String,
        allRhymesEnabled: Boolean,
        aoToAaMatchEnabled: Boolean,
        aorToAoMatchEnabled: Boolean,
    ): Set<String>
}
