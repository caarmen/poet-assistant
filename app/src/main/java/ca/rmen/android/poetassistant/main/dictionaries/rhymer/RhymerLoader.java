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

package ca.rmen.android.poetassistant.main.dictionaries.rhymer;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.ResultListEntry;
import ca.rmen.android.poetassistant.main.ResultListLoader;
import ca.rmen.rhymer.RhymeResult;

public class RhymerLoader extends ResultListLoader {

    private static final String TAG = Constants.TAG + RhymerLoader.class.getSimpleName();

    private final String mQuery;

    public RhymerLoader(Context context, String query) {
        super(context);
        mQuery = query;
    }

    @Override
    public List<ResultListEntry> loadInBackground() {
        Log.d(TAG, "loadInBackground() called with: " + "");

        Rhymer rhymer = Rhymer.getInstance(getContext());
        List<RhymeResult> rhymeResults = rhymer.getRhymingWords(mQuery);
        List<ResultListEntry> data = new ArrayList<>();
        if (rhymeResults == null) {
            return data;
        }
        for (RhymeResult rhymeResult : rhymeResults) {
            // Add the word variant, if there are multiple pronunciations.
            if (rhymeResults.size() > 1) {
                String heading = mQuery + " (" + (rhymeResult.variantNumber + 1) + ")";
                data.add(new ResultListEntry(ResultListEntry.Type.HEADING, heading));
            }

            if (rhymeResult.oneSyllableRhymes.length > 0) {
                data.add(new ResultListEntry(ResultListEntry.Type.SUBHEADING, getContext().getString(R.string.rhyme_section_one_syllable)));
                for (String word : rhymeResult.oneSyllableRhymes) {
                    data.add(new ResultListEntry(ResultListEntry.Type.WORD, word));
                }
            }
            if (rhymeResult.twoSyllableRhymes.length > 0) {
                data.add(new ResultListEntry(ResultListEntry.Type.SUBHEADING, getContext().getString(R.string.rhyme_section_two_syllables)));
                for (String word : rhymeResult.twoSyllableRhymes) {
                    data.add(new ResultListEntry(ResultListEntry.Type.WORD, word));
                }
            }
            if (rhymeResult.threeSyllableRhymes.length > 0) {
                data.add(new ResultListEntry(ResultListEntry.Type.SUBHEADING, getContext().getString(R.string.rhyme_section_three_syllables)));
                for (String word : rhymeResult.threeSyllableRhymes) {
                    data.add(new ResultListEntry(ResultListEntry.Type.WORD, word));
                }
            }
        }
        return data;
    }


}
