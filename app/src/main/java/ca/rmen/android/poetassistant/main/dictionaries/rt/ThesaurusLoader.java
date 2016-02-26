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
import java.util.Locale;
import java.util.Set;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListData;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListLoader;

public class ThesaurusLoader extends ResultListLoader<ResultListData<RTEntry>> {

    private static final String TAG = Constants.TAG + ThesaurusLoader.class.getSimpleName();

    public ThesaurusLoader(Context context) {
        super(context);
    }

    @Override
    protected ResultListData<RTEntry> getEntries(String query, String filter) {
        Log.d(TAG, "getEntries() called with: " + "query = [" + query + "], filter = [" + filter + "]");

        Thesaurus thesaurus = Thesaurus.getInstance(getContext());
        List<RTEntry> data = new ArrayList<>();
        if(TextUtils.isEmpty(query)) return emptyResult(query);
        ThesaurusEntry result  = thesaurus.lookup(query);
        ThesaurusEntry.ThesaurusEntryDetails[] entries = result.entries;
        if (entries.length == 0) return emptyResult(query);

        if (!TextUtils.isEmpty(filter)) {
            Set<String> rhymes = Rhymer.getInstance(getContext()).getFlatRhymes(filter);
            entries = filter(entries, rhymes);
        }

        for (ThesaurusEntry.ThesaurusEntryDetails entry : entries) {
            data.add(new RTEntry(RTEntry.Type.HEADING, entry.wordType.name().toLowerCase(Locale.US)));
            addResultSection(data, R.string.thesaurus_section_synonyms, entry.synonyms);
            addResultSection(data, R.string.thesaurus_section_antonyms, entry.antonyms);
        }
        return new ResultListData<>(result.word, data);
    }

    private ResultListData<RTEntry> emptyResult(String query) {
        return new ResultListData<>(query, new ArrayList<>());
    }

    private void addResultSection(List<RTEntry> results, int sectionHeadingResId, String[] words) {
        if (words.length > 0) {
            results.add(new RTEntry(RTEntry.Type.SUBHEADING, getContext().getString(sectionHeadingResId)));
            for (String word : words) {
                results.add(new RTEntry(RTEntry.Type.WORD, word));
            }
        }
    }

    private static ThesaurusEntry.ThesaurusEntryDetails[] filter(ThesaurusEntry.ThesaurusEntryDetails[] entries, Set<String> filter) {
        List<ThesaurusEntry.ThesaurusEntryDetails> filteredEntries = new ArrayList<>();
        for (ThesaurusEntry.ThesaurusEntryDetails entry : entries) {
            ThesaurusEntry.ThesaurusEntryDetails filteredEntry = filter(entry, filter);
            if (filteredEntry != null) filteredEntries.add(filteredEntry);
        }
        return filteredEntries.toArray(new ThesaurusEntry.ThesaurusEntryDetails[filteredEntries.size()]);
    }

    private static ThesaurusEntry.ThesaurusEntryDetails filter(ThesaurusEntry.ThesaurusEntryDetails entry, Set<String> filter) {
        ThesaurusEntry.ThesaurusEntryDetails filteredEntry = new ThesaurusEntry.ThesaurusEntryDetails(entry.wordType,
                RTUtils.filter(entry.synonyms, filter),
                RTUtils.filter(entry.antonyms, filter));
        if (isEmpty(filteredEntry)) return null;
        return filteredEntry;
    }

    private static boolean isEmpty(ThesaurusEntry.ThesaurusEntryDetails entry) {
        return entry.synonyms.length == 0 && entry.antonyms.length == 0;
    }


}
