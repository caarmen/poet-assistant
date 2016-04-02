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

package ca.rmen.android.poetassistant.main.dictionaries;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;
import java.util.Locale;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.Tts;
import ca.rmen.android.poetassistant.main.Tab;


public class ResultListFragment<T> extends ListFragment
        implements
        LoaderManager.LoaderCallbacks<List<T>>,
        InputDialogFragment.InputDialogListener {
    private static final String TAG = Constants.TAG + ResultListFragment.class.getSimpleName();
    private static final int ACTION_FILTER = 0;
    private static final String DIALOG_TAG = "dialog";
    private static final String EXTRA_FILTER = "filter";
    static final String EXTRA_TAB = "tab";
    static final String EXTRA_QUERY = "query";
    private Tab mTab;
    private ArrayAdapter<T> mAdapter;
    private List<T> mData;
    private TextView mListHeaderTextView;
    private View mFilterView;
    private TextView mFilterTextView;
    private View mHeaderView;
    private View mPlayButton;
    private Tts mTts;
    private TextView mEmptyView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        mTab = (Tab) getArguments().getSerializable(EXTRA_TAB);
        View view = inflater.inflate(R.layout.fragment_result_list, container, false);
        mEmptyView = (TextView) view.findViewById(android.R.id.empty);
        mListHeaderTextView = (TextView) view.findViewById(R.id.tv_list_header);
        mHeaderView = view.findViewById(R.id.list_header);
        mFilterView = view.findViewById(R.id.filter);
        mFilterTextView = (TextView) view.findViewById(R.id.tv_filter);
        mPlayButton = view.findViewById(R.id.btn_play);
        mPlayButton.setOnClickListener(mPlayButtonListener);

        View filterButton = view.findViewById(R.id.btn_filter);
        filterButton.setOnClickListener(mFilterButtonListener);
        if (mTab == Tab.RHYMER || mTab == Tab.THESAURUS) filterButton.setVisibility(View.VISIBLE);
        TextView filterTextView = (TextView) view.findViewById(R.id.tv_filter_label);
        filterTextView.setText(ResultListFactory.getFilterLabel(getActivity(), mTab));

        view.findViewById(R.id.btn_clear).setOnClickListener(mClearButtonListener);
        view.findViewById(R.id.btn_web_search).setOnClickListener(mWebSearchButtonListener);

        if (savedInstanceState != null) {
            String query = savedInstanceState.getString(EXTRA_QUERY);
            String filter = savedInstanceState.getString(EXTRA_FILTER);
            mListHeaderTextView.setText(query);
            mFilterTextView.setText(filter);
            mFilterView.setVisibility(TextUtils.isEmpty(filter) ? View.GONE : View.VISIBLE);
        }

        EventBus.getDefault().register(this);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, mTab + ": onActivityCreated() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
        mTts = Tts.getInstance(getActivity());
        //noinspection unchecked
        mAdapter = (ArrayAdapter<T>) ResultListFactory.createAdapter(getActivity(), mTab);
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(mTab.ordinal(), getArguments(), this);
        updatePlayButton();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, mTab + ": onSaveInstanceState() called with: " + "outState = [" + outState + "]");
        outState.putString(EXTRA_QUERY, (String) mListHeaderTextView.getText());
        outState.putString(EXTRA_FILTER, (String) mFilterTextView.getText());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            Share.share(getActivity(), mTab,
                    mListHeaderTextView.getText().toString(),
                    mFilterTextView.getText().toString(),
                    mData);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_share).setEnabled(!mAdapter.isEmpty());
    }

    public void query(String query) {
        Log.d(TAG, mTab + ": query() called with: " + "query = [" + query + "]");
        mListHeaderTextView.setText(query);
        mFilterView.setVisibility(View.GONE);
        Bundle args = new Bundle(1);
        args.putString(EXTRA_QUERY, query);
        getLoaderManager().restartLoader(mTab.ordinal(), args, this);
    }

    private void filter(String filter) {
        Bundle args = new Bundle(2);
        args.putString(EXTRA_QUERY, mListHeaderTextView.getText().toString());
        args.putString(EXTRA_FILTER, filter);
        getLoaderManager().restartLoader(mTab.ordinal(), args, this);
    }

    private void requery() {
        Bundle args = new Bundle(2);
        args.putString(EXTRA_QUERY, mListHeaderTextView.getText().toString());
        args.putString(EXTRA_FILTER, mFilterTextView.getText().toString());
        getLoaderManager().restartLoader(mTab.ordinal(), args, this);
    }

    private void updatePlayButton() {
        int ttsStatus = mTts.getStatus();
        int playButtonVisibility = ttsStatus == TextToSpeech.SUCCESS ? View.VISIBLE : View.GONE;
        mPlayButton.setVisibility(playButtonVisibility);
    }

    @Override
    public Loader<List<T>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, mTab + ": onCreateLoader() called with: " + "id = [" + id + "], args = [" + args + "]");
        String query = "";
        String filter = "";
        if (args != null) {
            query = args.getString(EXTRA_QUERY);
            filter = args.getString(EXTRA_FILTER);
            mListHeaderTextView.setText(query);
        }
        //noinspection unchecked
        return (Loader<List<T>>) ResultListFactory.createLoader(mTab, getActivity(), query, filter);
    }

    @Override
    public void onLoadFinished(Loader<List<T>> loader, List<T> data) {
        Log.d(TAG, mTab + ": onLoadFinished() called with: " + "loader = [" + loader + "], data = [" + data + "]");
        mAdapter.clear();
        mAdapter.addAll(data);
        mData = data;
        updateUi();

        // Hide the keyboard
        getListView().requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getListView().getWindowToken(), 0);

        // If we have no results, try to look up results for the word stem,
        // except for the rhymer: we shouldn't strip suffixes when looking for rhymes.
        if (data.isEmpty() && mTab != Tab.RHYMER) {
            String initialQuery = mListHeaderTextView.getText().toString();
            String stem = new PorterStemmer().stemWord(initialQuery);
            if (!initialQuery.equals(stem)) {
                mListHeaderTextView.setText(stem);
                requery();
            }
        }
    }

    private void updateUi() {
        Log.d(TAG, mTab + ": updateUi() called with: " + "");
        int headerVisible = mAdapter.getCount() == 0 && TextUtils.isEmpty(mListHeaderTextView.getText().toString()) ?
                View.GONE : View.VISIBLE;
        mHeaderView.setVisibility(headerVisible);
        String query = mListHeaderTextView.getText().toString();
        // If we have an empty list because the user didn't enter any search term,
        // we'll show a text to tell them to search.
        if (TextUtils.isEmpty(query)) {
            String emptySearch = getString(R.string.empty_list_without_query);
            ImageSpan imageSpan = new ImageSpan(getActivity(), R.drawable.ic_action_search_dark);
            SpannableStringBuilder ssb = new SpannableStringBuilder(emptySearch);
            int iconIndex = emptySearch.indexOf("%s");
            ssb.setSpan(imageSpan, iconIndex, iconIndex + 2, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            mEmptyView.setText(ssb, TextView.BufferType.SPANNABLE);
        }
        // If the user entered a query and there are no matches, show the normal "no results" text.
        else {
            mEmptyView.setText(R.string.empty_list_with_query);
        }
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void onLoaderReset(Loader<List<T>> loader) {
        Log.d(TAG, mTab + ": onLoaderReset() called with: " + "loader = [" + loader + "]");
        mAdapter.clear();
        mData = null;
        updateUi();
    }

    @Override
    public void onInputSubmitted(int actionId, String input) {
        if (actionId == ACTION_FILTER) {
            if (!TextUtils.isEmpty(input)) {
                mFilterView.setVisibility(View.VISIBLE);
                String normalizedInput = input.toLowerCase(Locale.getDefault()).trim();
                mFilterTextView.setText(normalizedInput);
                filter(normalizedInput);
            } else {
                mFilterView.setVisibility(View.GONE);
                mFilterTextView.setText(null);
                filter(null);
            }
        }
    }

    private final View.OnClickListener mPlayButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mTts.speak(mListHeaderTextView.getText().toString());
        }
    };

    private final View.OnClickListener mWebSearchButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent searchIntent = new Intent(Intent.ACTION_WEB_SEARCH);
            String word = mListHeaderTextView.getText().toString();
            searchIntent.putExtra(SearchManager.QUERY, word);
            // No apps can handle ACTION_WEB_SEARCH.  We'll try a more generic intent instead
            if (getActivity().getPackageManager().queryIntentActivities(searchIntent, 0).isEmpty()) {
                searchIntent = new Intent(Intent.ACTION_SEND);
                searchIntent.setType("text/plain");
                searchIntent.putExtra(Intent.EXTRA_TEXT, word);
            }
            startActivity(Intent.createChooser(searchIntent, getString(R.string.action_web_search, word)));
        }
    };

    private final View.OnClickListener mFilterButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            InputDialogFragment fragment = ResultListFactory.createFilterDialog(
                    getActivity(),
                    mTab,
                    ACTION_FILTER,
                    mFilterTextView.getText().toString());
            getChildFragmentManager().beginTransaction().add(fragment, DIALOG_TAG).commit();
        }
    };

    private final View.OnClickListener mClearButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mFilterTextView.setText(null);
            mFilterView.setVisibility(View.GONE);
            filter(null);
        }
    };

    @SuppressWarnings("unused")
    @Subscribe
    public void onTtsInitialized(Tts.OnTtsInitialized event) {
        Log.d(TAG, mTab + ": onTtsInitialized() called with: " + "event = [" + event + "]");
        updatePlayButton();
    }

}
