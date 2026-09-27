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

package ca.rmen.android.poetassistant.main.wotd

/**
 * Sealed class representing the UI state of the WOTD history screen.
 */
sealed class WotdScreenState {

    /**
     * Initial state, before the first history load completes.
     */
    object Idle : WotdScreenState()

    /**
     * History loaded. An empty entries list means the dictionary DB is not
     * loaded yet.
     *
     * @param entries The list of history entries, starting with today.
     */
    data class Success(val entries: List<WotdHistoryItem>) : WotdScreenState()
}
