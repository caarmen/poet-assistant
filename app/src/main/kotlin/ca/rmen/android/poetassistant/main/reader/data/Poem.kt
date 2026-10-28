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

import android.net.Uri

/**
 * A poem: the text, and the shape it is stored in.
 *
 * There are two kinds of poems: a temp poem (typed, not saved to a
 * file) and a persisted poem (backed by a file). The distinction is
 * part of the type: a temp poem cannot carry a file uri or name.
 */
sealed interface Poem {

    /**
     * The poem text. Never null: a persisted poem whose stored text is
     * absent reads as an empty text.
     */
    val text: String

    /**
     * A temp poem: typed by the user, not backed by a file.
     */
    data class TempPoem(override val text: String) : Poem

    /**
     * A persisted poem: backed by a file.
     *
     * @param uri The uri of the file backing the poem.
     * @param name The display name of the file, if known. Null when
     * the file provider does not expose a display name.
     * @param text The poem text.
     */
    data class PersistedPoem(
        val uri: Uri,
        val name: String?,
        override val text: String,
    ) : Poem
}
