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

import java.util.List;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListRenderer;

public class RTListRenderer extends ResultListRenderer<List<RTEntry>> {
    private final Context mContext;
    private final int mTitleResId;

    public RTListRenderer(Context context, int titleResId, String word, List<RTEntry> entries) {
        super(word, entries);
        mContext = context;
        mTitleResId = titleResId;
    }

    @Override
    public String toHtml() {
        String title = mContext.getString(mTitleResId, mWord);
        StringBuilder builder = new StringBuilder(title);
        for (RTEntry entry : mData) {
            int entryResId;
            if (entry.type == RTEntry.Type.HEADING) entryResId = R.string.share_heading;
            else if (entry.type == RTEntry.Type.SUBHEADING)
                entryResId = R.string.share_subheading;
            else entryResId = R.string.share_rt_entry;
            builder.append(mContext.getString(entryResId, entry.text));
        }
        return builder.toString();
    }
}
