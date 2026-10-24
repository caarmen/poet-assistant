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
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import ca.rmen.android.poetassistant.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [PoemRepository] backed by SharedPreferences.
 * Uses the same SharedPreferences instance and the same keys as the
 * legacy PoemPrefs: an existing user's poem survives the migration.
 */
@Singleton
class SharedPrefsPoemRepository @Inject constructor(
    @ApplicationContext context: Context,
) : PoemRepository {

    companion object {
        private val TAG = Constants.TAG + SharedPrefsPoemRepository::class.java.simpleName
        private const val PREF_POEM_TEXT = "poem_text"
        private const val PREF_POEM_URI = "poem_uri"
        private const val PREF_POEM_NAME = "poem_name"
    }

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    /**
     * Write methods assign into this flow before returning, so that
     * [savedPoem].value is always fresh for synchronous reads.
     */
    override val savedPoem: StateFlow<Poem?> field = MutableStateFlow<Poem?>(readSavedPoem())

    private val prefsListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            Log.v(TAG, "onSharedPreferenceChanged: key=$key")
            val newPoem = readSavedPoem()
            // Only notify actual changes, to avoid redundant emissions
            // (the legacy reader fragment invalidates its options menu on
            // every poemFile emission).
            if (savedPoem.value != newPoem) {
                savedPoem.value = newPoem
            }
        }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    override fun getTempPoemText(): String? = sharedPreferences.getString(PREF_POEM_TEXT, null)

    override fun setPoem(poem: Poem) {
        Log.v(TAG, "setPoem $poem")
        sharedPreferences.edit {
            if (poem.uri != null) {
                putString(PREF_POEM_URI, poem.uri.toString())
            } else {
                remove(PREF_POEM_URI)
            }
            putString(PREF_POEM_TEXT, poem.text)
            putString(PREF_POEM_NAME, poem.name)
        }
        savedPoem.value = readSavedPoem()
    }

    override fun updatePoemText(text: String) {
        Log.v(TAG, "updatePoemText $text")
        sharedPreferences.edit { putString(PREF_POEM_TEXT, text) }
        savedPoem.value = readSavedPoem()
    }

    override fun clear() {
        Log.v(TAG, "clear")
        sharedPreferences.edit {
            remove(PREF_POEM_TEXT)
                .remove(PREF_POEM_URI)
                .remove(PREF_POEM_NAME)
        }
        savedPoem.value = readSavedPoem()
    }

    private fun readSavedPoem(): Poem? {
        val uri = sharedPreferences.getString(PREF_POEM_URI, null)
        if (uri != null) {
            val text = sharedPreferences.getString(PREF_POEM_TEXT, null)
            val name = sharedPreferences.getString(PREF_POEM_NAME, null)
            return Poem(uri.toUri(), name, text)
        }
        return null
    }
}
