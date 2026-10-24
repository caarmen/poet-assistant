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
 * Sealed class representing the UI state of the rhymer screen.
 */
sealed class RhymerScreenState {

    /**
     * Idle state when no word has been searched.
     */
    object Idle : RhymerScreenState()

    /**
     * Nothing found for the query.
     *
     * @param word The query — the rhymer has no closest-word fallback.
     * @param isFavorite Whether the word is a favorite.
     * @param synonymFilter The active synonym filter, if any.
     */
    data class NotFound(
        val word: String,
        val isFavorite: Boolean,
        val synonymFilter: String?,
    ) : RhymerScreenState()

    /**
     * Results found.
     *
     * @param word The query.
     * @param isFavorite Whether the word is a favorite.
     * @param synonymFilter The active synonym filter, if any.
     * @param entries The result list items.
     */
    data class Success(
        val word: String,
        val isFavorite: Boolean,
        val synonymFilter: String?,
        val entries: List<RhymerListItem>,
    ) : RhymerScreenState()
}
