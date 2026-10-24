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

package ca.rmen.android.poetassistant.main.reader.usecases

import ca.rmen.android.poetassistant.main.reader.WordCount
import javax.inject.Inject

/**
 * Use case counting the words and characters of a poem.
 * Hyphens, apostrophes and underscores do not split words
 * (good-hearted, don't, variable_name are one word each).
 */
class GetWordCountUseCase @Inject constructor() {

    private companion object {
        private const val NON_SPLITTING_PUNCTUATION = "-'’_"

        @Suppress("Annotator")
        private val REGEX_STRIP by lazy { Regex("[$NON_SPLITTING_PUNCTUATION]") }

        @Suppress("Annotator")
        private val REGEX_SPLIT by lazy { Regex("[^$NON_SPLITTING_PUNCTUATION\\p{L}0-9]") }
    }

    /**
     * Counts the words and characters of the given text.
     *
     * @param text The poem text.
     * @return the word and character counts, or null for a text with
     * zero words (the count is hidden).
     */
    operator fun invoke(text: String?): WordCount? {
        if (text.isNullOrEmpty()) return null
        val words = text
            .replace(REGEX_STRIP, "")
            .split(REGEX_SPLIT)
            .count { it.isNotEmpty() }
        if (words == 0) return null
        return WordCount(words, text.length)
    }
}
