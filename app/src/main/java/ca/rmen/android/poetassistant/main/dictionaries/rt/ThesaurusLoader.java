/*
 * Copyright (c) 2016-2017 Carmen Alvarez
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
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.dagger.DaggerHelper;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.Favorites;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListData;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListLoader;
import ca.rmen.android.poetassistant.settings.Settings;
import ca.rmen.android.poetassistant.settings.SettingsPrefs;

public class ThesaurusLoader extends ResultListLoader<ResultListData<RTEntry>> {

    private static final String TAG = Constants.TAG + ThesaurusLoader.class.getSimpleName();

    @Inject Rhymer mRhymer;
    @Inject Thesaurus mThesaurus;
    @Inject SettingsPrefs mPrefs;
    @Inject Favorites mFavorites;
    private final String mQuery;
    private final String mFilter;

    public ThesaurusLoader(Context context, String query, String filter) {
        super(context);
        mQuery = query;
        mFilter = filter;
        DaggerHelper.getMainScreenComponent(context).inject(this);
    }

    @Override
    public ResultListData<RTEntry> loadInBackground() {
        Log.d(TAG, "loadInBackground() called with: query = " + mQuery + ", filter = " + mFilter);

        List<RTEntry> data = new ArrayList<>();
        if(TextUtils.isEmpty(mQuery)) return emptyResult();
        ThesaurusEntry result  = mThesaurus.lookup(mQuery);
        ThesaurusEntry.ThesaurusEntryDetails[] entries = result.entries;
        if (entries.length == 0) return emptyResult();

        if (!TextUtils.isEmpty(mFilter)) {
            Set<String> rhymes = mRhymer.getFlatRhymes(mFilter);
            entries = filter(entries, rhymes);
        }

        Settings.Layout layout = Settings.getLayout(mPrefs);
        Set<String> favorites = mFavorites.getFavorites();
        for (ThesaurusEntry.ThesaurusEntryDetails entry : entries) {
            data.add(new RTEntry(RTEntry.Type.HEADING, entry.wordType.name().toLowerCase(Locale.US)));
            addResultSection(favorites, data, R.string.thesaurus_section_synonyms, entry.synonyms, layout);
            addResultSection(favorites, data, R.string.thesaurus_section_antonyms, entry.antonyms, layout);
        }
        return new ResultListData<>(result.word, data);
    }

    private ResultListData<RTEntry> emptyResult() {
        return new ResultListData<>(mQuery, new ArrayList<>());
    }

    private void addResultSection(Set<String> favorites, List<RTEntry> results, int sectionHeadingResId, String[] words, Settings.Layout layout) {
        if (words.length > 0) {
            results.add(new RTEntry(RTEntry.Type.SUBHEADING, getContext().getString(sectionHeadingResId)));
            for (int i = 0; i < words.length; i++) {
                @ColorRes int color = (i % 2 == 0)? R.color.row_background_color_even : R.color.row_background_color_odd;
                results.add(new RTEntry(
                        RTEntry.Type.WORD,
                        words[i],
                        ContextCompat.getColor(getContext(), color),
                        favorites.contains(words[i]),
                        layout == Settings.Layout.EFFICIENT));
            }
        }
    }

    @VisibleForTesting
    static ThesaurusEntry.ThesaurusEntryDetails[] filter(ThesaurusEntry.ThesaurusEntryDetails[] entries, Set<String> filter) {
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
