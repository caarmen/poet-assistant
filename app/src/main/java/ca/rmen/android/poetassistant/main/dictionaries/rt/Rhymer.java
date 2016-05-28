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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ca.rmen.android.poetassistant.main.dictionaries.DbHelper;
import ca.rmen.rhymer.RhymeResult;
import ca.rmen.rhymer.WordVariant;

public class Rhymer extends ca.rmen.rhymer.Rhymer {
    private static final String DB_FILE = "rhymes";
    private static final int DB_VERSION = 2;
    private final DbHelper mDbHelper;

    private static Rhymer sInstance = null;

    public static synchronized Rhymer getInstance(Context context) {
        if (sInstance == null) sInstance = new Rhymer(context.getApplicationContext());
        return sInstance;
    }

    private Rhymer(Context context) {
        mDbHelper = new DbHelper(context, DB_FILE, DB_VERSION);
    }

    public boolean isLoaded() {
        return mDbHelper.getDb() != null;
    }

    @NonNull
    @Override
    protected List<WordVariant> getWordVariants(String word) {
        List<WordVariant> result = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getDb();
        if (db != null) {
            String[] projection = new String[]{"variant_number", "stress_syllables", "last_syllable", "last_two_syllables", "last_three_syllables"};
            String selection = "word=?";
            String[] selectionArgs = new String[]{word};
            Cursor cursor = db.query("word_variants", projection, selection, selectionArgs, null, null, null);
            if (cursor != null) {
                try {
                    while (cursor.moveToNext()) {
                        int column = 0;
                        int variantNumber = cursor.getInt(column++);
                        String lastStressSyllable = cursor.getString(column++);
                        String lastSyllable = cursor.getString(column++);
                        String lastTwoSyllables = cursor.getString(column++);
                        String lastThreeSyllables = cursor.getString(column);
                        WordVariant wordVariant = new WordVariant(variantNumber, lastStressSyllable, lastSyllable, lastTwoSyllables, lastThreeSyllables);
                        result.add(wordVariant);
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return result;
    }


    /**
     * @return the words which rhyme with the given word, in any order, matching one, two or three
     * syllables.
     */
    Set<String> getFlatRhymes(String word) {
        List<RhymeResult> rhymeResults = super.getRhymingWords(word);
        Set<String> flatRhymes = new HashSet<>();
        for (RhymeResult rhymeResult : rhymeResults) {
            Collections.addAll(flatRhymes, rhymeResult.strictRhymes);
            Collections.addAll(flatRhymes, rhymeResult.oneSyllableRhymes);
            Collections.addAll(flatRhymes, rhymeResult.twoSyllableRhymes);
            Collections.addAll(flatRhymes, rhymeResult.threeSyllableRhymes);
        }
        return flatRhymes;
    }

    @Override
    protected SortedSet<String> getWordsWithLastStressSyllable(String syllable) {
        return lookupBySyllable(syllable, "stress_syllables");
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

    @NonNull
    private SortedSet<String> lookupBySyllable(String syllables, String columnName) {
        SortedSet<String> result = new TreeSet<>();
        SQLiteDatabase db = mDbHelper.getDb();
        if (db != null) {
            String[] projection = new String[]{"word"};
            String selection = columnName + "=?";
            String[] selectionArgs = new String[]{syllables};
            Cursor cursor = db.query("word_variants", projection, selection, selectionArgs, null, null, null);
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
        }
        return result;

    }
}
