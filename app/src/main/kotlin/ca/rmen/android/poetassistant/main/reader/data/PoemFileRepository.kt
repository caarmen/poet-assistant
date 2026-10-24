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
 * Repository for poem document I/O, through the ContentResolver.
 */
interface PoemFileRepository {

    /**
     * Read the poem at [uri].
     *
     * @param uri The uri of the poem file to read.
     * @return the persisted poem, or null on failure (unreadable,
     * missing).
     */
    suspend fun read(uri: Uri): Poem.PersistedPoem?

    /**
     * Write [text] to [uri].
     *
     * @param uri The uri of the poem file to write to.
     * @param text The poem text to write.
     * @return the persisted poem, or null on failure.
     */
    suspend fun save(uri: Uri, text: String): Poem.PersistedPoem?
}
