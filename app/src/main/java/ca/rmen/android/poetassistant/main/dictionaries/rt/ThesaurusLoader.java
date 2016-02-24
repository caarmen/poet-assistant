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
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;

public class ThesaurusLoader extends AsyncTaskLoader<List<RTEntry>> {

    private static final String TAG = Constants.TAG + ThesaurusLoader.class.getSimpleName();

    private final String mQuery;


    public ThesaurusLoader(Context context, String query) {
        super(context);
        mQuery = query;
    }

    @Override
    public List<RTEntry> loadInBackground() {
        Log.d(TAG, "loadInBackground() called with: " + "");

        Thesaurus thesaurus = Thesaurus.getInstance(getContext());
        List<RTEntry> data = new ArrayList<>();
        Thesaurus.ThesaurusEntry[] entries = thesaurus.getEntries(mQuery);
        if (entries.length == 0) return data;

        for (Thesaurus.ThesaurusEntry entry : entries) {
            data.add(new RTEntry(RTEntry.Type.HEADING, entry.wordType.name().toLowerCase(Locale.US)));
            if (entry.synonyms.length > 0) {
                data.add(new RTEntry(RTEntry.Type.SUBHEADING, getContext().getString(R.string.thesaurus_section_synonyms)));
                for (String synonym : entry.synonyms) {
                    data.add(new RTEntry(RTEntry.Type.WORD, synonym));
                }
            }
            if (entry.antonyms.length > 0) {
                data.add(new RTEntry(RTEntry.Type.SUBHEADING, getContext().getString(R.string.thesaurus_section_antonyms)));
                for (String antonym : entry.antonyms) {
                    data.add(new RTEntry(RTEntry.Type.WORD, antonym));
                }
            }
        }
        return data;
    }


}
