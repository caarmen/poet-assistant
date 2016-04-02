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
import android.text.TextUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ca.rmen.android.poetassistant.main.dictionaries.DbUtil;
import ca.rmen.android.poetassistant.main.dictionaries.textprocessing.WordSimilarities;

public class Thesaurus {
    private static final String DB_FILE = "thesaurus";
    private static final int DB_VERSION = 2;

    private static Thesaurus sInstance;

    private final SQLiteDatabase mDb;

    public enum WordType {
        @SuppressWarnings("unused")ADJ,
        @SuppressWarnings("unused")ADV,
        @SuppressWarnings("unused")NOUN,
        @SuppressWarnings("unused")VERB,
        @SuppressWarnings("unused")UNKNOWN
    }


    public static class ThesaurusResults {
        public final String matchedWord;
        public final ThesaurusEntry[] entries;

        public ThesaurusResults(String matchedWord, ThesaurusEntry[] entries) {
            this.matchedWord = matchedWord;
            this.entries = entries;
        }
    }
    public static class ThesaurusEntry {
        public final WordType wordType;
        public final String[] synonyms;
        public final String[] antonyms;

        public ThesaurusEntry(WordType wordType, String[] synonyms, String[] antonyms) {
            this.wordType = wordType;
            this.synonyms = synonyms;
            this.antonyms = antonyms;
        }
    }

    public static synchronized Thesaurus getInstance(Context context) {
        if (sInstance == null) sInstance = new Thesaurus(context);
        return sInstance;
    }

    private Thesaurus(Context context) {
        mDb = DbUtil.open(context, DB_FILE, DB_VERSION);
    }

    public ThesaurusResults getEntries(String word) {
        String[] projection = new String[]{"word_type", "synonyms", "antonyms"};
        String selection = "word=?";
        String[] selectionArgs = new String[]{word};
        String lookupWord = word;
        Cursor cursor = mDb.query("thesaurus", projection, selection, selectionArgs, null, null, null);


        if (cursor != null && cursor.getCount() == 0) {
            String closestWord = new WordSimilarities().findClosestWord(word, mDb, "thesaurus", "word", "stem");
            if (closestWord != null) {
                lookupWord = closestWord;
                cursor.close();
                selectionArgs = new String[]{lookupWord};
                cursor = mDb.query("thesaurus", projection, selection, selectionArgs, null, null, null);
            }
        }

        if (cursor != null) {
            ThesaurusEntry[] result = new ThesaurusEntry[cursor.getCount()];
            try {
                while (cursor.moveToNext()) {
                    WordType wordType = WordType.valueOf(cursor.getString(0));
                    String synonymsList = cursor.getString(1);
                    String antonymsList = cursor.getString(2);
                    String[] synonyms = split(synonymsList);
                    String[] antonyms = split(antonymsList);
                    result[cursor.getPosition()] = new ThesaurusEntry(wordType, synonyms, antonyms);
                }
                return new ThesaurusResults(lookupWord, result);
            } finally {
                cursor.close();
            }
        }
        return new ThesaurusResults(word, new ThesaurusEntry[0]);
    }

    /**
     * @return the synonyms of the given word, in any order.
     */
    public Set<String> getFlatSynonyms(String word) {
        Set<String> flatSynonyms = new HashSet<>();

        String[] projection = new String[]{"synonyms"};
        String selection = "word=?";
        String[] selectionArgs = new String[]{word};
        Cursor cursor = mDb.query("thesaurus", projection, selection, selectionArgs, null, null, null);
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
        return flatSynonyms;
    }

    private static String[] split(String string) {
        if (TextUtils.isEmpty(string)) return new String[0];
        return string.split(",");
    }


}
