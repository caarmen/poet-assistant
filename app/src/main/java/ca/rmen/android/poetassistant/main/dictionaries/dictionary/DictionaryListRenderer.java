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

import java.util.List;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListRenderer;
import ca.rmen.android.poetassistant.main.dictionaries.rt.RTEntry;

public class DictionaryListRenderer extends ResultListRenderer<List<DictionaryEntryDetails>> {
    private final Context mContext;

    public DictionaryListRenderer(Context context, String word, List<DictionaryEntryDetails> entries) {
        super(word, entries);
        mContext = context;
    }

    @Override
    public String toHtml() {
        String title = mContext.getString(R.string.share_dictionary, mWord);
        StringBuilder builder = new StringBuilder(title);
        for (DictionaryEntryDetails entry : mData) {
            builder.append(mContext.getString(R.string.wotd_notification_definition, entry.partOfSpeech, entry.definition));
        }
        return builder.toString();
    }
}
