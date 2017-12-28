/*
 * Copyright (c) 2016 = 2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.reader

import android.content.Context
import android.net.Uri
import android.preference.PreferenceManager
import android.util.Log
import ca.rmen.android.poetassistant.Constants

class PoemPrefs(context: Context) {
    companion object {
        private val TAG = Constants.TAG + PoemPrefs::class.java.simpleName
        private const val PREF_POEM_TEXT = "poem_text"
        private const val PREF_POEM_URI = "poem_uri"
        private const val PREF_POEM_NAME = "poem_name"
    }

    private val mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    fun hasTempPoem() = !mSharedPreferences.contains(PREF_POEM_URI) && mSharedPreferences.contains(PREF_POEM_TEXT)

    fun getTempPoem() : String? = mSharedPreferences.getString(PREF_POEM_TEXT, null)

    fun hasSavedPoem() = mSharedPreferences.contains(PREF_POEM_URI)

    fun setSavedPoem(poemFile: PoemFile) {
        Log.v(TAG, "setSavedPoem $poemFile")
        val editor = mSharedPreferences.edit()
        if (poemFile.uri != null) {
            editor.putString(PREF_POEM_URI, poemFile.uri.toString())
        } else {
            editor.remove(PREF_POEM_URI)
        }
        editor.putString(PREF_POEM_TEXT, poemFile.text)
        editor.putString(PREF_POEM_NAME, poemFile.name)
        editor.apply()
    }

    fun updatePoemText(text: String) {
        Log.v(TAG, "updatePoemText $text")
        mSharedPreferences.edit().putString(PREF_POEM_TEXT, text).apply()
    }

    fun getSavedPoem() : PoemFile? {
        val uri = mSharedPreferences.getString(PREF_POEM_URI, null)
        if (uri != null) {
            val text = mSharedPreferences.getString(PREF_POEM_TEXT, null)
            val name = mSharedPreferences.getString(PREF_POEM_NAME, null)
            return PoemFile(Uri.parse(uri), name, text)
        }
        return null
    }

    fun clear() {
        Log.v(TAG, "clear")
        mSharedPreferences.edit()
                .remove(PREF_POEM_TEXT)
                .remove(PREF_POEM_URI)
                .remove(PREF_POEM_NAME)
                .apply()

    }

}
