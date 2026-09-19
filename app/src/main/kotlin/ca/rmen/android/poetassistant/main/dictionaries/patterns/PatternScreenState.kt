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

/**
 * Sealed class representing the UI state of the Pattern screen.
 */
sealed class PatternScreenState {

    /**
     * Idle state when no pattern search has been performed.
     */
    object Idle : PatternScreenState()

    /**
     * Success state with pattern search results.
     *
     * @param query The pattern that was searched for.
     * @param entries The list of matching pattern entries, sorted with favorites first.
     * @param isCapped Whether the results were capped at the maximum limit.
     */
    data class Success(
        val query: String,
        val entries: List<PatternEntry>,
        val isCapped: Boolean = false
    ) : PatternScreenState()

    /**
     * NotFound state when no words match the pattern.
     *
     * @param query The pattern that was searched for.
     */
    data class NotFound(
        val query: String
    ) : PatternScreenState()
}
