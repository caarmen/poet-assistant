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
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.Locale;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListFactory;
import ca.rmen.android.poetassistant.main.reader.ReaderFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class PagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = Constants.TAG + PagerAdapter.class.getSimpleName();

    private final Context mContext;
    private String mInitialRhymeQuery;
    private String mInitialThesaurusQuery;
    private String mInitialDictionaryQuery;
    private String mInitialPoemText;

    public PagerAdapter(Context context, FragmentManager fm, Intent intent) {
        super(fm);
        Log.v(TAG, "Constructor: intent = " + intent);
        mContext = context;
        Uri initialQuery = intent.getData();
        // Deep link to query in a specific tab
        if (initialQuery != null) {
            Tab tab = Tab.parse(initialQuery.getHost());
            if (tab == Tab.RHYMER) {
                mInitialRhymeQuery = initialQuery.getLastPathSegment();
            } else if (tab == Tab.THESAURUS) {
                mInitialThesaurusQuery = initialQuery.getLastPathSegment();
            } else if (tab == Tab.DICTIONARY) {
                mInitialDictionaryQuery = initialQuery.getLastPathSegment();
            } else if (Constants.DEEP_LINK_QUERY.equals(initialQuery.getHost())) {
                mInitialRhymeQuery = initialQuery.getLastPathSegment();
                mInitialThesaurusQuery = initialQuery.getLastPathSegment();
                mInitialDictionaryQuery = initialQuery.getLastPathSegment();
            }
        }
        // Text shared from another app:
        else if (Intent.ACTION_SEND.equals(intent.getAction())) {
            mInitialPoemText = intent.getStringExtra(Intent.EXTRA_TEXT);
        }
    }

    @Override
    public Fragment getItem(int position) {
        Log.v(TAG, "SectionsPagerAdapter getItem " + position);
        if (position == Tab.RHYMER.ordinal()) {
            return ResultListFactory.createListFragment(Tab.RHYMER, mInitialRhymeQuery);
        } else if (position == Tab.THESAURUS.ordinal()) {
            return ResultListFactory.createListFragment(Tab.THESAURUS, mInitialThesaurusQuery);
        } else if (position == Tab.DICTIONARY.ordinal()) {
            return ResultListFactory.createListFragment(Tab.DICTIONARY, mInitialDictionaryQuery);
        } else {
            return ReaderFragment.newInstance(mInitialPoemText);
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == Tab.RHYMER.ordinal())
            return mContext.getString(R.string.tab_rhymer).toUpperCase(Locale.getDefault());
        else if (position == Tab.THESAURUS.ordinal())
            return mContext.getString(R.string.tab_thesaurus).toUpperCase(Locale.getDefault());
        else if (position == Tab.DICTIONARY.ordinal())
            return mContext.getString(R.string.tab_dictionary).toUpperCase(Locale.getDefault());
        else
            return mContext.getString(R.string.tab_reader).toUpperCase(Locale.getDefault());
    }
}
