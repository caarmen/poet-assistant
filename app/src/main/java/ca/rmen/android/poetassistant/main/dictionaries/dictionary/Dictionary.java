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

import ca.rmen.android.poetassistant.main.dictionaries.DbUtil;

public class Dictionary {
    private static final String DB_FILE = "dictionary.db";

    private static Dictionary sInstance;

    private final SQLiteDatabase mDb;

    public static synchronized Dictionary getInstance(Context context) {
        if (sInstance == null) sInstance = new Dictionary(context);
        return sInstance;
    }

    private Dictionary(Context context) {
        mDb = DbUtil.open(context, DB_FILE);
    }

    public DictionaryEntry[] getEntries(String word) {
        String[] projection = new String[]{"part_of_speech", "definition"};
        String selection = "word=?";
        String[] selectionArgs = new String[]{word};
        Cursor cursor = mDb.query("dictionary", projection, selection, selectionArgs, null, null, null);
        if (cursor != null) {
            DictionaryEntry[] result = new DictionaryEntry[cursor.getCount()];
            try {
                while (cursor.moveToNext()) {
                    String partOfSpeech = cursor.getString(0);
                    String definition = cursor.getString(1);
                    result[cursor.getPosition()] = new DictionaryEntry(partOfSpeech, definition);
                }
                return result;
            } finally {
                cursor.close();
            }
        }
        return new DictionaryEntry[0];
    }
}
