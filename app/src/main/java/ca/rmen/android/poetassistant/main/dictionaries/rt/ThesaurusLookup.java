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

public class ThesaurusLookup {

    private static final String TAG = Constants.TAG + ThesaurusLookup.class.getSimpleName();

    private final Context mContext;
    private final String mQuery;
    private final String mFilter;

    public ThesaurusLookup(Context context, String query, String filter) {
        mContext = context.getApplicationContext();
        mQuery = query;
        mFilter = filter;
    }

    public List<RTEntry> lookup() {
        Log.d(TAG, "lookup() called with: mQuery=" + mQuery + ", mFilter=" + mFilter);

        Thesaurus thesaurus = Thesaurus.getInstance(mContext);
        List<RTEntry> data = new ArrayList<>();
        if(TextUtils.isEmpty(mQuery)) return data;
        Thesaurus.ThesaurusEntry[] entries = thesaurus.getEntries(mQuery);
        if (entries.length == 0) return data;

        if (!TextUtils.isEmpty(mFilter)) {
            Set<String> rhymes = Rhymer.getInstance(mContext).getFlatRhymes(mFilter);
            entries = filter(entries, rhymes);
        }

        for (Thesaurus.ThesaurusEntry entry : entries) {
            data.add(new RTEntry(RTEntry.Type.HEADING, entry.wordType.name().toLowerCase(Locale.US)));
            addResultSection(data, R.string.thesaurus_section_synonyms, entry.synonyms);
            addResultSection(data, R.string.thesaurus_section_antonyms, entry.antonyms);
        }
        return data;
    }

    private void addResultSection(List<RTEntry> results, int sectionHeadingResId, String[] words) {
        if (words.length > 0) {
            results.add(new RTEntry(RTEntry.Type.SUBHEADING, mContext.getString(sectionHeadingResId)));
            for (String word : words) {
                results.add(new RTEntry(RTEntry.Type.WORD, word));
            }
        }
    }

    private static Thesaurus.ThesaurusEntry[] filter(Thesaurus.ThesaurusEntry[] entries, Set<String> filter) {
        List<Thesaurus.ThesaurusEntry> filteredEntries = new ArrayList<>();
        for (Thesaurus.ThesaurusEntry entry : entries) {
            Thesaurus.ThesaurusEntry filteredEntry = filter(entry, filter);
            if (filteredEntry != null) filteredEntries.add(filteredEntry);
        }
        return filteredEntries.toArray(new Thesaurus.ThesaurusEntry[filteredEntries.size()]);
    }

    private static Thesaurus.ThesaurusEntry filter(Thesaurus.ThesaurusEntry entry, Set<String> filter) {
        Thesaurus.ThesaurusEntry filteredEntry = new Thesaurus.ThesaurusEntry(entry.wordType,
                RTUtils.filter(entry.synonyms, filter),
                RTUtils.filter(entry.antonyms, filter));
        if (isEmpty(filteredEntry)) return null;
        return filteredEntry;
    }

    private static boolean isEmpty(Thesaurus.ThesaurusEntry entry) {
        return entry.synonyms.length == 0 && entry.antonyms.length == 0;
    }


}
