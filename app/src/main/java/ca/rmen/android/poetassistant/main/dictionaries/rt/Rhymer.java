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

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.main.dictionaries.EmbeddedDb;
import ca.rmen.android.poetassistant.settings.SettingsPrefs;
import ca.rmen.rhymer.RhymeResult;
import ca.rmen.rhymer.WordVariant;

public class Rhymer extends ca.rmen.rhymer.Rhymer {
    private static final String TAG = Constants.TAG + Rhymer.class.getSimpleName();

    private final EmbeddedDb mEmbeddedDb;
    private final SettingsPrefs mPrefs;

    @Inject
    public Rhymer(EmbeddedDb embeddedDb, SettingsPrefs prefs) {
        mEmbeddedDb = embeddedDb;
        mPrefs = prefs;
    }

    public boolean isLoaded() {
        return mEmbeddedDb.isLoaded();
    }

    @NonNull
    @Override
    protected List<WordVariant> getWordVariants(String word) {
        List<WordVariant> result = new ArrayList<>();
        String[] projection = new String[]{"variant_number", "stress_syllables", "last_syllable", "last_two_syllables", "last_three_syllables"};
        String selection = "word=?";
        String[] selectionArgs = new String[]{word};
        Cursor cursor = mEmbeddedDb.query("word_variants", projection, selection, selectionArgs);
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

    /**
     * Of the given words, returns a set containing those which have a definition in the dictionary table.
     */
    Set<String> getWordsWithDefinitions(String[] words) {
        if (words.length == 0) return Collections.emptySet();
        Log.v(TAG, "getWordsWithDefinitions for " + words.length + " words");
        Set<String> result = new HashSet<>();
        String[] projection = new String[]{"word"};
        int queryCount = EmbeddedDb.getQueryCount(words.length);
        for (int i = 0; i < queryCount; i++) {
            String[] queryWords = EmbeddedDb.getArgsInQuery(words, i);
            Log.v(TAG, "getWordsWithDefinitions: query " + i + " has " + queryWords.length + " words");
            String selection = "word in " + buildInClause(queryWords.length) + " AND has_definition=1";
            Cursor cursor = mEmbeddedDb.query("word_variants", projection, selection, queryWords);
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


    private static String buildInClause(int size) {
        StringBuilder builder = new StringBuilder(size * 2 + 1);
        builder.append('(');
        for (int i = 0; i < size; i++) {
            builder.append('?');
            if (i != size - 1) builder.append(',');
        }
        builder.append(')');
        return builder.toString();
    }

    @NonNull
    private SortedSet<String> lookupBySyllable(String syllables, String columnName) {
        SortedSet<String> result = new TreeSet<>();
        String[] projection = new String[]{"word"};
        String selection = columnName + "=?";
        if (!mPrefs.getIsAllRhymesEnabled()) {
            selection += "AND has_definition=1";
        }
        String[] selectionArgs = new String[]{syllables};
        Cursor cursor = mEmbeddedDb.query("word_variants", projection, selection, selectionArgs);
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
