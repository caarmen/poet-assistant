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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListExporter;

public class ThesaurusListExporter implements ResultListExporter<List<RTEntry>> {
    private final Context mContext;

    public ThesaurusListExporter(Context context) {
        mContext = context;
    }

    @Override
    public String export(@NonNull String word,
                         @Nullable String filter, /* results only include rhymes of filter */
                         @NonNull List<RTEntry> entries) {
        final String title;
        if (TextUtils.isEmpty(filter)) {
            title = mContext.getString(R.string.share_thesaurus_title, word);
        } else {
            title = mContext.getString(R.string.share_thesaurus_title_with_filter, word, filter);
        }
        StringBuilder builder = new StringBuilder(title);
        for (RTEntry entry : entries) {
            int entryResId;
            if (entry.type == RTEntry.Type.HEADING) entryResId = R.string.share_rt_heading;
            else if (entry.type == RTEntry.Type.SUBHEADING) entryResId = R.string.share_rt_subheading;
            else entryResId = R.string.share_rt_entry;
            builder.append(mContext.getString(entryResId, entry.text));
        }
        return builder.toString();
    }
}
