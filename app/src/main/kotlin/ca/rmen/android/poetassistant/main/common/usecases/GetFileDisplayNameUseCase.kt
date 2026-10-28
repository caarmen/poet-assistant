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

package ca.rmen.android.poetassistant.main.common.usecases

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import ca.rmen.android.poetassistant.di.IODispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for the display name of a file, when the file provider
 * exposes one.
 *
 * @param context The application context.
 * @param ioDispatcher The coroutine dispatcher for the provider query.
 */
class GetFileDisplayNameUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    /**
     * The display name of the file at the given uri.
     *
     * @param uri The uri of the file, or null for no file.
     * @return the display name, or null when there is no file or the
     * provider does not expose a display name.
     */
    suspend operator fun invoke(uri: Uri?): String? = withContext(ioDispatcher) {
        uri?.let { displayNameUri ->
            context.contentResolver.query(displayNameUri, null, null, null, null)?.use { cursor ->
                val displayNameColumn = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameColumn >= 0 && cursor.moveToFirst()) {
                    cursor.getString(displayNameColumn)
                } else {
                    null
                }
            }
        }
    }
}
