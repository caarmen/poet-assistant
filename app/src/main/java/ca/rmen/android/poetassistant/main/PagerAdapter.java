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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.ViewGroup;

import java.util.Locale;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListFactory;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListFragment;
import ca.rmen.android.poetassistant.main.reader.ReaderFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class PagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = Constants.TAG + PagerAdapter.class.getSimpleName();
    private static final String EXTRA_IS_PATTERN_TAB_VISIBLE = "is_pattern_tab_visible";

    private final Context mContext;
    private boolean mIsPatternTabVisible;
    private String mInitialPatternQuery;
    private String mInitialRhymeQuery;
    private String mInitialThesaurusQuery;
    private String mInitialDictionaryQuery;
    private String mInitialPoemText;

    PagerAdapter(Context context, FragmentManager fm, Intent intent) {
        super(fm);
        Log.v(TAG, "Constructor: intent = " + intent);
        mContext = context;
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
    }

    public void setPatternTabVisible(boolean visible) {
        Log.v(TAG, "setPatternTabVisible: " + mIsPatternTabVisible + "->" + visible);
        if (mIsPatternTabVisible != visible) {
            mIsPatternTabVisible = visible;
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
        return mIsPatternTabVisible ? 6 : 5;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Tab tab = getTabForPosition(position);
        return ResultListFactory.getTabName(mContext, tab);
    }

    Drawable getIcon(int position) {
        if (!mContext.getResources().getBoolean(R.bool.tab_icons)) return null;
        Tab tab = getTabForPosition(position);
        if (tab == Tab.PATTERN)
            return getTintedIcon(R.drawable.ic_pattern);
        else if (tab == Tab.FAVORITES)
            return getTintedIcon(R.drawable.ic_star_activated_vector);
        else if (tab == Tab.RHYMER)
            return getTintedIcon(R.drawable.ic_rhymer);
        else if (tab == Tab.THESAURUS)
            return getTintedIcon(R.drawable.ic_thesaurus);
        else if (tab == Tab.DICTIONARY)
            return getTintedIcon(R.drawable.ic_dictionary);
        else
            return getTintedIcon(R.drawable.ic_play_enabled);
    }

    private Drawable getTintedIcon(@DrawableRes int drawableRes) {
        Drawable drawable = VectorDrawableCompat.create(mContext.getResources(), drawableRes, null).mutate();
        DrawableCompat.setTintList(drawable, ContextCompat.getColorStateList(mContext, R.color.tab_icon));
        return drawable;
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
        return Tab.values()[position];
    }

    public int getPositionForTab(Tab tab) {
        if (tab == Tab.PATTERN) {
            return mIsPatternTabVisible ? tab.ordinal() : POSITION_NONE;
        }
        return tab.ordinal();
    }

}
