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

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import java.util.Locale;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.about.AboutActivity;
import ca.rmen.android.poetassistant.databinding.ActivityMainBinding;
import ca.rmen.android.poetassistant.main.dictionaries.Search;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary;
import ca.rmen.android.poetassistant.main.dictionaries.rt.OnWordClickedListener;
import ca.rmen.android.poetassistant.main.dictionaries.rt.Rhymer;
import ca.rmen.android.poetassistant.main.dictionaries.rt.Thesaurus;
import ca.rmen.android.poetassistant.main.reader.ReaderFragment;
import ca.rmen.android.poetassistant.settings.SettingsActivity;


public class MainActivity extends AppCompatActivity implements OnWordClickedListener, WarningNoSpaceDialogFragment.WarningNoSpaceDialogListener {

    private static final String TAG = Constants.TAG + MainActivity.class.getSimpleName();
    private static final String DIALOG_TAG = "dialog";

    private Search mSearch;
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(mBinding.toolbar);
        Intent intent = getIntent();
        Uri data = intent.getData();
        PagerAdapter pagerAdapter = new PagerAdapter(this, getSupportFragmentManager(), intent);

        // Set up the ViewPager with the sections adapter.
        mBinding.viewPager.setAdapter(pagerAdapter);
        mBinding.viewPager.setOffscreenPageLimit(3);
        mBinding.viewPager.addOnPageChangeListener(mOnPageChangeListener);

        mBinding.tabs.setupWithViewPager(mBinding.viewPager);

        // If the app was launched with a query for the thesaurus, focus on that tab.
        if (data != null) {
            if (data.getHost().equalsIgnoreCase(Constants.DEEP_LINK_QUERY)) {
                mBinding.viewPager.setCurrentItem(Tab.DICTIONARY.ordinal());
            } else if (data.getHost().equalsIgnoreCase(Tab.RHYMER.name().toLowerCase(Locale.US))) {
                mBinding.viewPager.setCurrentItem(Tab.RHYMER.ordinal());
            } else if (data.getHost().equalsIgnoreCase(Tab.THESAURUS.name().toLowerCase(Locale.US))) {
                mBinding.viewPager.setCurrentItem(Tab.THESAURUS.ordinal());
            }
        } else if (Intent.ACTION_SEND.equals(intent.getAction()))
            mBinding.viewPager.setCurrentItem(Tab.READER.ordinal());

        mSearch = new Search(this, mBinding.viewPager);
        loadDictionaries();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    /**
     * Load our dictionaries when the activity starts, so that the first search
     * can already be fast.
     */
    private void loadDictionaries() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                Rhymer rhymer = Rhymer.getInstance(getApplicationContext());
                Thesaurus thesaurus = Thesaurus.getInstance(getApplicationContext());
                Dictionary dictionary = Dictionary.getInstance(getApplicationContext());
                return rhymer.isLoaded() && thesaurus.isLoaded() && dictionary.isLoaded();
            }

            @Override
            protected void onPostExecute(Boolean allDictionariesAreLoaded) {
                Fragment warningNoSpaceDialogFragment = getSupportFragmentManager().findFragmentByTag(DIALOG_TAG);
                if (!allDictionariesAreLoaded
                        && warningNoSpaceDialogFragment == null) {
                    getSupportFragmentManager().beginTransaction().add(new WarningNoSpaceDialogFragment(), DIALOG_TAG).commit();
                } else if (allDictionariesAreLoaded
                        && warningNoSpaceDialogFragment != null){
                    getSupportFragmentManager().beginTransaction().remove(warningNoSpaceDialogFragment).commit();
                }
            }
        }.execute();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent() called with: " + "intent = [" + intent + "]");
        setIntent(intent);
        // The user entered a search term either by typing or by voice
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (TextUtils.isEmpty(query)) query = intent.getStringExtra(SearchManager.USER_QUERY);
            if (TextUtils.isEmpty(query)) return;
            mSearch.search(query);
            if (mBinding.viewPager.getCurrentItem() == Tab.READER.ordinal()) mBinding.viewPager.setCurrentItem(Tab.RHYMER.ordinal());
        }
        // We got here from a deep link
        else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = getIntent().getData();
            handleDeepLink(data);
        }
        // Play some text in the tts tab
        else if (Intent.ACTION_SEND.equals(intent.getAction())) {
            mBinding.viewPager.setCurrentItem(Tab.READER.ordinal());
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            ReaderFragment readerFragment = (ReaderFragment) mBinding.viewPager.getAdapter().instantiateItem(mBinding.viewPager, Tab.READER.ordinal());
            readerFragment.setText(sharedText);
        }
    }

    private void handleDeepLink(Uri uri) {
        Log.d(TAG, "handleDeepLink() called with: " + "uri = [" + uri + "]");
        if (uri == null) return;
        String word = uri.getLastPathSegment();
        if(Constants.DEEP_LINK_QUERY.equals(uri.getHost())) {
            mSearch.search(word);
            mBinding.viewPager.setCurrentItem(Tab.DICTIONARY.ordinal());
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
        } else if (item.getItemId() == R.id.action_clear_search_history) {
            mSearch.clearSearchHistory();
            Snackbar.make(mBinding.getRoot(), R.string.search_history_cleared, Snackbar.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWordClicked(String word, Tab tab) {
        Log.d(TAG, "onWordClicked() called with: " + "word = [" + word + "], tab = [" + tab + "]");
        mSearch.search(word, tab);
    }

    @Override
    public void onWarningNoSpaceDialogDismissed() {
        Log.v(TAG, "onWarningNoSpaceDialogDismissed");
        finish();
    }

    // Hide the keyboard when we navigate to any tab other than the reader tab.
    private final ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            if (position != Tab.READER.ordinal()) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mBinding.viewPager.getWindowToken(), 0);
            }
        }
    };

}
