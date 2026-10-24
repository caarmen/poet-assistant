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

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.di.IODispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [PoemFileRepository] reading and writing poem files through the
 * [Context.contentResolver].
 */
@Singleton
class ContentResolverPoemFileRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : PoemFileRepository {

    companion object {
        private val TAG = Constants.TAG + ContentResolverPoemFileRepository::class.java.simpleName
    }

    override suspend fun read(uri: Uri): Poem.PersistedPoem? {
        Log.d(TAG, "read(uri=$uri)")
        return try {
            withContext(ioDispatcher) {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                    val text = it.readText()
                    val displayName = readDisplayName(uri)
                    Poem.PersistedPoem(uri, displayName, text)
                }
            }
        } catch (throwable: Throwable) {
            Log.w(TAG, "Couldn't open file", throwable)
            null
        }
    }

    override suspend fun save(uri: Uri, text: String): Poem.PersistedPoem? {
        Log.d(TAG, "save: uri=$uri, text=$text")
        return try {
            withContext(ioDispatcher) {
                context.contentResolver.openOutputStream(uri, "w")?.bufferedWriter()?.use {
                    it.write(text)
                    val displayName = readDisplayName(uri)
                    Poem.PersistedPoem(uri, displayName, text)
                }
            }
        } catch (throwable: Throwable) {
            Log.v(TAG, "Couldn't save file", throwable)
            null
        }
    }

    private fun readDisplayName(uri: Uri?): String? {
        uri?.let { displayNameUri ->
            val cursor = context.contentResolver.query(displayNameUri, null, null, null, null)
            cursor?.use {
                if (cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        return null
    }
}
