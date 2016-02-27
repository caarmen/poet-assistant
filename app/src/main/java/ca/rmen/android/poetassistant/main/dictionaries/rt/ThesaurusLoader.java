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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListLoader;
import rx.Observable;
import rx.functions.Func1;

public class ThesaurusLoader extends ResultListLoader<List<RTEntry>> {

    private static final String TAG = Constants.TAG + ThesaurusLoader.class.getSimpleName();

    public ThesaurusLoader(Context context) {
        super(context);
    }

    @Override
    protected Observable<List<RTEntry>> getEntries(String query, String filter) {

        Thesaurus thesaurus = Thesaurus.getInstance(getContext());
        Thesaurus.ThesaurusEntry[] entries = thesaurus.getEntries(query);

        Set<String> rhymes = null;
        if (!TextUtils.isEmpty(filter)) {
            rhymes = Rhymer.getInstance(getContext()).getFlatRhymes(filter);
            if (rhymes.isEmpty()) return Observable.just(Collections.emptyList());
        }

        return Observable
                .from(entries)
                .flatMap(filterRhymes(rhymes))
                .filter(isNotEmpty())
                .flatMap(toRTEntries());
    }

    private void addResultSection(List<RTEntry> results, int sectionHeadingResId, String[] words) {
        if (words.length > 0) {
            results.add(new RTEntry(RTEntry.Type.SUBHEADING, getContext().getString(sectionHeadingResId)));
            for (String word : words) {
                results.add(new RTEntry(RTEntry.Type.WORD, word));
            }
        }
    }

    private Func1<Thesaurus.ThesaurusEntry, Observable<List<RTEntry>>> toRTEntries() {
        return entry -> {
            List<RTEntry> data = new ArrayList<>();
            addResultSection(data, R.string.thesaurus_section_synonyms, entry.synonyms);
            addResultSection(data, R.string.thesaurus_section_antonyms, entry.antonyms);
            return Observable.just(data);
        };
    }

    private Func1<Thesaurus.ThesaurusEntry, Observable<Thesaurus.ThesaurusEntry>> filterRhymes(final Set<String> filter) {
        return (entry) -> {
            if (filter == null) return Observable.just(entry);
            return Observable.just(new Thesaurus.ThesaurusEntry(entry.wordType,
                    RTUtils.filter(entry.synonyms, filter),
                    RTUtils.filter(entry.antonyms, filter)));
        };
    }

    private Func1<Thesaurus.ThesaurusEntry, Boolean> isNotEmpty() {
        return entry -> entry != null && (entry.synonyms.length > 0 || entry.antonyms.length > 0);
    }

}
