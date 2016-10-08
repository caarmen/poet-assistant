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
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
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
import android.widget.CheckBox;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.Locale;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.Tts;
import ca.rmen.android.poetassistant.VectorCompat;
import ca.rmen.android.poetassistant.databinding.FragmentResultListBinding;
import ca.rmen.android.poetassistant.main.HelpDialogFragment;
import ca.rmen.android.poetassistant.main.Tab;
import ca.rmen.android.poetassistant.main.dictionaries.rt.OnFavoriteClickListener;


public class ResultListFragment<T> extends Fragment
        implements
        LoaderManager.LoaderCallbacks<ResultListData<T>>,
        InputDialogFragment.InputDialogListener {
    private static final String TAG = Constants.TAG + ResultListFragment.class.getSimpleName();
    private static final int ACTION_FILTER = 0;
    private static final String DIALOG_TAG = "dialog";
    private static final String EXTRA_FILTER = "filter";
    public static final String EXTRA_TAB = "tab";
    static final String EXTRA_QUERY = "query";
    private FragmentResultListBinding mBinding;
    private final HeaderButtonListener mHeaderButtonListener = new HeaderButtonListener();

    private Tab mTab;
    private ResultListAdapter<T> mAdapter;
    private ResultListData<T> mData;
    private Tts mTts;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        mTab = (Tab) getArguments().getSerializable(EXTRA_TAB);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_result_list, container, false);
        View view = mBinding.getRoot();
        mBinding.setHeaderButtonListener(mHeaderButtonListener);
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mBinding.recyclerView.setHasFixedSize(true);

        ResultListFactory.updateListHeaderButtonsVisbility(mBinding, mTab, TextToSpeech.ERROR);
        mBinding.tvFilterLabel.setText(ResultListFactory.getFilterLabel(getActivity(), mTab));


        if (savedInstanceState != null) {
            String query = savedInstanceState.getString(EXTRA_QUERY);
            String filter = savedInstanceState.getString(EXTRA_FILTER);
            mBinding.tvListHeader.setText(query);
            mBinding.tvFilter.setText(filter);
            mBinding.filter.setVisibility(TextUtils.isEmpty(filter) ? View.GONE : View.VISIBLE);
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
        mAdapter = (ResultListAdapter<T>) ResultListFactory.createAdapter(getActivity(), mTab);
        mBinding.recyclerView.setAdapter(mAdapter);
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
        outState.putString(EXTRA_QUERY, mBinding.tvListHeader.getText().toString());
        outState.putString(EXTRA_FILTER, mBinding.tvFilter.getText().toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            Share.share(getActivity(), mTab,
                    mBinding.tvListHeader.getText().toString(),
                    mBinding.tvFilter.getText().toString(),
                    mData.data);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_share).setEnabled(mAdapter.getItemCount() > 0);
    }

    public void query(String query) {
        Log.d(TAG, mTab + ": query() called with: " + "query = [" + query + "]");
        mBinding.tvListHeader.setText(query);
        mBinding.filter.setVisibility(View.GONE);
        Bundle args = new Bundle(1);
        args.putString(EXTRA_QUERY, query);
        getLoaderManager().restartLoader(mTab.ordinal(), args, this);
    }

    private void filter(String filter) {
        Bundle args = new Bundle(2);
        args.putString(EXTRA_QUERY, mBinding.tvListHeader.getText().toString());
        args.putString(EXTRA_FILTER, filter);
        getLoaderManager().restartLoader(mTab.ordinal(), args, this);
    }

    private void updatePlayButton() {
        ResultListFactory.updateListHeaderButtonsVisbility(mBinding, mTab, mTts.getStatus());
    }

    @Override
    public Loader<ResultListData<T>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, mTab + ": onCreateLoader() called with: " + "id = [" + id + "], args = [" + args + "]");
        mAdapter.clear();
        mData = null;

        String query = "";
        String filter = "";
        if (args != null) {
            query = args.getString(EXTRA_QUERY);
            filter = args.getString(EXTRA_FILTER);
            mBinding.tvListHeader.setText(query);
            mData = new ResultListData<>(query, false, Collections.emptyList());
        }
        mBinding.empty.setVisibility(View.GONE);
        mBinding.listHeader.setVisibility(View.VISIBLE);
        mBinding.recyclerView.scrollToPosition(0); // why do I have to do this?

        getActivity().supportInvalidateOptionsMenu();

        //noinspection unchecked
        return (Loader<ResultListData<T>>) ResultListFactory.createLoader(mTab, getActivity(), query, filter);
    }

    @Override
    public void onLoadFinished(Loader<ResultListData<T>> loader, ResultListData<T> data) {
        Log.d(TAG, mTab + ": onLoadFinished() called with: " + "loader = [" + loader + "], data = [" + data + "]");
        mAdapter.clear();
        //noinspection unchecked
        mAdapter.addAll(data.data);
        mData = data;
        updateUi();

        // Hide the keyboard
        mBinding.recyclerView.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mBinding.recyclerView.getWindowToken(), 0);
    }

    private void updateUi() {
        Log.d(TAG, mTab + ": updateUi() called with: " + "");
        int headerVisible = mAdapter.getItemCount() == 0 && TextUtils.isEmpty(mBinding.tvListHeader.getText().toString()) ?
                View.GONE : View.VISIBLE;
        mBinding.listHeader.setVisibility(headerVisible);

        String query = mData == null ? null : mData.matchedWord;
        mBinding.tvListHeader.setText(query);
        // If we have an empty list because the user didn't enter any search term,
        // we'll show a text to tell them to search.
        if (TextUtils.isEmpty(query)) {
            String emptySearch = getString(R.string.empty_list_without_query);
            ImageSpan imageSpan = VectorCompat.createVectorImageSpan(getActivity(), R.drawable.ic_action_search_dark);
            SpannableStringBuilder ssb = new SpannableStringBuilder(emptySearch);
            int iconIndex = emptySearch.indexOf("%s");
            ssb.setSpan(imageSpan, iconIndex, iconIndex + 2, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            mBinding.empty.setText(ssb, TextView.BufferType.SPANNABLE);
        }
        // If the user entered a query and there are no matches, show the normal "no results" text.
        else {
            String noResults = ResultListFactory.getEmptyListText(getContext(), mTab, query);
            mBinding.empty.setText(noResults);
        }
        if (mData == null || mData.data.isEmpty()) {
            mBinding.empty.setVisibility(View.VISIBLE);
            mBinding.recyclerView.setVisibility(View.GONE);
        } else {
            mBinding.empty.setVisibility(View.GONE);
            mBinding.recyclerView.setVisibility(View.VISIBLE);
        }

        if (mData != null) mBinding.btnStarQuery.setChecked(mData.isFavorite);

        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void onLoaderReset(Loader<ResultListData<T>> loader) {
        Log.d(TAG, mTab + ": onLoaderReset() called with: " + "loader = [" + loader + "]");
        mAdapter.clear();
        mData = null;
        updateUi();
    }

    @Override
    public void onInputSubmitted(int actionId, String input) {
        if (actionId == ACTION_FILTER) {
            if (!TextUtils.isEmpty(input)) {
                mBinding.filter.setVisibility(View.VISIBLE);
                String normalizedInput = input.toLowerCase(Locale.getDefault()).trim();
                mBinding.tvFilter.setText(normalizedInput);
                filter(normalizedInput);
            } else {
                mBinding.filter.setVisibility(View.GONE);
                mBinding.tvFilter.setText(null);
                filter(null);
            }
        }
    }

    /**
     * @param <T> Needed for the data binding tool to generate a FragmentResultListBinding class which compiles.
     */
    public class HeaderButtonListener<T> {

        public void onPlayButtonClicked(@SuppressWarnings("UnusedParameters") View v) {
            mTts.speak(mBinding.tvListHeader.getText().toString());
        }

        public void onFavoriteButtonClicked(View v) {
            String word = mBinding.tvListHeader.getText().toString();
            ((OnFavoriteClickListener) getActivity()).onFavoriteToggled(word, ((CheckBox)v).isChecked());
        }

        public void onWebSearchButtonClicked(@SuppressWarnings("UnusedParameters") View v) {
            Intent searchIntent = new Intent(Intent.ACTION_WEB_SEARCH);
            String word = mBinding.tvListHeader.getText().toString();
            searchIntent.putExtra(SearchManager.QUERY, word);
            // No apps can handle ACTION_WEB_SEARCH.  We'll try a more generic intent instead
            if (getActivity().getPackageManager().queryIntentActivities(searchIntent, 0).isEmpty()) {
                searchIntent = new Intent(Intent.ACTION_SEND);
                searchIntent.setType("text/plain");
                searchIntent.putExtra(Intent.EXTRA_TEXT, word);
            }
            startActivity(Intent.createChooser(searchIntent, getString(R.string.action_web_search, word)));
        }

        public void onFilterButtonClicked(@SuppressWarnings("UnusedParameters") View v) {
            InputDialogFragment fragment = ResultListFactory.createFilterDialog(
                    getActivity(),
                    mTab,
                    ACTION_FILTER,
                    mBinding.tvFilter.getText().toString());
            getChildFragmentManager().beginTransaction().add(fragment, DIALOG_TAG).commit();
        }

        public void onHelpButtonClicked(@SuppressWarnings("UnusedParameters") View v) {
            getFragmentManager().beginTransaction().add(new HelpDialogFragment(), DIALOG_TAG).commit();
        }

        public void onFilterClearButtonClicked(@SuppressWarnings("UnusedParameters") View v) {
            mBinding.tvFilter.setText(null);
            mBinding.filter.setVisibility(View.GONE);
            filter(null);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onTtsInitialized(Tts.OnTtsInitialized event) {
        Log.d(TAG, mTab + ": onTtsInitialized() called with: " + "event = [" + event + "]");
        updatePlayButton();
    }

}
