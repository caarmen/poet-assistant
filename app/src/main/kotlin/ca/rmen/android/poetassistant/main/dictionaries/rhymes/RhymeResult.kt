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
 * The rhymes of one pronunciation variant of a word.
 *
 * @param variantNumber The pronunciation variant, 0-based.
 * @param strictRhymes Words matching the stressed syllable.
 * @param oneSyllableRhymes Words matching the last syllable.
 * @param twoSyllableRhymes Words matching the last two syllables.
 * @param threeSyllableRhymes Words matching the last three syllables.
 */
data class RhymeResult(
    val variantNumber: Int,
    val strictRhymes: List<String>,
    val oneSyllableRhymes: List<String>,
    val twoSyllableRhymes: List<String>,
    val threeSyllableRhymes: List<String>,
)
