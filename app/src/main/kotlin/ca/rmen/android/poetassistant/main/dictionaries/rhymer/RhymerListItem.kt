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

package ca.rmen.android.poetassistant.main.dictionaries.rhymer

/**
 * One item of the rhymer result list.
 */
sealed class RhymerListItem {

    /**
     * A pronunciation-variant heading, e.g. "dove (2)". Only shown when
     * the word has more than one pronunciation variant.
     */
    data class VariantHeading(val text: String) : RhymerListItem()

    /**
     * A section title: "Favorites:", "Strict rhyme matches:",
     * "Last 3 syllables:", "Last 2 syllables:", "Last syllable:".
     */
    data class SectionHeading(val text: String) : RhymerListItem()

    /**
     * The "Limited to 500 matches" notice, appended after a section once
     * the result list reaches the limit.
     */
    data class MaxResultsNotice(val text: String) : RhymerListItem()

    /**
     * A result word.
     */
    data class Word(val word: String, val isFavorite: Boolean, val hasDefinition: Boolean) : RhymerListItem()
}
