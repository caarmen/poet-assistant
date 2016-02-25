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
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.rhymer.RhymeResult;

public class RhymerLoader extends AsyncTaskLoader<List<RTEntry>> {

    private static final String TAG = Constants.TAG + RhymerLoader.class.getSimpleName();

    private final String mQuery;
    private final String mFilter;

    public RhymerLoader(Context context, String query, String filter) {
        super(context);
        mQuery = query;
        mFilter = filter;
    }

    @Override
    public List<RTEntry> loadInBackground() {
        Log.d(TAG, "loadInBackground() called with: " + "");

        Rhymer rhymer = Rhymer.getInstance(getContext());
        List<RhymeResult> rhymeResults = rhymer.getRhymingWords(mQuery);
        List<RTEntry> data = new ArrayList<>();
        if (rhymeResults == null) {
            return data;
        }
        if (!TextUtils.isEmpty(mFilter)) {
            Set<String> synonyms = Thesaurus.getInstance(getContext()).getFlatSynonyms(mFilter);
            if (synonyms.isEmpty()) return data;
            rhymeResults = filter(rhymeResults, synonyms);
        }
        for (RhymeResult rhymeResult : rhymeResults) {
            // Add the word variant, if there are multiple pronunciations.
            if (rhymeResults.size() > 1) {
                String heading = mQuery + " (" + (rhymeResult.variantNumber + 1) + ")";
                data.add(new RTEntry(RTEntry.Type.HEADING, heading));
            }

            if (rhymeResult.strictRhymes.length > 0) {
                data.add(new RTEntry(RTEntry.Type.SUBHEADING, getContext().getString(R.string.rhyme_section_stress_syllables)));
                for (String word : rhymeResult.strictRhymes) {
                    data.add(new RTEntry(RTEntry.Type.WORD, word));
                }
            }
            if (rhymeResult.oneSyllableRhymes.length > 0) {
                data.add(new RTEntry(RTEntry.Type.SUBHEADING, getContext().getString(R.string.rhyme_section_one_syllable)));
                for (String word : rhymeResult.oneSyllableRhymes) {
                    data.add(new RTEntry(RTEntry.Type.WORD, word));
                }
            }
            if (rhymeResult.twoSyllableRhymes.length > 0) {
                data.add(new RTEntry(RTEntry.Type.SUBHEADING, getContext().getString(R.string.rhyme_section_two_syllables)));
                for (String word : rhymeResult.twoSyllableRhymes) {
                    data.add(new RTEntry(RTEntry.Type.WORD, word));
                }
            }
            if (rhymeResult.threeSyllableRhymes.length > 0) {
                data.add(new RTEntry(RTEntry.Type.SUBHEADING, getContext().getString(R.string.rhyme_section_three_syllables)));
                for (String word : rhymeResult.threeSyllableRhymes) {
                    data.add(new RTEntry(RTEntry.Type.WORD, word));
                }
            }
        }
        return data;
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
