/*
 * Copyright (c) 2016 Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.dictionaries.textprocessing;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.porterstemmer.PorterStemmer;

public class WordSimilarities {

    private static final String TAG = Constants.TAG + WordSimilarities.class.getSimpleName();

    public String findClosestWord(String word,
                                  SQLiteDatabase db,
                                  String table,
                                  @SuppressWarnings("SameParameterValue") String wordColumn,
                                  @SuppressWarnings("SameParameterValue") String stemColumn) {
        String stem = new PorterStemmer().stemWord(word);
        if (!word.equals(stem)) {
            String[] projection = new String[]{wordColumn};
            String selection = stemColumn + "=?";
            String[] selectionArgs = new String[]{stem};
            Cursor cursor = db.query(true, table, projection, selection, selectionArgs, null, null, null, null);
            return new WordSimilarities().findClosestWord(word, cursor);
        }
        return null;
    }

    /**
     * @param cursor the first column must contain the words we want to compare to word
     */
    private String findClosestWord(String word, Cursor cursor) {
        if (cursor != null) {
            try {
                String closestWord = null;
                int bestScore = 0;
                while (cursor.moveToNext()) {
                    String matchingWord = cursor.getString(0);
                    int score = calculateSimilarityScore(word, matchingWord);
                    if (score > bestScore) {
                        closestWord = matchingWord;
                        bestScore = score;
                    }
                }
                Log.v(TAG, "Closest word to "+ word + " is " + closestWord);
                return closestWord;
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * For now, the score is simply the number of letters in common
     * between the two words, starting from the beginning.
     */
    private int calculateSimilarityScore(String word1, String word2) {
        int length = Math.min(word1.length(), word2.length());
        for (int i = 0; i < length; i++) {
            if (word1.charAt(i) != word2.charAt(i)) return i;
        }
        return length;
    }

}
