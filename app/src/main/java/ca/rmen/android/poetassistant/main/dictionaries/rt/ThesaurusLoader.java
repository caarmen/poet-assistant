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
import java.util.List;
import java.util.Set;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListData;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListLoader;
import rx.Observable;
import rx.functions.Func1;

public class ThesaurusLoader extends ResultListLoader<ResultListData<RTEntry>> {

    private static final String TAG = Constants.TAG + ThesaurusLoader.class.getSimpleName();

    public ThesaurusLoader(Context context) {
        super(context);
    }

    @Override
    protected Observable<ResultListData<RTEntry>> getEntries(String query, String filter) {
        Log.d(TAG, "getEntries() called with: " + "query = [" + query + "], filter = [" + filter + "]");

        Thesaurus thesaurus = Thesaurus.getInstance(getContext());
        if(TextUtils.isEmpty(query)) return emptyResult(query);
        ThesaurusEntry result  = thesaurus.lookup(query);
        ThesaurusEntry.ThesaurusEntryDetails[] entries = result.entries;
        if (entries.length == 0) return emptyResult(query);

        Set<String> rhymes = null;
        if (!TextUtils.isEmpty(filter)) {
            rhymes = Rhymer.getInstance(getContext()).getFlatRhymes(filter);
            if (rhymes.isEmpty()) return emptyResult(query);
        }

        return Observable
                .from(entries)
                .flatMap(filterRhymes(rhymes))
                .filter(isNotEmpty())
                .flatMap(toRTEntries(query));
    }

    private Observable<ResultListData<RTEntry>> emptyResult(String query) {
        return Observable.just(new ResultListData<>(query, new ArrayList<>()));
    }

    private void addResultSection(List<RTEntry> results, int sectionHeadingResId, String[] words) {
        if (words.length > 0) {
            results.add(new RTEntry(RTEntry.Type.SUBHEADING, getContext().getString(sectionHeadingResId)));
            for (String word : words) {
                results.add(new RTEntry(RTEntry.Type.WORD, word));
            }
        }
    }

    private Func1<ThesaurusEntry.ThesaurusEntryDetails, Observable<ResultListData<RTEntry>>> toRTEntries(String query) {
        return entry -> {
            List<RTEntry> data = new ArrayList<>();
            addResultSection(data, R.string.thesaurus_section_synonyms, entry.synonyms);
            addResultSection(data, R.string.thesaurus_section_antonyms, entry.antonyms);
            return Observable.just(new ResultListData<>(query, data));
        };
    }

    private Func1<ThesaurusEntry.ThesaurusEntryDetails, Observable<ThesaurusEntry.ThesaurusEntryDetails>> filterRhymes(final Set<String> filter) {
        return (entry) -> {
            if (filter == null) return Observable.just(entry);
            return Observable.just(new ThesaurusEntry.ThesaurusEntryDetails(entry.wordType,
                    RTUtils.filter(entry.synonyms, filter),
                    RTUtils.filter(entry.antonyms, filter)));
        };
    }

    private Func1<ThesaurusEntry.ThesaurusEntryDetails, Boolean> isNotEmpty() {
        return entry -> entry != null && (entry.synonyms.length > 0 || entry.antonyms.length > 0);
    }

}
