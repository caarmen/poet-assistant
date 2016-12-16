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

package ca.rmen.android.poetassistant.main.dictionaries.dictionary;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.main.dictionaries.DbHelper;
import ca.rmen.android.poetassistant.main.dictionaries.Patterns;
import ca.rmen.android.poetassistant.main.dictionaries.textprocessing.WordSimilarities;

public class Dictionary {
    // When looking up random words, their "frequency" is a factor in the selection.
    // Words which are too frequent (a, the, why) are not interesting words.
    // Words which are too rare (aalto) are likely not interesting either.
    private static final int MIN_INTERESTING_FREQUENCY = 1500;
    private static final int MAX_INTERESTING_FREQUENCY = 25000;

    private static final int MAX_PREFIX_MATCHES = 10;

    private final DbHelper mDbHelper;

    @Inject
    public Dictionary (DbHelper dbHelper) {
        mDbHelper = dbHelper;
    }

    public boolean isLoaded() {
        return mDbHelper.isLoaded();
    }

    @NonNull
    DictionaryEntry lookup(String word) {

        String[] projection = new String[]{"part_of_speech", "definition"};
        String selection = "word=?";
        String[] selectionArgs = new String[]{word};
        String lookupWord = word;
        Cursor cursor = mDbHelper.query("dictionary", projection, selection, selectionArgs);

        if (cursor != null && cursor.getCount() == 0) {
            String closestWord = new WordSimilarities().findClosestWord(word, mDbHelper);
            if (closestWord != null) {
                lookupWord = closestWord;
                cursor.close();
                selectionArgs = new String[]{lookupWord};
                cursor = mDbHelper.query("dictionary", projection, selection, selectionArgs);
            }
        }
        if (cursor != null) {
            DictionaryEntry.DictionaryEntryDetails[] result = new DictionaryEntry.DictionaryEntryDetails[cursor.getCount()];
            try {
                while (cursor.moveToNext()) {
                    String partOfSpeech = cursor.getString(0);
                    String definition = cursor.getString(1);
                    result[cursor.getPosition()] = new DictionaryEntry.DictionaryEntryDetails(partOfSpeech, definition);
                }
                return new DictionaryEntry(lookupWord, result);
            } finally {
                cursor.close();
            }
        }
        return new DictionaryEntry(word, new DictionaryEntry.DictionaryEntryDetails[0]);
    }

    public
    @NonNull
    String[] findWordsByPattern(String pattern) {
        String[] projection = new String[]{"word"};
        String selection = "word LIKE ?";
        String[] selectionArgs = new String[]{pattern};
        String orderBy = "word";
        String limit = String.valueOf(Patterns.MAX_RESULTS);
        Cursor cursor = mDbHelper.query(true, "dictionary", projection, selection, selectionArgs, orderBy, limit);
        if (cursor != null) {
            try {
                if (cursor.getCount() > 0) {
                    String[] result = new String[cursor.getCount()];
                    while (cursor.moveToNext()) {
                        result[cursor.getPosition()] = cursor.getString(0);
                    }
                    return result;
                }
            } finally {
                cursor.close();
            }
        }
        return new String[0];
    }

    /**
     * @return at most limit words starting with the given prefix
     */
    public String[] findWordsWithPrefix(String prefix) {
        String[] projection = new String[]{"word"};
        String selection = "has_definition=1 AND word LIKE ?";
        String[] selectionArgs = new String[]{prefix + "%"};
        String orderBy = "word";
        Cursor cursor = mDbHelper.query(
                true,
                "word_variants", projection, selection, selectionArgs,
                orderBy, String.valueOf(MAX_PREFIX_MATCHES));
        if (cursor != null) {
            try {
                if (cursor.getCount() > 0) {
                    String[] result = new String[cursor.getCount()];
                    while (cursor.moveToNext()) {
                        result[cursor.getPosition()] = cursor.getString(0);
                    }
                    return result;
                }
            } finally {
                cursor.close();
            }
        }
        return new String[0];

    }

    @Nullable
    public DictionaryEntry getRandomEntry() {
        String[] projection = new String[]{"word"};
        String limit = "1";
        String selection = "google_ngram_frequency > ? AND google_ngram_frequency < ?";
        String orderBy = "RANDOM()";
        String[] args = new String[]
                {
                        String.valueOf(MIN_INTERESTING_FREQUENCY),
                        String.valueOf(MAX_INTERESTING_FREQUENCY)
                };
        Cursor cursor = mDbHelper.query(false, "stems", projection, selection, args,
                orderBy, limit);
        if (cursor != null) {
            String word = null;
            try {
                if (cursor.moveToNext()) {
                    word = cursor.getString(0);
                }
            } finally {
                cursor.close();
            }

            if (TextUtils.isEmpty(word)) return null;
            return lookup(word);
        }

        return null;
    }
}
