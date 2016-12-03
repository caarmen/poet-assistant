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
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.DaggerHelper;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListData;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListLoader;
import ca.rmen.android.poetassistant.settings.SettingsPrefs;
import ca.rmen.rhymer.RhymeResult;

public class RhymerLoader extends ResultListLoader<ResultListData<RTEntry>> {

    private static final String TAG = Constants.TAG + RhymerLoader.class.getSimpleName();

    private final String mQuery;
    private final String mFilter;
    @Inject SettingsPrefs mPrefs;
    @Inject Rhymer mRhymer;
    @Inject Thesaurus mThesaurus;

    public RhymerLoader(Context context, String query, String filter) {
        super(context);
        mQuery = query;
        mFilter = filter;
        DaggerHelper.getAppComponent(context).inject(this);
    }

    @Override
    public ResultListData<RTEntry> loadInBackground() {
        Log.d(TAG, "loadInBackground() called with: query = " + mQuery + ", filter = " + mFilter);
        long before = System.currentTimeMillis();

        List<RTEntry> data = new ArrayList<>();
        if (TextUtils.isEmpty(mQuery)) return emptyResult();

        List<RhymeResult> rhymeResults = mRhymer.getRhymingWords(mQuery);
        if (rhymeResults == null) {
            return emptyResult();
        }
        if (!TextUtils.isEmpty(mFilter)) {
            Set<String> synonyms = mThesaurus.getFlatSynonyms(mFilter);
            if (synonyms.isEmpty()) return emptyResult();
            rhymeResults = filter(rhymeResults, synonyms);
        }
        Set<String> favorites = mFavorites.getFavorites();
        if (!favorites.isEmpty()) {
            addResultSection(favorites, data, R.string.rhyme_section_favorites, getMatchingFavorites(rhymeResults, favorites));
        }
        for (RhymeResult rhymeResult : rhymeResults) {
            // Add the word variant, if there are multiple pronunciations.
            if (rhymeResults.size() > 1) {
                String heading = mQuery + " (" + (rhymeResult.variantNumber + 1) + ")";
                data.add(new RTEntry(RTEntry.Type.HEADING, heading));
            }

            addResultSection(favorites, data, R.string.rhyme_section_stress_syllables, rhymeResult.strictRhymes);
            addResultSection(favorites, data, R.string.rhyme_section_three_syllables, rhymeResult.threeSyllableRhymes);
            addResultSection(favorites, data, R.string.rhyme_section_two_syllables, rhymeResult.twoSyllableRhymes);
            addResultSection(favorites, data, R.string.rhyme_section_one_syllable, rhymeResult.oneSyllableRhymes);
        }
        ResultListData<RTEntry> result = new ResultListData<>(mQuery, favorites.contains(mQuery), data);
        long after = System.currentTimeMillis();
        Log.d(TAG, "loadInBackground() finished in " + (after - before) + "ms");
        return result;
    }

    private String[] getMatchingFavorites(List<RhymeResult> rhymeResults, Set<String> favorites) {
        Set<String> matchingFavorites = new TreeSet<>();
        for (RhymeResult rhymeResult : rhymeResults) {
            for (String rhyme : rhymeResult.strictRhymes) {
                if (favorites.contains(rhyme)) {
                    matchingFavorites.add(rhyme);
                }
            }
            for (String rhyme : rhymeResult.oneSyllableRhymes) {
                if (favorites.contains(rhyme)) {
                    matchingFavorites.add(rhyme);
                }
            }
            for (String rhyme : rhymeResult.twoSyllableRhymes) {
                if (favorites.contains(rhyme)) {
                    matchingFavorites.add(rhyme);
                }
            }
            for (String rhyme : rhymeResult.threeSyllableRhymes) {
                if (favorites.contains(rhyme)) {
                    matchingFavorites.add(rhyme);
                }
            }
        }
        return matchingFavorites.toArray(new String[0]);
    }

    private ResultListData<RTEntry> emptyResult() {
        return new ResultListData<>(mQuery, false, new ArrayList<>());
    }

    private void addResultSection(Set<String> favorites, List<RTEntry> results, int sectionHeadingResId, String[] rhymes) {
        if (rhymes.length > 0) {

            Set<String> wordsWithDefinitions = mPrefs.getIsAllRhymesEnabled() ? mRhymer.getWordsWithDefinitions(rhymes) : null;
            results.add(new RTEntry(RTEntry.Type.SUBHEADING, getContext().getString(sectionHeadingResId)));
            for (int i = 0; i < rhymes.length; i++) {
                @ColorRes int color = (i % 2 == 0)? R.color.row_background_color_even : R.color.row_background_color_odd;
                boolean hasDefinition = wordsWithDefinitions == null || wordsWithDefinitions.contains(rhymes[i]);
                results.add(new RTEntry(
                        RTEntry.Type.WORD,
                        rhymes[i],
                        ContextCompat.getColor(getContext(), color),
                        favorites.contains(rhymes[i]),
                        hasDefinition));
            }
        }
    }


    private static List<RhymeResult> filter(List<RhymeResult> rhymes, Set<String> filter) {
        List<RhymeResult> filteredRhymes = new ArrayList<>();
        for (RhymeResult rhymeResult : rhymes) {
            RhymeResult filteredRhymeResult = filter(rhymeResult, filter);
            if (filteredRhymeResult != null) filteredRhymes.add(filteredRhymeResult);
        }
        return filteredRhymes;
    }

    private static RhymeResult filter(RhymeResult rhyme, Set<String> filter) {
        RhymeResult result = new RhymeResult(rhyme.variantNumber,
                RTUtils.filter(rhyme.strictRhymes, filter),
                RTUtils.filter(rhyme.oneSyllableRhymes, filter),
                RTUtils.filter(rhyme.twoSyllableRhymes, filter),
                RTUtils.filter(rhyme.threeSyllableRhymes, filter));
        if (isEmpty(result)) return null;
        return result;
    }

    private static boolean isEmpty(RhymeResult rhymeResult) {
        return rhymeResult.strictRhymes.length == 0
                && rhymeResult.oneSyllableRhymes.length == 0
                && rhymeResult.twoSyllableRhymes.length == 0
                && rhymeResult.threeSyllableRhymes.length == 0;
    }


}
