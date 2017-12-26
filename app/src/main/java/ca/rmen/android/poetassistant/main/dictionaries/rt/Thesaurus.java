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
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.main.dictionaries.EmbeddedDb;
import ca.rmen.android.poetassistant.main.dictionaries.textprocessing.WordSimilarities;
import io.reactivex.Observable;

public class Thesaurus {

    private static final String TAG = Constants.TAG + Thesaurus.class.getSimpleName();

    private final EmbeddedDb mEmbeddedDb;

    private enum RelationType {
        SYNONYM("synonyms"),
        ANTONYM("antonyms");
        public final String columnName;

        RelationType(String columnName) {
            this.columnName = columnName;
        }
    }

    @Inject
    public Thesaurus(EmbeddedDb embeddedDb) {
        mEmbeddedDb = embeddedDb;
    }

    public boolean isLoaded() {
        return mEmbeddedDb.isLoaded();
    }

    @NonNull
    ThesaurusEntry lookup(String word, boolean includeReverseLookup) {
        return lookup(word, EnumSet.allOf(RelationType.class), includeReverseLookup);
    }

    @NonNull
    private ThesaurusEntry lookup(String word, Set<RelationType> relationTypes, boolean includeReverseLookup) {
        String[] projection = new String[]{"word_type", "synonyms", "antonyms"};
        String selection = "word=?";
        String[] selectionArgs = new String[]{word};
        String lookupWord = word;
        Cursor cursor = mEmbeddedDb.query("thesaurus", projection, selection, selectionArgs);

        if (cursor != null && cursor.getCount() == 0) {
            String closestWord = new WordSimilarities().findClosestWord(word, mEmbeddedDb);
            if (closestWord != null) {
                lookupWord = closestWord;
                cursor.close();
                selectionArgs = new String[]{lookupWord};
                cursor = mEmbeddedDb.query("thesaurus", projection, selection, selectionArgs);
            }
        }

        if (cursor != null) {
            List<ThesaurusEntry.ThesaurusEntryDetails> result = new ArrayList<>();
            try {
                List<String> forwardSynonyms = new ArrayList<>();
                List<String> forwardAntonyms = new ArrayList<>();
                while (cursor.moveToNext()) {
                    ThesaurusEntry.WordType wordType = ThesaurusEntry.WordType.valueOf(cursor.getString(0));
                    List<String> synonyms = new ArrayList<>();
                    List<String> antonyms = new ArrayList<>();
                    if (relationTypes.contains(RelationType.SYNONYM)) {
                        String synonymsList = cursor.getString(1);
                        synonyms = split(synonymsList);
                        forwardSynonyms.addAll(synonyms);
                    }
                    if (relationTypes.contains(RelationType.ANTONYM)) {
                        String antonymsList = cursor.getString(2);
                        antonyms = split(antonymsList);
                        forwardAntonyms.addAll(antonyms);
                    }
                    result.add(new ThesaurusEntry.ThesaurusEntryDetails(wordType, synonyms, antonyms));
                }
                if (includeReverseLookup) {
                    if (relationTypes.contains(RelationType.SYNONYM)) {
                        result.addAll(lookupReverseRelatedWords(RelationType.SYNONYM, lookupWord, forwardSynonyms));
                    }
                    if (relationTypes.contains(RelationType.ANTONYM)) {
                        result.addAll(lookupReverseRelatedWords(RelationType.ANTONYM, lookupWord, forwardAntonyms));
                    }
                }
                return new ThesaurusEntry(lookupWord, result);

            } finally {
                cursor.close();
            }
        }
        return new ThesaurusEntry(word, Collections.emptyList());
    }

    /**
     * @param relationType        whether we want to look up synonyms or antonyms
     * @param word                the word for which we want to find synonyms or antonyms
     * @param excludeRelatedWords words from this collection will be excluded from the result
     * @return words which have an entry in the thesaurus table containing the given word as a synonym or antonym.
     */
    private List<ThesaurusEntry.ThesaurusEntryDetails> lookupReverseRelatedWords(RelationType relationType, String word, Collection<String> excludeRelatedWords) {
        Log.v(TAG, "lookupReverseRelatedWords: relationType = " + relationType + ", word = " + word + ", exclude " + excludeRelatedWords);
        String[] projection = new String[]{"word", "word_type"};
        String selection = String.format(Locale.US, "(%s = ? OR %s LIKE ? OR %s LIKE ? OR %s LIKE ?) ",
                relationType.columnName, relationType.columnName, relationType.columnName, relationType.columnName);
        String[] selectionArgs = new String[4 + excludeRelatedWords.size()];
        int i = 0;
        selectionArgs[i++] = word; // only relatedWord
        selectionArgs[i++] = String.format(Locale.US, "%s,%%", word); // first relatedWord
        selectionArgs[i++] = String.format(Locale.US, "%%,%s", word); // last relatedWord
        selectionArgs[i++] = String.format(Locale.US, "%%,%s,%%", word); // somewhere in the list of relatedWords
        if (!excludeRelatedWords.isEmpty()) {
            selection += " AND word NOT IN " + EmbeddedDb.Companion.buildInClause(excludeRelatedWords.size());
            for (String forwardRelatedWord : excludeRelatedWords) {
                selectionArgs[i++] = forwardRelatedWord;
            }
        }
        Log.v(TAG, "Query: selection = " + selection);
        Log.v(TAG, "Query: selectionArgs = " + Arrays.toString(selectionArgs));
        Cursor cursor = mEmbeddedDb.query("thesaurus", projection, selection, selectionArgs);
        if (cursor != null) {

            try {
                List<ThesaurusEntry.ThesaurusEntryDetails> reverseRelatedWords = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String relatedWord = cursor.getString(0);
                    ThesaurusEntry.WordType wordType = ThesaurusEntry.WordType.valueOf(cursor.getString(1));
                    ThesaurusEntry.ThesaurusEntryDetails entryDetails = relationType == RelationType.SYNONYM ?
                            new ThesaurusEntry.ThesaurusEntryDetails(wordType, Collections.singletonList(relatedWord), Collections.emptyList())
                            : new ThesaurusEntry.ThesaurusEntryDetails(wordType, Collections.emptyList(), Collections.singletonList(relatedWord));

                    reverseRelatedWords.add(entryDetails);
                }
                List<ThesaurusEntry.ThesaurusEntryDetails> result = merge(reverseRelatedWords);
                Log.v(TAG, "lookupReverseRelatedWords: result = " + result);
                return result;
            } finally {
                cursor.close();
            }
        }
        return Collections.emptyList();
    }

    /**
     * @param entries a list of {@link ThesaurusEntry.ThesaurusEntryDetails} possibly containing multiple items for a given word type.
     * @return a list of one {@link ThesaurusEntry.ThesaurusEntryDetails} per word type.
     */
    private List<ThesaurusEntry.ThesaurusEntryDetails> merge(List<ThesaurusEntry.ThesaurusEntryDetails> entries) {
        return Observable.fromIterable(entries)
                .groupBy(thesaurusEntryDetails -> thesaurusEntryDetails.wordType)
                .flatMapSingle(group -> group.reduce(
                        new ThesaurusEntry.ThesaurusEntryDetails(group.getKey(), Collections.emptyList(), Collections.emptyList()),
                        (acc, thesaurusEntryDetails) -> new ThesaurusEntry.ThesaurusEntryDetails(acc.wordType,
                                union(acc.synonyms, thesaurusEntryDetails.synonyms),
                                union(acc.antonyms, thesaurusEntryDetails.antonyms))))
                .toList()
                .blockingGet();
    }

    private static List<String> union(List<String> first, List<String> second) {
        Set<String> result = new LinkedHashSet<>();
        result.addAll(first);
        result.addAll(second);
        return new ArrayList<>(result);
    }

    /**
     * @return the synonyms of the given word.
     */
    @NonNull
    Collection<String> getFlatSynonyms(String word, boolean includeReverseLookup) {
        List<ThesaurusEntry.ThesaurusEntryDetails> entries = lookup(word, EnumSet.of(RelationType.SYNONYM), includeReverseLookup).entries;
        return Observable.fromIterable(merge(entries))
                .flatMapIterable((thesaurusEntryDetails -> thesaurusEntryDetails.synonyms))
                .toList()
                .blockingGet();
    }

    private static List<String> split(String string) {
        if (TextUtils.isEmpty(string)) return Collections.emptyList();
        return Arrays.asList(string.split(","));
    }
}
