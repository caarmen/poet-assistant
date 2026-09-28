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

package ca.rmen.android.poetassistant.main.dictionaries.thesaurus

/**
 * One item of the thesaurus result list.
 */
sealed class ThesaurusListItem {

    /**
     * A word type heading, e.g. "noun".
     */
    data class Heading(val text: String) : ThesaurusListItem()

    /**
     * A section heading, e.g. "Synonyms".
     */
    data class Subheading(val text: String) : ThesaurusListItem()

    /**
     * A result word.
     */
    data class Word(val word: String, val isFavorite: Boolean) : ThesaurusListItem()
}
