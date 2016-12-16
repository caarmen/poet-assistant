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

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.Locale;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.Tts;
import ca.rmen.android.poetassistant.VectorCompat;
import ca.rmen.android.poetassistant.databinding.FragmentResultListBinding;
import ca.rmen.android.poetassistant.main.Tab;


public class ResultListFragment<T> extends Fragment
        implements
        LoaderManager.LoaderCallbacks<ResultListData<T>>,
        InputDialogFragment.InputDialogListener,
        ConfirmDialogFragment.ConfirmDialogListener,
        ResultListHeader.HeaderButtonCallback {
    private static final String TAG = Constants.TAG + ResultListFragment.class.getSimpleName();
    private static final String EXTRA_FILTER = "filter";
    public static final String EXTRA_TAB = "tab";
    static final String EXTRA_QUERY = "query";
    private FragmentResultListBinding mBinding;

    private Tab mTab;
    private ResultListAdapter<T> mAdapter;
    private ResultListData<T> mData;
    @Inject Tts mTts;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        mTab = (Tab) getArguments().getSerializable(EXTRA_TAB);
        ResultListFactory.inject(mTab, this);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_result_list, container, false);
        View view = mBinding.getRoot();
        mBinding.resultListHeader.setResultListHeader(new ResultListHeader(mTab, this));
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mBinding.recyclerView.setHasFixedSize(true);

        ResultListFactory.updateListHeaderButtonsVisibility(mBinding, mTab, TextToSpeech.ERROR);
        mBinding.resultListHeader.tvFilterLabel.setText(ResultListFactory.getFilterLabel(getActivity(), mTab));


        if (savedInstanceState != null) {
            String query = savedInstanceState.getString(EXTRA_QUERY);
            String filter = savedInstanceState.getString(EXTRA_FILTER);
            mBinding.resultListHeader.tvListHeader.setText(query);
            mBinding.resultListHeader.tvFilter.setText(filter);
            mBinding.resultListHeader.filter.setVisibility(TextUtils.isEmpty(filter) ? View.GONE : View.VISIBLE);
        }

        EventBus.getDefault().register(this);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, mTab + ": onActivityCreated() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
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
        outState.putString(EXTRA_QUERY, mBinding.resultListHeader.tvListHeader.getText().toString());
        outState.putString(EXTRA_FILTER, mBinding.resultListHeader.tvFilter.getText().toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            Share.share(getActivity(), mTab,
                    mBinding.resultListHeader.tvListHeader.getText().toString(),
                    mBinding.resultListHeader.tvFilter.getText().toString(),
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
        mBinding.resultListHeader.tvListHeader.setText(query);
        mBinding.resultListHeader.filter.setVisibility(View.GONE);
        Bundle args = new Bundle(1);
        args.putString(EXTRA_QUERY, query);
        getLoaderManager().restartLoader(mTab.ordinal(), args, this);
    }

    private void filter(String filter) {
        Bundle args = new Bundle(2);
        args.putString(EXTRA_QUERY, mBinding.resultListHeader.tvListHeader.getText().toString());
        args.putString(EXTRA_FILTER, filter);
        getLoaderManager().restartLoader(mTab.ordinal(), args, this);
    }

    private void updatePlayButton() {
        ResultListFactory.updateListHeaderButtonsVisibility(mBinding, mTab, mTts.getStatus());
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
            mBinding.resultListHeader.tvListHeader.setText(query);
            mData = new ResultListData<>(query, false, Collections.emptyList());
        }
        mBinding.empty.setVisibility(View.GONE);
        mBinding.resultListHeader.listHeader.setVisibility(View.VISIBLE);
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
        int headerVisible = mAdapter.getItemCount() == 0 && TextUtils.isEmpty(mBinding.resultListHeader.tvListHeader.getText().toString()) ?
                View.GONE : View.VISIBLE;
        mBinding.resultListHeader.listHeader.setVisibility(headerVisible);

        String query = mData == null ? null : mData.matchedWord;
        mBinding.resultListHeader.tvListHeader.setText(query);
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

        if (mData != null) mBinding.resultListHeader.btnStarQuery.setChecked(mData.isFavorite);

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
        if (actionId == ResultListHeader.ACTION_FILTER) {
            if (!TextUtils.isEmpty(input)) {
                mBinding.resultListHeader.filter.setVisibility(View.VISIBLE);
                String normalizedInput = input.toLowerCase(Locale.getDefault()).trim();
                mBinding.resultListHeader.tvFilter.setText(normalizedInput);
                filter(normalizedInput);
            } else {
                mBinding.resultListHeader.filter.setVisibility(View.GONE);
                mBinding.resultListHeader.tvFilter.setText(null);
                filter(null);
            }
        }
    }

    @Override
    public void onOk(int actionId) {
        if (actionId == ResultListHeader.ACTION_CLEAR_FAVORITES) {
            new Favorites(getContext()).clear();
            Snackbar.make(mBinding.getRoot(), R.string.favorites_cleared, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFilterCleared() {
        filter(null);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onTtsInitialized(Tts.OnTtsInitialized event) {
        Log.d(TAG, mTab + ": onTtsInitialized() called with: " + "event = [" + event + "]");
        updatePlayButton();
    }

}
