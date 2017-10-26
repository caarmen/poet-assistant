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

package ca.rmen.android.poetassistant.main;

import android.app.ActivityManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.BuildConfig;
import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.dagger.DaggerHelper;
import ca.rmen.android.poetassistant.Favorites;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.about.AboutActivity;
import ca.rmen.android.poetassistant.databinding.ActivityMainBinding;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary;
import ca.rmen.android.poetassistant.main.dictionaries.rt.OnWordClickListener;
import ca.rmen.android.poetassistant.main.dictionaries.rt.Rhymer;
import ca.rmen.android.poetassistant.main.dictionaries.rt.Thesaurus;
import ca.rmen.android.poetassistant.main.dictionaries.search.Search;
import ca.rmen.android.poetassistant.main.reader.ReaderFragment;
import ca.rmen.android.poetassistant.settings.SettingsActivity;
import ca.rmen.android.poetassistant.widget.CABEditText;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity implements OnWordClickListener, WarningNoSpaceDialogFragment.WarningNoSpaceDialogListener, CABEditText.ImeListener {

    private static final String TAG = Constants.TAG + MainActivity.class.getSimpleName();
    private static final String DIALOG_TAG = "dialog";

    private Search mSearch;
    private ActivityMainBinding mBinding;
    private PagerAdapter mPagerAdapter;
    @Inject Rhymer mRhymer;
    @Inject Thesaurus mThesaurus;
    @Inject Dictionary mDictionary;
    @Inject Favorites mFavorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG && ActivityManager.isUserAMonkey()) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(mBinding.toolbar);
        Intent intent = getIntent();
        Uri data = intent.getData();
        mPagerAdapter = new PagerAdapter(this, getSupportFragmentManager(), intent);
        mPagerAdapter.registerDataSetObserver(mAdapterChangeListener);

        // Set up the ViewPager with the sections adapter.
        mBinding.viewPager.setAdapter(mPagerAdapter);
        mBinding.viewPager.setOffscreenPageLimit(5);
        mBinding.viewPager.addOnPageChangeListener(mOnPageChangeListener);

        mBinding.tabs.setupWithViewPager(mBinding.viewPager);
        mAdapterChangeListener.onChanged();

        // If the app was launched with a query for the a particular tab, focus on that tab.
        if (data != null && data.getHost() != null) {
            Tab tab = Tab.parse(data.getHost());
            if (tab == null) tab = Tab.DICTIONARY;
            mBinding.viewPager.setCurrentItem(mPagerAdapter.getPositionForTab(tab), false);
        } else if (Intent.ACTION_SEND.equals(intent.getAction())) {
            mBinding.viewPager.setCurrentItem(mPagerAdapter.getPositionForTab(Tab.READER), false);
        }

        mSearch = new Search(this, mBinding.viewPager);
        // Load our dictionaries when the activity starts, so that the first search can already be fast.
        Single.fromCallable(this::loadDatabase)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onDatabaseLoadResult);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
        // Weird bug that I don't understand :(
        // Open about (or settings) and come back to the main activity:
        // the AppBarLayout is hidden (even if it wasn't hidden before).
        // We'll force it to be shown again here.
        AppBarLayoutHelper.forceExpandAppBarLayout(mBinding.appBarLayout);
    }

    @Override
    public void onBackPressed() {
        Log.v(TAG, "onBackPressed");
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        Log.v(TAG, "onPause");
        super.onPause();
    }

    @WorkerThread
    private boolean loadDatabase() {
        DaggerHelper.getMainScreenComponent(getApplication()).inject(MainActivity.this);
        return mRhymer.isLoaded() && mThesaurus.isLoaded() && mDictionary.isLoaded();
    }

    @MainThread
    private void onDatabaseLoadResult(boolean databaseIsLoaded) {
        Fragment warningNoSpaceDialogFragment = getSupportFragmentManager().findFragmentByTag(DIALOG_TAG);
        if (!databaseIsLoaded
                && warningNoSpaceDialogFragment == null) {
            getSupportFragmentManager().beginTransaction().add(new WarningNoSpaceDialogFragment(), DIALOG_TAG).commit();
        } else if (databaseIsLoaded
                && warningNoSpaceDialogFragment != null){
            getSupportFragmentManager().beginTransaction().remove(warningNoSpaceDialogFragment).commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent() called with: " + "intent = [" + intent + "]");
        setIntent(intent);
        // The user entered a search term either by typing or by voice
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getDataString();
            if (TextUtils.isEmpty(query)) {
                query = intent.getStringExtra(SearchManager.QUERY);
            }
            if (TextUtils.isEmpty(query)) {
                CharSequence userQuery = intent.getCharSequenceExtra(SearchManager.USER_QUERY);
                if (!TextUtils.isEmpty(userQuery)) query = userQuery.toString();
            }
            if (TextUtils.isEmpty(query)) return;
            mSearch.addSuggestions(query);
            mSearch.search(query);
        }
        // We got here from a deep link
        else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = getIntent().getData();
            handleDeepLink(data);
        }
        // Load some shared text into the reader tab
        else if (Intent.ACTION_SEND.equals(intent.getAction())) {
            mBinding.viewPager.setCurrentItem(mPagerAdapter.getPositionForTab(Tab.READER), false);
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            ReaderFragment readerFragment = (ReaderFragment) mPagerAdapter.getFragment(mBinding.viewPager, Tab.READER);
            readerFragment.setText(sharedText);
        }
    }

    private void handleDeepLink(Uri uri) {
        Log.d(TAG, "handleDeepLink() called with: " + "uri = [" + uri + "]");
        if (uri == null) return;
        String word = uri.getLastPathSegment();
        if(Constants.DEEP_LINK_QUERY.equals(uri.getHost())) {
            mBinding.viewPager.setCurrentItem(mPagerAdapter.getPositionForTab(Tab.DICTIONARY), false);
            mSearch.search(word);
        } else {
            Tab tab = Tab.parse(uri.getHost());
            if (tab != null) mSearch.search(word, tab);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu() called with: " + "menu = [" + menu + "]");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mSearch.setSearchView(searchView);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.action_random_word) {
            mSearch.lookupRandom();
            return true;
        } else if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.action_wotd_history) {
            mPagerAdapter.setExtraTab(Tab.WOTD);
            mBinding.viewPager.setCurrentItem(mPagerAdapter.getPositionForTab(Tab.WOTD), false);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWordClick(String word, Tab tab) {
        Log.d(TAG, "onWordClick() called with: " + "word = [" + word + "], tab = [" + tab + "]");
        mSearch.search(word, tab);
    }

    @Override
    public void onWarningNoSpaceDialogDismissed() {
        Log.v(TAG, "onWarningNoSpaceDialogDismissed");
        finish();
    }

    @Override
    public void onImeClosed() {
        // In the reader fragment, when the user taps on the EditText, the soft keyboard is opened, and UI scrolls up, hiding the AppBarLayout.
        // When the user taps back to close the soft keyboard, we should show the AppBarLayout again, or else the only way
        // for the user to access the actionbar + tabs would be to swipe left or right to another fragment.
        AppBarLayoutHelper.forceExpandAppBarLayout(mBinding.appBarLayout);
    }


    private final ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);

            Tab tab = mPagerAdapter.getTabForPosition(position);

            if (tab == Tab.READER) {
                AppBarLayoutHelper.enableAutoHide(MainActivity.this);
            } else {
                // Hide the keyboard when we navigate to any tab other than the reader tab.
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mBinding.viewPager.getWindowToken(), 0);
            }
            AppBarLayoutHelper.forceExpandAppBarLayout(mBinding.appBarLayout);
        }
    };

    private final DataSetObserver mAdapterChangeListener = new DataSetObserver() {
        @Override
        public void onChanged() {
            for (int i=0; i < mBinding.tabs.getTabCount(); i++) {
                Drawable icon = mPagerAdapter.getIcon(i);
                TabLayout.Tab tab = mBinding.tabs.getTabAt(i);
                if (tab != null) {
                    if (icon != null) {
                        tab.setIcon(icon);
                    }
                    if (!getResources().getBoolean(R.bool.tab_text)) {
                        tab.setText(null);
                    }
                }
            }
        }
    };

}
