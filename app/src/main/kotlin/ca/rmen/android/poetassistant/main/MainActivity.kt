/*
 * Copyright (c) 2016-2017 Carmen Alvarez
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

import android.app.ActivityManager
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.database.DataSetObserver
import android.databinding.DataBindingUtil
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import ca.rmen.android.poetassistant.BuildConfig
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.Favorites
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.Threading
import ca.rmen.android.poetassistant.about.AboutActivity
import ca.rmen.android.poetassistant.dagger.DaggerHelper
import ca.rmen.android.poetassistant.databinding.ActivityMainBinding
import ca.rmen.android.poetassistant.main.dictionaries.ResultListFragment
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary
import ca.rmen.android.poetassistant.main.dictionaries.rt.OnWordClickListener
import ca.rmen.android.poetassistant.main.dictionaries.rt.Rhymer
import ca.rmen.android.poetassistant.main.dictionaries.rt.Thesaurus
import ca.rmen.android.poetassistant.main.dictionaries.search.Search
import ca.rmen.android.poetassistant.main.reader.ReaderFragment
import ca.rmen.android.poetassistant.settings.Settings
import ca.rmen.android.poetassistant.settings.SettingsActivity
import ca.rmen.android.poetassistant.settings.SettingsPrefs
import ca.rmen.android.poetassistant.widget.CABEditText
import javax.inject.Inject

class MainActivity : AppCompatActivity(), OnWordClickListener, WarningNoSpaceDialogFragment.WarningNoSpaceDialogListener, CABEditText.ImeListener {
    companion object {
        private val TAG = Constants.TAG + MainActivity::class.java.simpleName
        private const val DIALOG_TAG = "dialog"
    }

    private lateinit var mSearch: Search
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mPagerAdapter: PagerAdapter
    @Inject lateinit var mPrefs: SettingsPrefs
    @Inject lateinit var mRhymer: Rhymer
    @Inject lateinit var mThesaurus: Thesaurus
    @Inject lateinit var mDictionary: Dictionary
    @Inject lateinit var mFavorites: Favorites
    @Inject lateinit var mThreading: Threading

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: savedInstanceState = $savedInstanceState")
        if (BuildConfig.DEBUG && ActivityManager.isUserAMonkey()) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        super.onCreate(savedInstanceState)
        DaggerHelper.getMainScreenComponent(application).inject(this)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(mBinding.toolbar)
        mPagerAdapter = PagerAdapter(this, supportFragmentManager, intent)
        mPagerAdapter.registerDataSetObserver(mAdapterChangeListener)

        // Set up the ViewPager with the sections adapter.
        mBinding.viewPager.adapter = mPagerAdapter
        mBinding.viewPager.offscreenPageLimit = 5
        mBinding.viewPager.addOnPageChangeListener(mOnPageChangeListener)

        mBinding.tabs.setupWithViewPager(mBinding.viewPager)
        val savedTab = Settings.getTab(mPrefs)
        if (savedTab != null && savedTab.ordinal < mPagerAdapter.count) {
            mBinding.viewPager.currentItem = savedTab.ordinal
        }
        mAdapterChangeListener.onChanged()

        // If the app was launched with a query for the a particular tab, focus on that tab.
        if (intent.data?.host != null) {
            val tab = Tab.parse(intent.data!!.host!!) ?: Tab.DICTIONARY
            mBinding.viewPager.setCurrentItem(mPagerAdapter.getPositionForTab(tab), false)
        } else if (Intent.ACTION_SEND == intent.action) {
            mBinding.viewPager.setCurrentItem(mPagerAdapter.getPositionForTab(Tab.READER), false)
        }

        mSearch = Search(this, mBinding.viewPager)
        // Load our dictionaries when the activity starts, so that the first search can already be fast.
        mThreading.execute({ loadDatabase() },
                { onDatabaseLoadResult(it) })
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onResume() {
        super.onResume()
        Log.v(TAG, "onResume")
        // Weird bug that I don't understand :(
        // Open about (or settings) and come back to the main activity:
        // the AppBarLayout is hidden (even if it wasn't hidden before).
        // We'll force it to be shown again here.
        AppBarLayoutHelper.forceExpandAppBarLayout(mBinding.appBarLayout)
    }

    override fun onBackPressed() {
        Log.v(TAG, "onBackPressed")
        super.onBackPressed()
    }

    override fun onPause() {
        Log.v(TAG, "onPause")
        super.onPause()
    }

    @WorkerThread
    private fun loadDatabase(): Boolean {
        return mRhymer.isLoaded() && mThesaurus.isLoaded() && mDictionary.isLoaded()
    }

    @MainThread
    private fun onDatabaseLoadResult(databaseIsLoaded: Boolean) {
        val warningNoSpaceDialogFragment = supportFragmentManager.findFragmentByTag(DIALOG_TAG)
        if (!databaseIsLoaded && warningNoSpaceDialogFragment == null) {
            supportFragmentManager.beginTransaction().add(WarningNoSpaceDialogFragment(), DIALOG_TAG).commit()
        } else if (databaseIsLoaded && warningNoSpaceDialogFragment != null) {
            supportFragmentManager.beginTransaction().remove(warningNoSpaceDialogFragment).commit()
        }
    }

    override fun onNewIntent(intent: Intent) {
        Log.d(TAG, "onNewIntent: intent=$intent")
        setIntent(intent)
        when (intent.action) {
        // The user entered a search term either by typing or by voice
            Intent.ACTION_SEARCH -> {
                var query = intent.dataString
                if (TextUtils.isEmpty(query)) {
                    query = intent.getStringExtra(SearchManager.QUERY)
                }
                if (TextUtils.isEmpty(query)) {
                    val userQuery = intent.getCharSequenceExtra(SearchManager.USER_QUERY)
                    if (!TextUtils.isEmpty(userQuery)) query = userQuery.toString()
                }
                if (TextUtils.isEmpty(query)) return
                mSearch.addSuggestions(query!!)
                mSearch.search(query)
            }
        // We got here from a deep link
            Intent.ACTION_VIEW -> {
                handleDeepLink(intent.data)
            }
        // Load some shared text into the reader tab
            Intent.ACTION_SEND -> {
                mBinding.viewPager.setCurrentItem(mPagerAdapter.getPositionForTab(Tab.READER), false)
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                val readerFragment = mPagerAdapter.getFragment(mBinding.viewPager, Tab.READER) as ReaderFragment
                readerFragment.setText(sharedText)

            }
        }
    }

    private fun handleDeepLink(uri: Uri?) {
        Log.d(TAG, "handleDeepLink, uri=$uri")
        if (uri == null) return
        val word = uri.lastPathSegment
        if (Constants.DEEP_LINK_QUERY == uri.host) {
            mBinding.viewPager.setCurrentItem(mPagerAdapter.getPositionForTab(Tab.DICTIONARY), false)
            word?.let { mSearch.search(it)}
        } else if (uri.host != null && word != null) {
            val tab = Tab.parse(uri.host!!)
            tab?.let {mSearch.search(word, it)}
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.d(TAG, "onCreateOptionsMenu, menu=$menu")
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        mSearch.setSearchView(searchView)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                return true
            }
            R.id.action_random_word -> {
                mSearch.lookupRandom()
                return true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
            R.id.action_wotd_history -> {
                mPagerAdapter.setExtraTab(Tab.WOTD)
                mBinding.viewPager.setCurrentItem(mPagerAdapter.getPositionForTab(Tab.WOTD), false)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onImeClosed() {
        // In the reader fragment, when the user taps on the EditText, the soft keyboard is opened, and UI scrolls up, hiding the AppBarLayout.
        // When the user taps back to close the soft keyboard, we should show the AppBarLayout again, or else the only way
        // for the user to access the actionbar + tabs would be to swipe left or right to another fragment.
        AppBarLayoutHelper.forceExpandAppBarLayout(mBinding.appBarLayout)
    }

    override fun onWordClick(word: String, tab: Tab) {
        Log.v(TAG, "onWordClick: word=$word, tab=$tab")
        mSearch.search(word, tab)
    }

    override fun onWarningNoSpaceDialogDismissed() {
        Log.v(TAG, "onWarningNoSpaceDialogDismissed")
        finish()
    }

    private val mOnPageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            val tab = mPagerAdapter.getTabForPosition(position)

            if (tab == Tab.READER) {
                AppBarLayoutHelper.enableAutoHide(this@MainActivity)
            } else {
                // Hide the keyboard when we navigate to any tab other than the reader tab.
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.hideSoftInputFromWindow(mBinding.viewPager.windowToken, 0)
            }
            val fragment = mPagerAdapter.getFragment(mBinding.viewPager, tab)
            (fragment as? ResultListFragment<*>)?.enableAutoHideIfNeeded()

            AppBarLayoutHelper.forceExpandAppBarLayout(mBinding.appBarLayout)
            mPrefs.tab = tab.name
        }
    }

    private val mAdapterChangeListener = object : DataSetObserver() {
        override fun onChanged() {
            for (i in 0 until mBinding.tabs.tabCount) {
                val icon = mPagerAdapter.getIcon(i)
                val tab = mBinding.tabs.getTabAt(i)
                if (tab != null) {
                    if (icon != null) {
                        tab.icon = icon
                    }
                    if (!resources.getBoolean(R.bool.tab_text)) {
                        tab.text = null
                    }
                }
            }
        }
    }
}
