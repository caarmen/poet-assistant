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

package ca.rmen.android.poetassistant.main;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.main.dictionaries.rhymer.RhymerLoader;
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ThesaurusLoader;

public abstract class ResultListLoader extends AsyncTaskLoader<List<ResultListEntry>> {
    private static final String TAG = Constants.TAG + ResultListLoader.class.getSimpleName();

    static ResultListLoader getLoader(Tab tab, Context context, String query) {
        switch (tab) {
            case RHYMER:
                return new RhymerLoader(context, query);
            case THESAURUS:
            default:
                return new ThesaurusLoader(context, query);
        }
    }

    protected ResultListLoader(Context context) {
        super(context);
    }
}

