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

package ca.rmen.android.poetassistant.main.dictionaries.search;

import android.app.SearchManager;
import android.content.Context;
import android.database.MatrixCursor;
import android.os.Build;
import android.provider.BaseColumns;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;

import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.DaggerHelper;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary;
import ca.rmen.android.poetassistant.settings.SettingsPrefs;
import java8.util.stream.StreamSupport;

/**
 * SharedPreferences and db-backed cursor to read suggestions.  Suggestions include
 * words which have been looked up before, as well as similar words in the database.
 */
public class SuggestionsCursor extends MatrixCursor {
    private static final String[] COLUMNS =
            new String[]{BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_ICON_1,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA};

    @Inject SettingsPrefs mSettingsPrefs;
    @Inject Dictionary mDictionary;
    private final String mFilter;

    SuggestionsCursor(Context context, String filter) {
        super(COLUMNS);
        mFilter = filter;
        DaggerHelper.getAppComponent(context).inject(this);
        loadHistory();
        loadSimilarWords();
    }

    private void loadHistory() {
        Set<String> suggestions = mSettingsPrefs.getSuggestedWords();
        TreeSet<String> sortedSuggestions = new TreeSet<>();
        sortedSuggestions.addAll(suggestions);
        // https://code.google.com/p/android/issues/detail?id=226686
        final @DrawableRes int iconId;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            iconId = R.drawable.ic_search_history_deprecated;
        } else {
            iconId = R.drawable.ic_search_history;
        }
        StreamSupport.stream(sortedSuggestions)
                .filter(suggestion -> TextUtils.isEmpty(mFilter) || suggestion.contains(mFilter))
                .forEach(suggestion -> addSuggestion(suggestion, iconId));
    }

    private void loadSimilarWords() {
        if (!TextUtils.isEmpty(mFilter)) {
            String[] similarSoundingWords = mDictionary.findWordsWithPrefix(mFilter.trim().toLowerCase(Locale.getDefault()));
            // https://code.google.com/p/android/issues/detail?id=226686
            final @DrawableRes int iconId;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                iconId = R.drawable.ic_action_search_deprecated;
            } else {
                iconId = R.drawable.ic_action_search;
            }
            for (String similarSoundingWord : similarSoundingWords) {
                addSuggestion(similarSoundingWord, iconId);
            }
        }
    }

    private void addSuggestion(String word, @DrawableRes int iconId) {
        addRow(new Object[]{getCount(), word, iconId, word});
    }

}
