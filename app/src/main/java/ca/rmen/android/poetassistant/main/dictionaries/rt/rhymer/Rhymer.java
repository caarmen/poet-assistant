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

package ca.rmen.android.poetassistant.main.dictionaries.rt.rhymer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import ca.rmen.android.poetassistant.main.dictionaries.DbUtil;
import ca.rmen.rhymer.WordVariant;

public class Rhymer extends ca.rmen.rhymer.Rhymer {
    private static final String DB_FILE = "rhymes.db";
    private final SQLiteDatabase mDb;

    private static Rhymer sInstance = null;

    public static synchronized Rhymer getInstance(Context context) {
        if (sInstance == null) sInstance = new Rhymer(context.getApplicationContext());
        return sInstance;
    }

    private Rhymer(Context context) {
        mDb = DbUtil.open(context, DB_FILE);
    }

    @Override
    protected List<WordVariant> getWordVariants(String word) {
        String[] projection = new String[]{"variant_number", "last_syllable", "last_two_syllables", "last_three_syllables"};
        String selection = "word=?";
        String[] selectionArgs = new String[]{word};
        Cursor cursor = mDb.query("word_variants", projection, selection, selectionArgs, null, null, null);
        List<WordVariant> result = new ArrayList<>();
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    int column = 0;
                    int variantNumber = cursor.getInt(column++);
                    String lastSyllable = cursor.getString(column++);
                    String lastTwoSyllables = cursor.getString(column++);
                    String lastThreeSyllables = cursor.getString(column);
                    WordVariant wordVariant = new WordVariant(variantNumber, lastSyllable, lastTwoSyllables, lastThreeSyllables);
                    result.add(wordVariant);
                }
            } finally {
                cursor.close();
            }
        }
        return result;
    }

    @Override
    protected SortedSet<String> getWordsWithLastSyllable(String syllable) {
        return lookupBySyllable(syllable, "last_syllable");
    }

    @Override
    protected SortedSet<String> getWordsWithLastTwoSyllables(String syllables) {
        return lookupBySyllable(syllables, "last_two_syllables");
    }

    @Override
    protected SortedSet<String> getWordsWithLastThreeSyllables(String syllables) {
        return lookupBySyllable(syllables, "last_three_syllables");
    }

    private SortedSet<String> lookupBySyllable(String syllables, String columnName) {
        String[] projection = new String[]{"word"};
        String selection = columnName + "=?";
        String[] selectionArgs = new String[]{syllables};
        Cursor cursor = mDb.query("word_variants", projection, selection, selectionArgs, null, null, null);
        SortedSet<String> result = new TreeSet<>();
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String word = cursor.getString(0);
                    result.add(word);
                }
            } finally {
                cursor.close();
            }
        }
        return result;

    }
}
