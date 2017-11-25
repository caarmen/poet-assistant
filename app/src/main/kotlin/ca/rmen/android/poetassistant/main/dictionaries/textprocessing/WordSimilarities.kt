/*
 * Copyright (c) 2016 - 2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.dictionaries.textprocessing

import android.database.Cursor
import android.util.Log
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.main.dictionaries.EmbeddedDb
import ca.rmen.porterstemmer.PorterStemmer

class WordSimilarities {
    companion object {
        private val TAG = Constants.TAG + WordSimilarities::class.java.simpleName
    }

    fun findClosestWord(word: String, embeddedDb: EmbeddedDb): String? {
        val stem = PorterStemmer().stemWord(word)
        val projection = arrayOf("word")
        val selection = "stem=?"
        val selectionArgs = arrayOf(stem)
        val cursor = embeddedDb.query(true, "stems", projection, selection, selectionArgs, null, null)
        return findClosestWord(word, cursor)
    }

    /**
     * @param cursor the first column must contain the words we want to compare to word
     */
    private fun findClosestWord(word: String, cursor: Cursor?): String? {
        cursor?.use {
            var closestWord : String? = null
            var bestScore = 0
            while (cursor.moveToNext()) {
                val matchingWord = cursor.getString(0)
                val score = calculateSimilarityScore(word, matchingWord)
                if (score > bestScore) {
                    closestWord = matchingWord
                    bestScore = score
                }
            }
            Log.v(TAG, "Closest word to $word is $closestWord")
            return closestWord
        }
        return null
    }

    /**
     * For now, the score is simply the number of letters in common
     * between the two words, starting from the beginning.
     */
    private fun calculateSimilarityScore(word1 : String, word2 : String) : Int {
        val length = Math.min(word1.length, word2.length)
        return (0 until length).firstOrNull { word1[it] != word2[it] }
                ?: length
    }
}
