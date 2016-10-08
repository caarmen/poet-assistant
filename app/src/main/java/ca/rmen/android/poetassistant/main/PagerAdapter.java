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
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import java.util.Locale;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListFactory;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListFragment;
import ca.rmen.android.poetassistant.main.reader.ReaderFragment;
import ca.rmen.android.poetassistant.settings.SettingsPrefs;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class PagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = Constants.TAG + PagerAdapter.class.getSimpleName();
    private static final String EXTRA_IS_PATTERN_TAB_VISIBLE = "is_pattern_tab_visible";

    private final Context mContext;
    private final SettingsPrefs mPrefs;
    private boolean mIsPatternTabVisible;
    private boolean mIsFavoritesTabVisible;
    private String mInitialPatternQuery;
    private String mInitialRhymeQuery;
    private String mInitialThesaurusQuery;
    private String mInitialDictionaryQuery;
    private String mInitialPoemText;

    PagerAdapter(Context context, FragmentManager fm, Intent intent) {
        super(fm);
        Log.v(TAG, "Constructor: intent = " + intent);
        mContext = context;
        mPrefs = SettingsPrefs.get(context.getApplicationContext());
        Uri initialQuery = intent.getData();
        // Deep link to query in a specific tab
        if (initialQuery != null) {
            Tab tab = Tab.parse(initialQuery.getHost());
            if (tab == Tab.PATTERN) {
                mInitialPatternQuery = initialQuery.getLastPathSegment();
            } else if (tab == Tab.RHYMER) {
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

        setFavoritesTabVisible(mPrefs.getIsFavoritesTabVisible());
    }

    public void setPatternTabVisible(boolean visible) {
        Log.v(TAG, "setPatternTabVisible: " + mIsPatternTabVisible + "->" + visible);
        if (mIsPatternTabVisible != visible) {
            if (visible) mIsFavoritesTabVisible = false;
            mIsPatternTabVisible = visible;
            notifyDataSetChanged();
        }
    }

    public void setFavoritesTabVisible(boolean visible) {
        Log.v(TAG, "setFavoritesTabVisible: " + mIsFavoritesTabVisible + "->" + visible);
        if (mIsFavoritesTabVisible != visible) {
            if (visible) mIsPatternTabVisible = false;
            mIsFavoritesTabVisible = visible;
            mPrefs.putIsFavoritesTabVisible(visible);
            notifyDataSetChanged();
        }
    }

    @Override
    public Fragment getItem(int position) {
        Log.v(TAG, "getItem " + position);
        Tab tab = getTabForPosition(position);
        if (tab == Tab.PATTERN) {
            return ResultListFactory.createListFragment(Tab.PATTERN, mInitialPatternQuery);
        } else if (tab == Tab.FAVORITES) {
                return ResultListFactory.createListFragment(Tab.FAVORITES, null);
        } else if (tab == Tab.RHYMER) {
            return ResultListFactory.createListFragment(Tab.RHYMER, mInitialRhymeQuery);
        } else if (tab == Tab.THESAURUS) {
            return ResultListFactory.createListFragment(Tab.THESAURUS, mInitialThesaurusQuery);
        } else if (tab == Tab.DICTIONARY) {
            return ResultListFactory.createListFragment(Tab.DICTIONARY, mInitialDictionaryQuery);
        } else {
            return ReaderFragment.newInstance(mInitialPoemText);
        }
    }

    @Override
    public int getItemPosition(Object object) {
        Log.v(TAG, "getItemPosition " + object);
        if (object instanceof ResultListFragment) {
            Tab tab = (Tab) ((ResultListFragment)object).getArguments().getSerializable(ResultListFragment.EXTRA_TAB);
            return getPositionForTab(tab);
        }
        if (object instanceof ReaderFragment) {
            return getPositionForTab(Tab.READER);
        }
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return mIsPatternTabVisible || mIsFavoritesTabVisible ? 5 : 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Tab tab = getTabForPosition(position);
        if (tab == Tab.PATTERN)
            return mContext.getString(R.string.tab_pattern).toUpperCase(Locale.getDefault());
        else if (tab == Tab.FAVORITES)
            return mContext.getString(R.string.tab_favorites).toUpperCase(Locale.getDefault());
        else if (tab == Tab.RHYMER)
            return mContext.getString(R.string.tab_rhymer).toUpperCase(Locale.getDefault());
        else if (tab == Tab.THESAURUS)
            return mContext.getString(R.string.tab_thesaurus).toUpperCase(Locale.getDefault());
        else if (tab == Tab.DICTIONARY)
            return mContext.getString(R.string.tab_dictionary).toUpperCase(Locale.getDefault());
        else
            return mContext.getString(R.string.tab_reader).toUpperCase(Locale.getDefault());
    }

    @Override
    public Parcelable saveState() {
        Bundle bundle = new Bundle(1);
        bundle.putBoolean(EXTRA_IS_PATTERN_TAB_VISIBLE, mIsPatternTabVisible);
        return bundle;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        Bundle bundle = (Bundle) state;
        boolean isPatternTabVisible = bundle.getBoolean(EXTRA_IS_PATTERN_TAB_VISIBLE);
        setPatternTabVisible(isPatternTabVisible);
        if (!isPatternTabVisible) {
            setFavoritesTabVisible(mPrefs.getIsFavoritesTabVisible());
        }
    }

    public Fragment getFragment(ViewGroup viewGroup, Tab tab) {
        Log.v(TAG, "getFragment: tab=" + tab);
        int position = getPositionForTab(tab);
        if (position < 0) return null;
        // Not intuitive: instantiateItem will actually return an existing Fragment, whereas getItem() will always instantiate a new Fragment.
        // We want to retrieve the existing fragment.
        return (Fragment) instantiateItem(viewGroup, position);
    }

    @Override
    public long getItemId(int position) {
        Tab tab = getTabForPosition(position);
        return tab.ordinal();
    }

    public Tab getTabForPosition(int position) {
        if (mIsPatternTabVisible) {
            if (position == 0) return Tab.PATTERN;
            return Tab.values()[position + 1];
        }
        if (mIsFavoritesTabVisible) {
            if (position == 0) return Tab.FAVORITES;
            return Tab.values()[position + 1];
        }

        return Tab.values()[position + 2];
    }

    public int getPositionForTab(Tab tab) {
        // If we're showing the pattern tab, we have:
        // pattern, rhymer, thesaurus, dictionary, reader
        if (mIsPatternTabVisible) {
            if (tab == Tab.PATTERN) return tab.ordinal();
            if (tab == Tab.FAVORITES) return POSITION_NONE;
            return tab.ordinal() - 1;
        }
        // If we're showing the favorites tab, we have:
        // favorites, rhymer, thesaurus, dictionary, reader
        if (mIsFavoritesTabVisible) {
            if (tab == Tab.PATTERN) return POSITION_NONE;
            if (tab == Tab.FAVORITES) return tab.ordinal() - 1;
            return tab.ordinal() - 1;
        }

        // By default we have rhymer, thesaurus, dictionary, reader
        if (tab == Tab.PATTERN || tab == Tab.FAVORITES) return POSITION_NONE;
        return tab.ordinal() - 2;
    }

}
