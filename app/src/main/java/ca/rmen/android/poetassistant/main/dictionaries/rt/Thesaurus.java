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

package ca.rmen.android.poetassistant.main.dictionaries.rt;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ca.rmen.android.poetassistant.main.dictionaries.DbHelper;
import ca.rmen.android.poetassistant.main.dictionaries.textprocessing.WordSimilarities;

public class Thesaurus {
    private static final String DB_FILE = "thesaurus";
    private static final int DB_VERSION = 2;

    private static Thesaurus sInstance;
    private final DbHelper mDbHelper;

    public static synchronized Thesaurus getInstance(Context context) {
        if (sInstance == null) sInstance = new Thesaurus(context);
        return sInstance;
    }

    private Thesaurus(Context context) {
        mDbHelper = new DbHelper(context, DB_FILE, DB_VERSION);
    }

    public boolean isLoaded() {
        return mDbHelper.getDb() != null;
    }

    @NonNull
    public ThesaurusEntry lookup(String word) {
        SQLiteDatabase db = mDbHelper.getDb();
        if (db != null) {
            String[] projection = new String[]{"word_type", "synonyms", "antonyms"};
            String selection = "word=?";
            String[] selectionArgs = new String[]{word};
            String lookupWord = word;
            Cursor cursor = db.query("thesaurus", projection, selection, selectionArgs, null, null, null);


            if (cursor != null && cursor.getCount() == 0) {
                String closestWord = new WordSimilarities().findClosestWord(word, db, "thesaurus", "word", "stem");
                if (closestWord != null) {
                    lookupWord = closestWord;
                    cursor.close();
                    selectionArgs = new String[]{lookupWord};
                    cursor = db.query("thesaurus", projection, selection, selectionArgs, null, null, null);
                }
            }

            if (cursor != null) {
                ThesaurusEntry.ThesaurusEntryDetails[] result = new ThesaurusEntry.ThesaurusEntryDetails[cursor.getCount()];
                try {
                    while (cursor.moveToNext()) {
                        ThesaurusEntry.WordType wordType = ThesaurusEntry.WordType.valueOf(cursor.getString(0));
                        String synonymsList = cursor.getString(1);
                        String antonymsList = cursor.getString(2);
                        String[] synonyms = split(synonymsList);
                        String[] antonyms = split(antonymsList);
                        result[cursor.getPosition()] = new ThesaurusEntry.ThesaurusEntryDetails(wordType, synonyms, antonyms);
                    }
                    return new ThesaurusEntry(lookupWord, result);
                } finally {
                    cursor.close();
                }
            }
        }
        return new ThesaurusEntry(word, new ThesaurusEntry.ThesaurusEntryDetails[0]);
    }

    /**
     * @return the synonyms of the given word, in any order.
     */
    @NonNull
    public Set<String> getFlatSynonyms(String word) {
        Set<String> flatSynonyms = new HashSet<>();
        SQLiteDatabase db = mDbHelper.getDb();
        if (db != null) {

            String[] projection = new String[]{"synonyms"};
            String selection = "word=?";
            String[] selectionArgs = new String[]{word};
            Cursor cursor = db.query("thesaurus", projection, selection, selectionArgs, null, null, null);
            if (cursor != null) {
                try {
                    while (cursor.moveToNext()) {
                        String synonymsList = cursor.getString(0);
                        String[] synonyms = split(synonymsList);
                        Collections.addAll(flatSynonyms, synonyms);
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return flatSynonyms;
    }

    private static String[] split(String string) {
        if (TextUtils.isEmpty(string)) return new String[0];
        return string.split(",");
    }


}
