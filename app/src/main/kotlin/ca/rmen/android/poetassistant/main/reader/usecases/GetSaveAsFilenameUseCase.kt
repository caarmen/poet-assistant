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

package ca.rmen.android.poetassistant.main.reader.usecases

import android.util.Log
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.main.reader.data.Poem
import ca.rmen.android.poetassistant.main.reader.data.PoemRepository
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import javax.inject.Inject

/**
 * Use case for the suggested file name when saving the poem to a new
 * file: the saved poem's name when one exists, else a name generated
 * from the poem text.
 *
 * @param poemRepository Repository for the saved poem.
 */
class GetSaveAsFilenameUseCase @Inject constructor(
    private val poemRepository: PoemRepository,
) {

    /**
     * The suggested file name for the create-document picker.
     *
     * @param text The current poem text.
     * @return the suggested file name, or null for no suggestion.
     */
    operator fun invoke(text: String): String? {
        val savedPoem = poemRepository.poem.value as? Poem.PersistedPoem
        return savedPoem?.name ?: generateFileName(text)
    }

    companion object {
        private val TAG = Constants.TAG + GetSaveAsFilenameUseCase::class.java.simpleName

        /**
         * Generates a suggested filename based on the first few words
         * of the poem text.
         */
        private fun generateFileName(text: String): String? {
            val minLength = 8
            val maxLength = 16
            var textStart: String
            try {
                textStart = Pattern.compile("[^\\p{L}]+").matcher(text).replaceAll("-")
                textStart = textStart.replace(Regex("^-"), "")
            } catch (e: PatternSyntaxException) {
                // Not sure why \\p{IsAlphabetic} worked on unit tests but not on an android device.
                // \\p{L} worked on a couple of devices, but let's not take any chances.
                Log.v(TAG, "Couldn't generate file name for $text: ${e.message}", e)
                return null
            }
            textStart = textStart.substring(0, minOf(maxLength, textStart.length))
            var lastWordBegin = textStart.length
            for (i in textStart.length - 1 downTo minLength + 1) {
                if (!Character.isLetter(textStart[i])) {
                    lastWordBegin = i
                    break
                }
            }
            // replace trailing hyphen
            textStart = textStart.replace(Regex("-$"), "")
            // If there's nothing left, give up.
            if (textStart.isEmpty()) return null
            if (textStart.length <= minLength) return "$textStart.txt"
            lastWordBegin = minOf(lastWordBegin, textStart.length)
            textStart = textStart.substring(0, lastWordBegin) + ".txt"
            return textStart
        }
    }
}
