/*
 * Copyright (c) 2026-present Carmen Alvarez
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

import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryRepository
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.InterestingWords
import javax.inject.Inject

/**
 * Returns a random word from the interesting-words pool.
 *
 * The repository performs its query on the IO dispatcher.
 *
 * @param dictionaryRepository The dictionary repository for candidate words.
 */
class GetRandomWordUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
) {

    /**
     * Picks a random word from the interesting-words pool.
     *
     * The pick is uniform over the pool and fresh on each call: no seed.
     * Do not use the word-of-the-day pick here: seeding is the
     * word-of-the-day-specific concept, and this feature must stay
     * unseeded.
     *
     * @return A random word, or null if the dictionary DB is not loaded
     * yet.
     */
    suspend operator fun invoke(): String? {
        val candidates = dictionaryRepository.getWordsInFrequencyRange(
            InterestingWords.MIN_INTERESTING_FREQUENCY,
            InterestingWords.MAX_INTERESTING_FREQUENCY
        )
        if (candidates.isEmpty()) return null
        return candidates.random()
    }
}
