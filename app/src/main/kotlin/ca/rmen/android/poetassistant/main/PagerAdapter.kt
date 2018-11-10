/*
 * Copyright (c) 2016 - 2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.DrawableRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.Log
import android.view.ViewGroup
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.dictionaries.ResultListFactory
import ca.rmen.android.poetassistant.main.dictionaries.ResultListFragment
import ca.rmen.android.poetassistant.main.reader.ReaderFragment
import java.util.Locale

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class PagerAdapter// Text shared from another app:// Deep link to query in a specific tab
(context: Context, fm: FragmentManager, intent: Intent) : FragmentPagerAdapter(fm) {
    companion object {
        private val TAG = Constants.TAG + PagerAdapter::class.java.simpleName
        private const val EXTRA_EXTRA_TAB = "extra_tab"
    }

    private val mContext: Context = context
    private var mExtraTab: Tab? = null
    private var mInitialPatternQuery: String? = null
    private var mInitialRhymeQuery: String? = null
    private var mInitialThesaurusQuery: String? = null
    private var mInitialDictionaryQuery: String? = null
    private var mInitialPoemText: String? = null

    fun setExtraTab(tab: Tab?) {
        Log.v(TAG, "setExtraTab $tab")
        if (mExtraTab != tab) {
            mExtraTab = tab
            notifyDataSetChanged()
        }
    }

    override fun getItem(position: Int): Fragment {
        Log.v(TAG, "getItem $position")
        return when (getTabForPosition(position)) {
            Tab.PATTERN -> ResultListFactory.createListFragment(Tab.PATTERN, mInitialPatternQuery)
            Tab.FAVORITES -> ResultListFactory.createListFragment(Tab.FAVORITES, null)
            Tab.WOTD -> ResultListFactory.createListFragment(Tab.WOTD, null)
            Tab.RHYMER -> ResultListFactory.createListFragment(Tab.RHYMER, mInitialRhymeQuery)
            Tab.THESAURUS -> ResultListFactory.createListFragment(Tab.THESAURUS, mInitialThesaurusQuery)
            Tab.DICTIONARY -> ResultListFactory.createListFragment(Tab.DICTIONARY, mInitialDictionaryQuery)
            else -> ReaderFragment.newInstance(mInitialPoemText)
        }
    }

    override fun getItemPosition(obj: Any): Int {
        Log.v(TAG, "getItemPosition $obj")
        if (obj is ResultListFragment<*>) {
            val arguments = obj.arguments
            if (arguments != null) {
                val tab = arguments.getSerializable(ResultListFragment.EXTRA_TAB) as Tab
                return getPositionForTab(tab)
            }
        }
        if (obj is ReaderFragment) {
            return getPositionForTab(Tab.READER)
        }
        return android.support.v4.view.PagerAdapter.POSITION_NONE
    }

    override fun getCount(): Int = if (mExtraTab != null) 6 else 5

    override fun getPageTitle(position: Int): CharSequence {
        val tab = getTabForPosition(position)
        return ResultListFactory.getTabName(mContext, tab).toUpperCase(Locale.getDefault())
    }

    @DrawableRes
    fun getIcon(position: Int): Int? {
        if (!mContext.resources.getBoolean(R.bool.tab_icons)) return null
        val tab = getTabForPosition(position)
        return when (tab) {
            Tab.PATTERN -> R.drawable.ic_tab_pattern
            Tab.FAVORITES -> R.drawable.ic_tab_star
            Tab.WOTD -> R.drawable.ic_tab_wotd
            Tab.RHYMER -> R.drawable.ic_tab_rhymer
            Tab.THESAURUS -> R.drawable.ic_tab_thesaurus
            Tab.DICTIONARY -> R.drawable.ic_tab_dictionary
            else -> R.drawable.ic_tab_reader
        }
    }

    override fun saveState(): Parcelable? {
        val bundle = Bundle(1)
        if (mExtraTab != null) bundle.putSerializable(EXTRA_EXTRA_TAB, mExtraTab)
        return bundle
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        val bundle = state as Bundle
        if (bundle.containsKey(EXTRA_EXTRA_TAB)) {
            mExtraTab = bundle.getSerializable(EXTRA_EXTRA_TAB) as Tab
            notifyDataSetChanged()
        }
    }

    fun getFragment(viewGroup: ViewGroup, tab: Tab): Fragment? {
        Log.v(TAG, "getFragment: tab=$tab")
        val position = getPositionForTab(tab)
        if (position < 0) return null
        // Not intuitive: instantiateItem will actually return an existing Fragment, whereas getItem() will always instantiate a new Fragment.
        // We want to retrieve the existing fragment.
        return instantiateItem(viewGroup, position) as Fragment
    }

    override fun getItemId(position: Int): Long {
        return getTabForPosition(position).ordinal.toLong()
    }

    fun getTabForPosition(position: Int): Tab {
        if (mExtraTab != null && position == count - 1) return mExtraTab!!
        return Tab.values()[position]
    }

    fun getPositionForTab(tab: Tab): Int {
        if (tab == Tab.PATTERN || tab == Tab.WOTD) {
            return if (mExtraTab == tab) count - 1 else POSITION_NONE
        }
        return tab.ordinal
    }

    init {
        Log.v(TAG, "Constructor: intent = $intent")
        val initialQuery = intent.data
        if (initialQuery?.host != null) {
            val tab = Tab.parse(initialQuery.host!!)
            when {
                tab == Tab.PATTERN -> mInitialPatternQuery = initialQuery.lastPathSegment
                tab == Tab.RHYMER -> mInitialRhymeQuery = initialQuery.lastPathSegment
                tab == Tab.THESAURUS -> mInitialThesaurusQuery = initialQuery.lastPathSegment
                tab == Tab.DICTIONARY -> mInitialDictionaryQuery = initialQuery.lastPathSegment
                Constants.DEEP_LINK_QUERY == initialQuery.host -> {
                    mInitialRhymeQuery = initialQuery.lastPathSegment
                    mInitialThesaurusQuery = initialQuery.lastPathSegment
                    mInitialDictionaryQuery = initialQuery.lastPathSegment
                }
            }
        }
        // Text shared from another app:
        else if (Intent.ACTION_SEND == intent.action) {
            mInitialPoemText = intent.getStringExtra(Intent.EXTRA_TEXT)
        }
    }
}
