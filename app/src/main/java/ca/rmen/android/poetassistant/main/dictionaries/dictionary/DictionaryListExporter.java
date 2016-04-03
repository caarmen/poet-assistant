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

package ca.rmen.android.poetassistant.main.dictionaries.dictionary;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListExporter;

public class DictionaryListExporter implements ResultListExporter<List<DictionaryEntry.DictionaryEntryDetails>> {
    private final Context mContext;

    public DictionaryListExporter(Context context) {
        mContext = context;
    }

    @Override
    public String export(@NonNull String word,
                         @Nullable String filter /* always null for the dictionary*/,
                         @NonNull List<DictionaryEntry.DictionaryEntryDetails> entries) {
        String title = mContext.getString(R.string.share_dictionary_title, word);
        StringBuilder builder = new StringBuilder(title);
        for (DictionaryEntry.DictionaryEntryDetails entry : entries) {
            builder.append(mContext.getString(R.string.share_dictionary_entry, entry.partOfSpeech, entry.definition));
        }
        return builder.toString();
    }
}
