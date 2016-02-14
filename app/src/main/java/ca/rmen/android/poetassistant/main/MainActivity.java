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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.about.AboutActivity;


public class MainActivity extends AppCompatActivity implements OnWordClickedListener {

    private static final String TAG = Constants.TAG + MainActivity.class.getSimpleName();

    private Search mSearch;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        Uri data = intent.getData();
        PagerAdapter pagerAdapter = new PagerAdapter(this, getSupportFragmentManager(), intent);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setOffscreenPageLimit(2);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // If the app was launched with a query for the thesaurus, focus on that tab.
        if (data != null && data.getHost().equalsIgnoreCase(Tab.THESAURUS.name()))
            mViewPager.setCurrentItem(Tab.THESAURUS.ordinal());
        else if (Intent.ACTION_SEND.equals(intent.getAction()))
            mViewPager.setCurrentItem(Tab.READER.ordinal());

        mSearch = new Search(this, mViewPager);
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
        }
        // We got here from a deep link
        else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = getIntent().getData();
            if (data != null) {
                Tab tab = Tab.parse(data.getHost());
                if (tab != null) mSearch.search(data.getLastPathSegment(), tab);
            }
        }
        // Play some text in the tts tab
        else if (Intent.ACTION_SEND.equals(intent.getAction())) {
            mViewPager.setCurrentItem(Tab.READER.ordinal());
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            TtsFragment ttsFragment = (TtsFragment) mViewPager.getAdapter().instantiateItem(mViewPager, Tab.RHYMER.ordinal());
            ttsFragment.speak(sharedText);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu() called with: " + "menu = [" + menu + "]");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mSearch.setSearchView(searchView);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.action_clear_search_history) {
            mSearch.clearSearchHistory();
            View rootView = findViewById(android.R.id.content);
            Snackbar.make(rootView, R.string.search_history_cleared, Snackbar.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWordClicked(String word, Tab tab) {
        Log.d(TAG, "onWordClicked() called with: " + "word = [" + word + "], tab = [" + tab + "]");
        mSearch.search(word, tab);
    }

}
