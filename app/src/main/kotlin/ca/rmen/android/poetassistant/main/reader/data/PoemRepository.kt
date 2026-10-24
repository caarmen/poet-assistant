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

package ca.rmen.android.poetassistant.main.reader.data

import kotlinx.coroutines.flow.StateFlow

/**
 * Repository for poem persistence.
 * The SharedPreferences keys are stable across app versions, so a
 * user's poem survives app updates.
 */
interface PoemRepository {

    /**
     * The current poem, if any: a persisted poem when a file backs it,
     * a temp poem when only the text is stored.
     */
    val poem: StateFlow<Poem?>

    /**
     * Persist the given poem as the current poem.
     */
    fun setPoem(poem: Poem)

    /**
     * Persist the poem text, keeping the current file binding (if any).
     */
    fun updatePoemText(text: String)

    /**
     * Remove all stored poem data.
     */
    fun clear()
}
