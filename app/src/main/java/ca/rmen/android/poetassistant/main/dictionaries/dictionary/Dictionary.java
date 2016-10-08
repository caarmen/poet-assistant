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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import ca.rmen.android.poetassistant.main.dictionaries.DbHelper;
import ca.rmen.android.poetassistant.main.dictionaries.Patterns;
import ca.rmen.android.poetassistant.main.dictionaries.textprocessing.WordSimilarities;

public class Dictionary {
    private static final String DB_FILE = "dictionary";
    private static final int DB_VERSION = 2;

    private static Dictionary sInstance;

    private final DbHelper mDbHelper;

    public static synchronized Dictionary getInstance(Context context) {
        if (sInstance == null) sInstance = new Dictionary(context);
        return sInstance;
    }

    private Dictionary (Context context) {
        mDbHelper = new DbHelper(context, DB_FILE, DB_VERSION);
    }

    public boolean isLoaded() {
        return mDbHelper.getDb() != null;
    }

    @NonNull
    DictionaryEntry lookup(String word) {
        SQLiteDatabase db = mDbHelper.getDb();
        if (db != null) {

            String[] projection = new String[]{"part_of_speech", "definition"};
            String selection = "word=?";
            String[] selectionArgs = new String[]{word};
            String lookupWord = word;
            Cursor cursor = db.query("dictionary", projection, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.getCount() == 0) {
                String closestWord = new WordSimilarities().findClosestWord(word, db, "stems", "word", "stem");
                if (closestWord != null) {
                    lookupWord = closestWord;
                    cursor.close();
                    selectionArgs = new String[]{lookupWord};
                    cursor = db.query("dictionary", projection, selection, selectionArgs, null, null, null);
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
        }
        return new DictionaryEntry(word, new DictionaryEntry.DictionaryEntryDetails[0]);
    }

    public
    @NonNull
    String[] findWordsByPattern(String pattern) {
        SQLiteDatabase db = mDbHelper.getDb();
        if (db != null) {
            String[] projection = new String[]{"word"};
            String selection = "word LIKE ?";
            String[] selectionArgs = new String[]{pattern};
            String orderBy = "word";
            String limit = String.valueOf(Patterns.MAX_RESULTS);
            Cursor cursor = db.query(true, "dictionary", projection, selection, selectionArgs, null, null, orderBy, limit);
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
        }
        return new String[0];
    }

    @Nullable
    public DictionaryEntry getRandomEntry() {
        SQLiteDatabase db = mDbHelper.getDb();
        if (db != null) {
            String[] projection = new String[]{"word"};
            String orderBy = "RANDOM()";
            String limit = "1";
            Cursor cursor = db.query(false, "dictionary", projection, null, null, null, null,
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
        }

        return null;
    }
}
