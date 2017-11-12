/*
 * Copyright (c) 2016 - 2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.dictionaries.search

object Patterns {
    /**
     * USER_PATTERN_SYMBOLS are symbols the user can type for pattern matching.
     * These are mapped 1-to-1 to SQLITE_PATTERN_SYMBOLS which will be used in
     * the 'LIKE' clause.  We provide this mapping because ? and * are usually
     * easier for a user to type, than _ or %.
     */
    private val USER_PATTERN_SYMBOLS = arrayOf("?", "*")
    private val SQLITE_PATTERN_SYMBOLS = arrayOf("_", "%")

    /**
     * @return true if the given input contains symbols that can be used with pattern matching
     */
    fun isPattern(input: String): Boolean {
        USER_PATTERN_SYMBOLS.forEach { if (input.contains(it)) return true }
        return false
    }

    /**
     * @return a pattern string that can be used in an SQLite query.
     */
    fun convertForSqlite(input: String) : String {
        var result = input
        USER_PATTERN_SYMBOLS.forEachIndexed { index, s -> result = result.replace(s, SQLITE_PATTERN_SYMBOLS[index]) }
        return result
    }
}
