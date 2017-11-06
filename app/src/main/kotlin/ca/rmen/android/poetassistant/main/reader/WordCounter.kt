/*
 * Copyright (c) 2017 Carmen Alvarez
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
import android.text.TextUtils
import ca.rmen.android.poetassistant.R

object WordCounter {
    // The following will each be considered as one word: good-hearted, don't, variable_name
    private const val NON_SPLITTING_PUNCTUATION = "-'’_"

    private val REGEX_STRIP by lazy { Regex("[$NON_SPLITTING_PUNCTUATION]") }
    private val REGEX_SPLIT by lazy { Regex("[^$NON_SPLITTING_PUNCTUATION\\p{L}0-9]") }

    fun countWords(text: String?): Int {
        if (TextUtils.isEmpty(text)) return 0
        val tokens = text!!
                .replace(REGEX_STRIP, "")
                .split(REGEX_SPLIT)
                .filterNot({ TextUtils.isEmpty(it) })
        return tokens.size
    }

    fun countCharacters(text: String?) : Int {
        if (TextUtils.isEmpty(text)) return 0
        return text!!.length
    }

    fun getWordCountText(context: Context, text: String?): String? {
        val words = countWords(text)
        val characters = countCharacters(text)
        if (words == 0) return null
        return context.getString(R.string.reader_word_char_count,
                context.resources.getQuantityString(R.plurals.reader_word_count, words, words),
                context.resources.getQuantityString(R.plurals.reader_char_count, characters, characters))
    }
}

