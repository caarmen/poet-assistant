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

package ca.rmen.android.poetassistant.main.dictionaries.patterns

import javax.inject.Inject

/**
 * Pure utility for pattern matching operations.
 * Replaces the legacy Patterns object.
 *
 * User pattern symbols (? and *) are mapped to SQLite pattern symbols (_ and %) for use in
 * SQLite LIKE clauses. This mapping makes patterns easier for users to type.
 */
class PatternMatcher @Inject constructor() {

    companion object{
        /**
         * Symbols that users can type for pattern matching.
         */
        private val USER_PATTERN_SYMBOLS = arrayOf("?", "*")

        /**
         * Corresponding SQLite pattern symbols for the LIKE clause.
         */
        private val SQLITE_PATTERN_SYMBOLS = arrayOf("_", "%")
    }

    /**
     * Converts user-friendly pattern symbols to SQLite pattern symbols.
     * User symbols ? and * are replaced with _ and % respectively.
     *
     * @param input The pattern string with user symbols (? and *).
     * @return A pattern string suitable for SQLite LIKE clauses (using _ and %).
     */
    fun convertForSqlite(input: String): String {
        var result = input
        USER_PATTERN_SYMBOLS.forEachIndexed { index, s ->
            result = result.replace(s, SQLITE_PATTERN_SYMBOLS[index])
        }
        return result
    }
}
