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
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListLoader;
import ca.rmen.rhymer.RhymeResult;
import rx.Observable;
import rx.functions.Func1;

public class RhymerLoader extends ResultListLoader<List<RTEntry>> {

    private static final String TAG = Constants.TAG + RhymerLoader.class.getSimpleName();

    public RhymerLoader(Context context) {
        super(context);
    }

    @Override
    protected Observable<List<RTEntry>> getEntries(String query, String filter) {
        Log.d(TAG, "getEntries() called with: " + "query = [" + query + "], filter = [" + filter + "]");
        Rhymer rhymer = Rhymer.getInstance(getContext());
        List<RhymeResult> rhymeResults = rhymer.getRhymingWords(query);
        Set<String> synonyms = null;
        if (!TextUtils.isEmpty(filter)) {
            synonyms = Thesaurus.getInstance(getContext()).getFlatSynonyms(filter);
            if (synonyms.isEmpty()) return Observable.just(Collections.emptyList());
        }

        return Observable
                .from(rhymeResults)
                .flatMap(filterSynonyms(synonyms))
                .filter(isNotEmpty())
                .flatMap(toRTEntries(query, rhymeResults.size() > 1));
    }

    private void addResultSection(List<RTEntry> results, int sectionHeadingResId, String[] rhymes) {
        if (rhymes.length > 0) {
            results.add(new RTEntry(RTEntry.Type.SUBHEADING, getContext().getString(sectionHeadingResId)));
            for (String word : rhymes) {
                results.add(new RTEntry(RTEntry.Type.WORD, word));
            }
        }
    }

    private Func1<RhymeResult, Observable<List<RTEntry>>> toRTEntries(final String query, final boolean showWordVariant) {
        return rhymeResult -> {
            List<RTEntry> data = new ArrayList<>();
            if (showWordVariant) {
                String heading = query + " (" + (rhymeResult.variantNumber + 1) + ")";
                data.add(new RTEntry(RTEntry.Type.HEADING, heading));
            }
            addResultSection(data, R.string.rhyme_section_stress_syllables, rhymeResult.strictRhymes);
            addResultSection(data, R.string.rhyme_section_one_syllable, rhymeResult.oneSyllableRhymes);
            addResultSection(data, R.string.rhyme_section_two_syllables, rhymeResult.twoSyllableRhymes);
            addResultSection(data, R.string.rhyme_section_three_syllables, rhymeResult.threeSyllableRhymes);
            return Observable.just(data);
        };
    }

    private Func1<RhymeResult, Observable<RhymeResult>> filterSynonyms(final Set<String> filter) {
        return (rhyme) -> {
            if (filter == null) return Observable.just(rhyme);
            return Observable.just(new RhymeResult(rhyme.variantNumber,
                    RTUtils.filter(rhyme.strictRhymes, filter),
                    RTUtils.filter(rhyme.oneSyllableRhymes, filter),
                    RTUtils.filter(rhyme.twoSyllableRhymes, filter),
                    RTUtils.filter(rhyme.threeSyllableRhymes, filter)));
        };
    }

    private Func1<RhymeResult, Boolean> isNotEmpty() {
        return rhymeResult -> rhymeResult != null
                &&
                (rhymeResult.strictRhymes.length > 0
                        || rhymeResult.oneSyllableRhymes.length > 0
                        || rhymeResult.twoSyllableRhymes.length > 0
                        || rhymeResult.threeSyllableRhymes.length > 0);
    }

}
