/*
 * Copyright (c) 2016-present Carmen Alvarez
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
import android.content.Context
import android.content.Intent
import android.database.DataSetObserver
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import androidx.annotation.MainThread
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.Toolbar
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerTitleStrip
import com.google.android.material.tabs.TabLayout
import ca.rmen.android.poetassistant.BuildConfig
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.main.favorites.data.FavoritesRepository
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.about.AboutActivity
import ca.rmen.android.poetassistant.di.IODispatcher
import ca.rmen.android.poetassistant.getInsets
import ca.rmen.android.poetassistant.main.dictionaries.EmbeddedDb
import ca.rmen.android.poetassistant.main.dictionaries.rt.OnWordClickListener
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.usecases.GetRandomWordUseCase
import ca.rmen.android.poetassistant.main.dictionaries.search.Search
import ca.rmen.android.poetassistant.main.dictionaries.search.suggestions.data.SuggestionsRepository
import ca.rmen.android.poetassistant.main.dictionaries.search.suggestions.SuggestionsViewModel
import ca.rmen.android.poetassistant.main.reader.ReaderFragment
import ca.rmen.android.poetassistant.settings.SettingsActivity
import ca.rmen.android.poetassistant.settings.SettingsRepository
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.search.SearchView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// Split into separate impl and base class to get full code coverage stats:
// https://medium.com/livefront/dagger-hilt-testing-injected-android-components-with-code-coverage-30089a1f6872

@AndroidEntryPoint
class MainActivity : MainActivityImpl()

open class MainActivityImpl : AppCompatActivity(), OnWordClickListener, WarningNoSpaceDialogFragment.WarningNoSpaceDialogListener {
    companion object {
        private val TAG = Constants.TAG + MainActivity::class.java.simpleName
        private const val DIALOG_TAG = "dialog"
    }

    private lateinit var mSearch: Search
    private lateinit var mPagerAdapter: PagerAdapter
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var mEmbeddedDb: EmbeddedDb
    @Inject lateinit var getRandomWordUseCase: GetRandomWordUseCase
    @Inject lateinit var mFavoritesRepository: FavoritesRepository
    @Inject lateinit var suggestionsRepository: SuggestionsRepository

    @IODispatcher @Inject lateinit var ioDispatcher: CoroutineDispatcher

    private lateinit var appBarLayout: AppBarLayout
    private lateinit var searchSuggestionsList: RecyclerView
    private lateinit var tabs: TabLayout
    private lateinit var toolbar: Toolbar
    private lateinit var viewPager: ViewPager
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: savedInstanceState = $savedInstanceState")
        if (BuildConfig.DEBUG && ActivityManager.isUserAMonkey()) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pagerTitleStrip = findViewById<PagerTitleStrip>(R.id.pager_title_strip)
        appBarLayout = findViewById(R.id.app_bar_layout)
        searchSuggestionsList = findViewById(R.id.search_suggestions_list)
        searchView = findViewById(R.id.search_view)
        tabs = findViewById(R.id.tabs)
        toolbar = findViewById(R.id.toolbar)
        viewPager = findViewById(R.id.view_pager)

        setSupportActionBar(toolbar)
        tabs.tabMode = if (resources.getBoolean(R.bool.tab_text)) TabLayout.MODE_SCROLLABLE else TabLayout.MODE_FIXED
        pagerTitleStrip.visibility = if (resources.getBoolean(R.bool.tab_text)) View.GONE else View.VISIBLE
        mPagerAdapter = PagerAdapter(this, supportFragmentManager, intent)
        mPagerAdapter.registerDataSetObserver(mAdapterChangeListener)

        // Set up the ViewPager with the sections adapter.
        viewPager.adapter = mPagerAdapter
        viewPager.offscreenPageLimit = 5
        viewPager.addOnPageChangeListener(mOnPageChangeListener)

        tabs.setupWithViewPager(viewPager)
        val savedTab = settingsRepository.tab
        if (savedTab != null && savedTab.ordinal < mPagerAdapter.count) {
            viewPager.currentItem = savedTab.ordinal
        }
        mAdapterChangeListener.onChanged()

        // If the app was launched with a query for the a particular tab, focus on that tab.
        if (intent.data?.host != null) {
            val tab = Tab.parse(intent.data!!.host!!) ?: Tab.DICTIONARY
            viewPager.setCurrentItem(mPagerAdapter.getPositionForTab(tab), false)
        } else if (Intent.ACTION_SEND == intent.action) {
            viewPager.setCurrentItem(mPagerAdapter.getPositionForTab(Tab.READER), false)
        }

        mSearch = Search(this, viewPager, getRandomWordUseCase = getRandomWordUseCase, suggestionsRepository = suggestionsRepository)
        // Load our dictionaries when the activity starts, so that the first search can already be fast.
        lifecycleScope.launch {
            val dbLoaded = loadDatabase()
            onDatabaseLoadResult(dbLoaded)
        }
        volumeControlStream = AudioManager.STREAM_MUSIC
        getInsets(toolbar) { view, insets ->
            view.updatePadding(
                left = insets.left,
                right = insets.right,
            )
        }
        getInsets(appBarLayout) { view, insets ->
            view.updatePadding(
                left = insets.left,
                right = insets.right,
                top = insets.top,
            )
        }
        getInsets(searchSuggestionsList) { view, insets ->
            view.updatePadding(
                left = insets.left,
                right = insets.right,
            )
        }
        val suggestionsViewModel = ViewModelProvider(this).get(SuggestionsViewModel::class.java)
        mSearch.setSearchView(searchView, suggestionsViewModel, lifecycleScope)
    }

    override fun onResume() {
        super.onResume()
        Log.v(TAG, "onResume")
        // Weird bug that I don't understand :(
        // Open about (or settings) and come back to the main activity:
        // the AppBarLayout is hidden (even if it wasn't hidden before).
        // We'll force it to be shown again here.
        AppBarLayoutHelper.forceExpandAppBarLayout(appBarLayout)
    }

    override fun onPause() {
        Log.v(TAG, "onPause")
        super.onPause()
    }

    private suspend fun loadDatabase(): Boolean {
        return withContext(ioDispatcher) {
            mEmbeddedDb.isLoaded()
        }
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
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: intent=$intent")
        setIntent(intent)
        handleNewIntent(intent)
    }

    internal fun handleNewIntent(intent: Intent) {
        when (intent.action) {
            // We got here from a deep link
            Intent.ACTION_VIEW -> {
                handleDeepLink(intent.data)
            }
            // Load some shared text into the reader tab
            Intent.ACTION_SEND -> {
                viewPager.setCurrentItem(mPagerAdapter.getPositionForTab(Tab.READER), false)
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                val readerFragment = mPagerAdapter.getFragment(viewPager, Tab.READER) as ReaderFragment
                readerFragment.setPoem(sharedText)
            }
        }
    }

    private fun handleDeepLink(uri: Uri?) {
        Log.d(TAG, "handleDeepLink, uri=$uri")
        if (uri == null) return
        val word = uri.lastPathSegment
        if (Constants.DEEP_LINK_QUERY == uri.host) {
            viewPager.setCurrentItem(mPagerAdapter.getPositionForTab(Tab.DICTIONARY), false)
            word?.let { mSearch.search(it)}
        } else if (uri.host != null && word != null) {
            val tab = Tab.parse(uri.host!!)
            tab?.let {mSearch.search(word, it)}
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.d(TAG, "onCreateOptionsMenu, menu=$menu")
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> {
                // Route the share action to the visible tab. With the
                // ViewPager, several fragments are resumed at once, so we
                // cannot rely on the fragments consuming the item.
                val tab = mPagerAdapter.getTabForPosition(viewPager.currentItem)
                (mPagerAdapter.getFragment(viewPager, tab) as? TabMenuActions)?.onShare()
                return true
            }
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                return true
            }
            R.id.action_random_word -> {
                lifecycleScope.launch {
                    mSearch.lookupRandom()
                }
                return true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
            R.id.action_wotd_history -> {
                mPagerAdapter.setExtraTab(Tab.WOTD)
                viewPager.setCurrentItem(mPagerAdapter.getPositionForTab(Tab.WOTD), false)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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
                AppBarLayoutHelper.enableAutoHide(this@MainActivityImpl)
            } else {
                // Hide the keyboard when we navigate to any tab other than the reader tab.
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.hideSoftInputFromWindow(viewPager.windowToken, 0)
            }
            AppBarLayoutHelper.forceExpandAppBarLayout(appBarLayout)
            settingsRepository.tab = tab
        }
    }

    private val mAdapterChangeListener = object : DataSetObserver() {
        override fun onChanged() {
            for (i in 0 until tabs.tabCount) {
                val icon = mPagerAdapter.getIcon(i)
                val tab = tabs.getTabAt(i)
                if (tab != null) {
                    if (icon != null) {
                        tab.setIcon(icon)
                    }
                    if (!resources.getBoolean(R.bool.tab_text)) {
                        tab.text = null
                    }
                }
            }
        }
    }
}
