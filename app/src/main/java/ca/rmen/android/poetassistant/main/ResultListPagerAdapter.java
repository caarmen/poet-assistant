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
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.Locale;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class ResultListPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = Constants.TAG + ResultListPagerAdapter.class.getSimpleName();

    private final Context mContext;
    private String mInitialRhymeQuery;
    private String mInitialThesaurusQuery;

    public static class Query {
        public final Dictionary dictionary;
        public final String word;

        public Query(Dictionary dictionary, String word) {
            this.dictionary = dictionary;
            this.word = word;
        }
    }

    public ResultListPagerAdapter(Context context, FragmentManager fm, Uri initialQuery) {
        super(fm);
        Log.v(TAG, "Constructor: initialQuery = " + initialQuery);
        mContext = context;
        if (initialQuery != null) {
            Dictionary dictionary = Dictionary.parse(initialQuery.getHost());
            if (dictionary == Dictionary.RHYMER) {
                mInitialRhymeQuery = initialQuery.getLastPathSegment();
            } else if (dictionary == Dictionary.THESAURUS) {
                mInitialThesaurusQuery= initialQuery.getLastPathSegment();
            }
        }
    }

    @Override
    public Fragment getItem(int position) {
        Log.v(TAG, "SectionsPagerAdapter getItem " + position);
        if (position == Dictionary.RHYMER.ordinal()) {
            return ResultListFragment.newInstance(Dictionary.RHYMER, mInitialRhymeQuery);
        } else /*if (position == TAB_THESAURUS)*/ {
            return ResultListFragment.newInstance(Dictionary.THESAURUS, mInitialThesaurusQuery);
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == Dictionary.RHYMER.ordinal())
            return mContext.getString(R.string.tab_rhymer).toUpperCase(Locale.getDefault());
        else if (position == Dictionary.THESAURUS.ordinal())
            return mContext.getString(R.string.tab_thesaurus).toUpperCase(Locale.getDefault());
        return null;
    }
}
